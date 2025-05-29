package moe.ku6.yukireplay.playback;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import lombok.Getter;
import moe.ku6.yukireplay.YukiReplay;
import moe.ku6.yukireplay.api.codec.Instruction;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.exception.InvalidMagicException;
import moe.ku6.yukireplay.api.exception.PlaybackLoadException;
import moe.ku6.yukireplay.api.exception.ProtocolVersionMismatchException;
import moe.ku6.yukireplay.api.exception.VersionMismatchException;
import moe.ku6.yukireplay.api.nms.IVersionAdaptor;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackEntity;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import moe.ku6.yukireplay.api.util.CodecUtil;
import moe.ku6.yukireplay.api.util.Magic;
import net.kyori.adventure.util.Codec;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ReplayPlayback implements IPlayback, Listener {
    private final World world;
    @Getter
    private final byte[] metadata;
    private final List<List<Instruction>> frames = new ArrayList<>();
    private final List<Integer> schedulerHandles = new ArrayList<>();
    private final Set<Player> viewers = new HashSet<>();
    private final Map<Integer, TrackedPlaybackEntity> tracked = new HashMap<>();
    private int playhead = 0;
    private boolean closed;
    private boolean playing;

    public ReplayPlayback(World world, byte[] data) throws PlaybackLoadException {
        this.world = world;

        // parse data
        var buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.BIG_ENDIAN);

        {
            var magic = new byte[4];
            buf.get(magic);
            if (!Arrays.equals(magic, Magic.FORMAT_MAGIC))
                throw new InvalidMagicException();
        }

        {
            var version = buf.getShort();
            if (version != Magic.FORMAT_VERSION)
                throw new VersionMismatchException(version, Magic.FORMAT_VERSION);
        }

        {
            var protocolVersion = CodecUtil.ReadLengthPrefixed(buf);
            var expectedVersion = IVersionAdaptor.GetNMSVersion();
            if (!protocolVersion.equalsIgnoreCase(expectedVersion))
                throw new ProtocolVersionMismatchException(protocolVersion, expectedVersion);
        }

        {
            var metadataSize = buf.getInt();
            metadata = new byte[metadataSize];
            buf.get(metadata);
        }

        // end of header
        // start of data
        int frameNumber = 1;

        while (buf.hasRemaining()) {
            int frame = buf.getInt();
            if (frame != frameNumber)
                throw new PlaybackLoadException("Frame number mismatch, got %d, expected %d".formatted(frame, frameNumber));

            List<Instruction> instructions = new ArrayList<>();
            int instructionCount = buf.getShort();
            for (int i = 0; i < instructionCount; i++) {
                var id = buf.getShort();
                var type = InstructionType.ById(id);
                if (type == null)
                    throw new PlaybackLoadException("Unknown instruction type %d".formatted(id));

                int payloadLength = buf.getInt();
                var payload = buf.slice(buf.position(), payloadLength);
                buf.position(buf.position() + payloadLength);

                var instruction = type.CreateInstance(payload);
                instructions.add(instruction);
            }

            frames.add(instructions);
            ++frameNumber;
        }

        schedulerHandles.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(YukiReplay.getInstance(), this::Update, 0, 1));
        Bukkit.getPluginManager().registerEvents(this, YukiReplay.getInstance());
    }

    @Override
    public void AddViewers(Player... players) {
        EnsureValid();
        viewers.addAll(Arrays.asList(players));

        for (var player : players) {
            for (var trackedPlayer : tracked.values()) {
                trackedPlayer.SpawnFor(player);
            }
        }
    }

    @Override
    public void RemoveViewers(Player... players) {
        EnsureValid();
        for (var player : players) {
            viewers.remove(player);
        }

        for (var player : players) {
            for (var trackedPlayer : tracked.values()) {
                trackedPlayer.DespawnFor(player);
            }
        }
    }

    @Override
    public <T extends IPlaybackEntity> T GetTracked(int trackerId) {
        return (T) tracked.get(trackerId);
    }

    @Override
    public void AddTrackedEntity(IPlaybackEntity entity) {
        EnsureValid();
        if (tracked.containsKey(entity.GetTrackerId())) {
            throw new IllegalStateException("Entity with tracker id %d already exists".formatted(entity.GetTrackerId()));
        }
        if (!(entity instanceof TrackedPlaybackEntity trackedEntity)) {
            throw new IllegalArgumentException("Entity must be an instance of TrackedPlaybackEntity");
        }
        tracked.put(entity.GetTrackerId(), trackedEntity);
        viewers.forEach(trackedEntity::SpawnFor);
    }

    @Override
    public void AddTrackedPlayer(IPlaybackPlayer player) {
        AddTrackedEntity(player);
    }

    @Override
    public void RemoveTrackedEntity(IPlaybackEntity entity) {
        EnsureValid();
        if (!(entity instanceof TrackedPlaybackEntity trackedEntity)) {
            throw new IllegalArgumentException("Entity must be an instance of TrackedPlaybackEntity");
        }
        tracked.remove(trackedEntity.GetTrackerId());
        entity.Remove();
        viewers.forEach(trackedEntity::DespawnFor);
    }

    @Override
    public void RemoveTrackedPlayer(IPlaybackPlayer player) {
        RemoveTrackedEntity(player);
    }

    @Override
    public boolean IsPlaying() {
        return !closed && playing;
    }

    @Override
    public void SetPlaying(boolean playing) {
        EnsureValid();
        if (this.playing == playing) return;
        this.playing = playing;
    }

    @Override
    public int GetPlayhead() {
        return playhead;
    }

    @Override
    public int GetTotalFrames() {
        return frames.size();
    }

    @Override
    public Collection<Player> GetViewers() {
        return viewers;
    }

    @Override
    public void SendViewerPacket(PacketWrapper<?> packet) {
        var mgr = PacketEvents.getAPI().getPlayerManager();
        viewers.forEach(c -> mgr.sendPacket(c, packet));
    }

    @Override
    public World GetWorld() {
        return world;
    }

    @Override
    public void StepPlayback() {
        EnsureValid();
        if (playhead >= frames.size()) return;
        ++playhead;

//        System.out.println("playhead: " + playhead);
        for (var instruction : frames.get(playhead - 1)) {
            instruction.Apply(this);
        }
    }

    private void Update() {
        if (!playing) return;
        StepPlayback();
        if (playhead >= frames.size()) {
            playing = false;
        }
    }

    @Override
    public String GetMetadataAsString() {
        return new String(metadata, StandardCharsets.UTF_8);
    }

    @Override
    public void Close() {
        if (closed) return;
        closed = true;
        schedulerHandles.forEach(c -> Bukkit.getScheduler().cancelTask(c));
        schedulerHandles.clear();
        HandlerList.unregisterAll(this);
    }

    private void EnsureValid() {
        if (closed)
            throw new IllegalStateException("Playback is closed");
    }
}
