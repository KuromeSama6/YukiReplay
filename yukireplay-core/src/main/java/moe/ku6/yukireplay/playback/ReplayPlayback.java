package moe.ku6.yukireplay.playback;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import lombok.Getter;
import moe.ku6.yukireplay.YukiReplay;
import moe.ku6.yukireplay.api.codec.*;
import moe.ku6.yukireplay.api.exception.InvalidMagicException;
import moe.ku6.yukireplay.api.exception.PlaybackLoadException;
import moe.ku6.yukireplay.api.exception.ProtocolVersionMismatchException;
import moe.ku6.yukireplay.api.exception.VersionMismatchException;
import moe.ku6.yukireplay.api.nms.IVersionAdaptor;
import moe.ku6.yukireplay.api.playback.EntityLifetime;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackEntity;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import moe.ku6.yukireplay.api.util.CodecUtil;
import moe.ku6.yukireplay.api.util.Magic;
import moe.ku6.yukireplay.api.util.Vec3i;
import moe.ku6.yukireplay.recorder.block.TrackedBlockChange;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ReplayPlayback implements IPlayback, Listener, PacketListener {
    private final World world;
    @Getter
    private final byte[] metadata;
    private final List<List<Instruction>> frames = new ArrayList<>();
    private final List<Integer> schedulerHandles = new ArrayList<>();
    private final Set<Player> viewers = new HashSet<>();
    private final Map<Integer, TrackedPlaybackEntity> tracked = new HashMap<>();
    private final Map<Vec3i, TrackedBlockChange> blockChanges = new HashMap<>();
    private int playhead = 0;
    private double extraFramesCounter = 0;
    private double speed = 1d;
    private boolean closed;
    private boolean playing, rewind;
    private final PacketListenerCommon packetListenerHandle;

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

        // lifetime computation
        var openLifetimePeriods = new HashMap<Integer, Integer>();
        var lifetimes = new HashMap<Integer, EntityLifetime>();

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

                // lifetime computation
                if (instruction instanceof IEntityLifetimeStart lifetimeStart) {
                    var trackerId = lifetimeStart.GetTrackerId();
                    if (openLifetimePeriods.containsKey(trackerId))
                        throw new PlaybackLoadException("Entity %d has overlapping lifetime periods: last started at %d, new started at %d".formatted(trackerId, openLifetimePeriods.get(trackerId), frame));

                    openLifetimePeriods.put(trackerId, frame);
                    if (!lifetimes.containsKey(trackerId)) {
                        AddTrackedEntity(lifetimeStart.CreateEntity(this));
                    }
                }

                if (instruction instanceof IEntityLifetimeEnd lifetimeEnd) {
                    var trackerId = lifetimeEnd.GetTrackerId();
                    if (!openLifetimePeriods.containsKey(trackerId)) {
//                        YukiReplay.getInstance().getLog().warning("Skipping entity %s's lifetime period end on %d - no start found (%s)".formatted(trackerId, frame, lifetimeEnd));
                        continue;
                    }

                    var start = openLifetimePeriods.remove(trackerId);

                    var lifetime = lifetimes.computeIfAbsent(trackerId, a -> new EntityLifetime());
                    lifetime.AddPeriod(start, frame);
                }

            }

            frames.add(instructions);
            ++frameNumber;
        }

        // close unfinished lifetimes
        for (var entry : openLifetimePeriods.entrySet()) {
            var trackerId = entry.getKey();
            var startFrame = entry.getValue();
//            YukiReplay.getInstance().getLog().warning("Closing entity %s's lifetime period on %d - no end found".formatted(trackerId, frameNumber));
            var lifetime = lifetimes.computeIfAbsent(trackerId, a -> new EntityLifetime());
            lifetime.AddPeriod(startFrame, frameNumber);
        }

        for (var trackerId : lifetimes.keySet()) {
            var entity = GetTracked(trackerId);
            if (entity != null) {
                entity.SetLifetime(lifetimes.get(trackerId));
            }
        }


        schedulerHandles.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(YukiReplay.getInstance(), this::Update, 0, 1));
        Bukkit.getPluginManager().registerEvents(this, YukiReplay.getInstance());
        packetListenerHandle = PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.HIGH);
    }


    @Override
    public void AddViewers(Player... players) {
        EnsureValid();
        viewers.addAll(Arrays.asList(players));

        for (var player : players) {
            for (var entity : tracked.values()) {
                if (entity.getLifetime().IsAlive(Math.max(playhead, 1))) entity.SpawnFor(player);
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
    public List<IPlaybackPlayer> GetTrackedPlayers() {
        return tracked.values().stream()
            .filter(c -> c instanceof IPlaybackPlayer)
            .map(c -> (IPlaybackPlayer) c)
            .toList();
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
    }

    @Override
    public void AddTrackedPlayer(IPlaybackPlayer player) {
        AddTrackedEntity(player);
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
    public double GetSpeed() {
        return speed;
    }

    @Override
    public void SetSpeed(double speed) {
        EnsureValid();
        if (speed <= 0) {
            throw new IllegalArgumentException("Speed must be greater than 0");
        }
        extraFramesCounter = 0;
        this.speed = speed;
    }

    @Override
    public boolean IsRewinding() {
        return rewind;
    }

    @Override
    public void SetRewinding(boolean rewinding) {
        rewind = rewinding;
    }

    @Override
    public World GetWorld() {
        return world;
    }

    private void AdvancePlayback() {
        if (rewind) RewindPlayback();
        else StepPlayback();
    }

    @Override
    public void StepPlayback() {
        EnsureValid();
        if (playhead >= frames.size()) return;

//        System.out.println("playhead: " + playhead);
        for (var instruction : frames.get(playhead)) {
            instruction.Apply(this);

            if (instruction instanceof IBlockChange blockChange) {
                var pos = blockChange.GetLocation();
                if (!blockChanges.containsKey(pos)) {
                    var block = pos.ToLocation(world).getBlock();
                    blockChanges.put(pos, new TrackedBlockChange(pos, block.getType(), block.getData()));
                }
            }
        }

        TickEntities();
        ++playhead;
    }

    @Override
    public void RewindPlayback() {
        EnsureValid();
        if (playhead <= 1) return; // cant rewind before the first frame
        --playhead;

//        System.out.println("playhead: " + playhead);
        for (var instruction : frames.get(playhead)) {
            instruction.Rewind(this);
        }

        TickEntities();
    }

    @Override
    public void Restart() {
        EnsureValid();
        playhead = 1;
        playing = false;

        ResetChangedBlocks();

        StepPlayback();
    }

    private void ResetChangedBlocks() {
        for (var trackedBlock : blockChanges.values()) {
            var pos = trackedBlock.getLocation().ToLocation(world);
            viewers.forEach(c -> c.sendBlockChange(pos, trackedBlock.getMaterial(), trackedBlock.getData()));
        }
    }

    private void TickEntities() {
        for (var tracked : tracked.values()) {
            try {
                tracked.Tick(playhead);
            } catch (Exception e) {
                YukiReplay.getInstance().getLog().warning("Error ticking entity %s at frame %d: %s".formatted(tracked.GetTrackerId(), playhead, e.getMessage()));
                e.printStackTrace();
            }
        }
    }

    private void Update() {
        if (!playing) return;

        if (speed < 1) {
            double framesToSkipPerFrame = 1.0 - speed;

            extraFramesCounter += framesToSkipPerFrame;

            boolean skipThisFrame = false;
            if (extraFramesCounter >= 1.0) {
                extraFramesCounter -= 1.0;
                skipThisFrame = true;
            }

            if (!skipThisFrame) {
                AdvancePlayback(); // Only advance if not skipping this frame
            }

        } else if (speed > 1) {
            // fast-forward
            double extraFramesPerFrame = speed - 1.0;

            // Accumulate fractional frames
            extraFramesCounter += extraFramesPerFrame;

            boolean showExtra = false;
            if (extraFramesCounter >= 1.0) {
                extraFramesCounter -= 1.0;
                showExtra = true;
            }

            AdvancePlayback();
            if (showExtra) AdvancePlayback();

        } else {
            // normal speed
            AdvancePlayback();
        }

        if (playhead >= frames.size()) {
            playing = false;
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT) {
            var packet = new WrapperPlayClientPlayerBlockPlacement(event);
            var pos = new Vec3i(packet.getBlockPosition());
            if (blockChanges.containsKey(pos)) {
                event.setCancelled(true);
            }
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
        PacketEvents.getAPI().getEventManager().unregisterListener(packetListenerHandle);
    }

    private void EnsureValid() {
        if (closed)
            throw new IllegalStateException("Playback is closed");
    }
}
