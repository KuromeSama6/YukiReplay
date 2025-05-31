package moe.ku6.yukireplay.recorder;

import lombok.Getter;
import moe.ku6.yukireplay.YukiReplay;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionEntityDespawn;
import moe.ku6.yukireplay.api.nms.IVersionAdaptor;
import moe.ku6.yukireplay.api.recorder.IRecorder;
import moe.ku6.yukireplay.api.codec.Instruction;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionAddPlayer;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionEntityPosition;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionRemovePlayer;
import moe.ku6.yukireplay.api.codec.impl.util.InstructionFrameEnd;
import moe.ku6.yukireplay.api.recorder.RecorderOptions;
import moe.ku6.yukireplay.api.util.CodecUtil;
import moe.ku6.yukireplay.api.util.Magic;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A ReplayRecorder records the game state of a world and its players. ReplayRecorders are designed to work best with small maps in minigame or PVP servers.
 * <br>
 * Thread safety: ReplayRecorder is not thread safe. It is designed to be used on the main thread only.
 */
public class ReplayRecorder implements IRecorder {
    private static int nextTrackerId = 128;
    private final YukiReplay plugin = YukiReplay.getInstance();
    private final RecorderOptions options;
    private final World world;
    private final List<Integer> schedulerHandles = new ArrayList<>();
    private final Set<Player> players = new HashSet<>();
    private final Set<Chunk> chunks = new HashSet<>();
    private final Queue<Instruction> scheduledInstructions = new ArrayDeque<>();
    private final List<Instruction> instructions;
    private final Map<UUID, TrackedRecordingEntity> trackedEntities = new HashMap<>();
    private final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    private final RecorderListener listener;
    @Getter
    private boolean closed;
    @Getter
    private boolean recording;
    /**
     * A frame is a single tick of the game.
     */
    @Getter
    private int frame;
    @Getter
    private byte[] optionalMetadata = new byte[0];
    private boolean headersWritten;
    private int writtenFrame = 0;

    public ReplayRecorder(World world, RecorderOptions options) {
        this.world = world;
        this.options = options;

        if (options.isAutoAddLoadedChunks()) AddChunks(world.getLoadedChunks());

        instructions = new ArrayList<>(options.getInitialSize());

        listener = new RecorderListener(this);
        schedulerHandles.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::Update, 0, 1));
    }

    public ReplayRecorder(World world) {
        this(world, RecorderOptions.Default());
    }

    public TrackedRecordingEntity GetTrackedEntity(Entity entity) {
        EnsureValid();
        if (!recording) return null;
        if (!trackedEntities.containsKey(entity.getUniqueId())) return null;

        return trackedEntities.get(entity.getUniqueId());
    }

    public TrackedRecordingPlayer GetTrackedPlayer(Entity entity) {
        EnsureValid();
        if (!(entity instanceof Player player)) return null;
        if (!recording) return null;
        if (!players.contains(player)) return null;

        return (TrackedRecordingPlayer)trackedEntities.get(player.getUniqueId());
    }

    public int GetNextTrackerId() {
        return nextTrackerId++;
    }

    @Override
    public void AddChunks(Chunk... chunks) {
        EnsureValid();
        for (var chunk : chunks) {
            if (!chunk.isLoaded())
                throw new IllegalStateException("Chunk " + chunk + " is not loaded");
            this.chunks.add(chunk);
        }
    }

    @Override
    public void AddPlayers(Player... players) {
        EnsureValid();
        for (var player : players) {
            if (!player.isOnline()) throw new IllegalStateException("Player " + player.getName() + " is not online");
            if (!this.players.contains(player)) {
                AddTrackedPlayer(player);
                continue;
            }

        }
        this.players.addAll(List.of(players));
    }

    @Override
    public void RemovePlayers(Player... players) {
        EnsureValid();
        for (var player : players) {
            if (!this.players.contains(player)) {
                RemoveTrackedPlayer(player);
                continue;
            }
        }
        this.players.removeAll(List.of(players));
    }

    @Override
    public void StartRecording() {
        EnsureValid();
        EnsureIsRecording(false);

        if (chunks.isEmpty())
            throw new IllegalStateException("No chunks added. Add chunks before starting recording.");

        recording = true;
    }

    @Override
    public void StopRecording() {
        EnsureValid();
        EnsureIsRecording(true);
        recording = false;
    }

    @Override
    public void SetRecording(boolean recording) {
        EnsureValid();
        if (this.recording == recording) return;
        if (recording) {
            StartRecording();
        } else {
            StopRecording();
        }
    }

    @Override
    public void Close() {
        if (closed) return;
        closed = true;
        listener.Close();
        schedulerHandles.forEach(Bukkit.getScheduler()::cancelTask);
        schedulerHandles.clear();
        players.clear();
        chunks.clear();
        instructions.clear();
        try {
            byteStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void SetMetadata(byte[] data) {
        EnsureValid();
        Objects.requireNonNull(data, "data");
        if (headersWritten)
            throw new IllegalStateException("Headers already written. Cannot set metadata after headers are written.");

        optionalMetadata = data;
    }

    public void SetMetadata(String data) {
        SetMetadata(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Serialize all frames recorded so far, and return the serialized data in a byte array.
     * <br>
     * Note 1: Upon calling this method, all recorded frames will be cleared and serialized. These frames will still be kept in memory, but no longer modifiable. Subsequent calls will return the same data in addition to the new frames recorded until flushed.
     * <br>
     * Note 2: To discard the recorded frames after returning them, pass true to the flush parameter. After flushing, subsequent calls will only return new frames recorded, and will no longer contain headers and metadata. Use this if you want to flush the recorded frames to a file or a database.
     */
    @Override
    public byte[] Serialize(boolean flush) throws IOException {
        EnsureValid();

        var outputStream = new DataOutputStream(byteStream);
        if (!headersWritten) {
            // write headers and metadata
            outputStream.write(Magic.FORMAT_MAGIC);
            outputStream.writeShort(Magic.FORMAT_VERSION);
            CodecUtil.WriteLengthPrefixed(outputStream, IVersionAdaptor.GetNMSVersion());
            outputStream.writeInt(optionalMetadata.length);
            outputStream.write(optionalMetadata);

            headersWritten = true;
        }


        int frameInstructionCount = 0;
        var frameOs = new ByteArrayOutputStream();
        DataOutputStream frameStream = new DataOutputStream(frameOs);

        for (var instruction : instructions) {
            if (instruction == InstructionFrameEnd.INSTANCE) {
                // end frame
                ++writtenFrame;
//                System.out.println("frame end: " + writtenFrame);
                outputStream.writeInt(writtenFrame);
                outputStream.writeShort(frameInstructionCount);
                outputStream.write(frameOs.toByteArray());

                frameInstructionCount = 0;
                frameOs.reset();
                frameStream = new DataOutputStream(frameOs);

            } else {
                var instructionOs = new ByteArrayOutputStream();
                DataOutputStream instructionStream = new DataOutputStream(instructionOs);
                frameStream.writeShort(instruction.GetType().getId());

                instruction.Serialize(instructionStream);

                frameStream.writeInt(instructionStream.size());
                frameStream.write(instructionOs.toByteArray());

                instructionOs.reset();

                ++frameInstructionCount;
//                System.out.println("instruction %s of frame %s".formatted(instruction, writtenFrame));
            }
        }

        var ret = byteStream.toByteArray();
        instructions.clear();

        if (flush) {
            byteStream.reset();
        }

        return ret;
    }

    private void Update() {
        if (closed) return;
        if (!recording) return;

        // remove out of bounds players
        for (var player : new ArrayList<>(players)) {
            if (options.isAutoRemovePlayers() && !chunks.contains(player.getLocation().getChunk())) {
                RemoveTrackedPlayer(player);
            }

            if (options.isAutoRemoveOfflinePlayers() && !player.isOnline()) {
                RemoveTrackedPlayer(player);
            }
        }

        // add in bounds players
        if (options.isAutoAddPlayers()) {
            for (var player : Bukkit.getOnlinePlayers()) {
                if (!players.contains(player) && chunks.contains(player.getLocation().getChunk())) {
                    AddTrackedPlayer(player);
                }
            }
        }

        for (var tracked : new ArrayList<>(trackedEntities.values())) {
            if (tracked instanceof TrackedRecordingPlayer) continue;
            if (!tracked.getEntity().isValid()) {
                RemoveTrackedEntity(tracked.getEntity());
                continue;
            }

            tracked.UpdatePosition();
        }

        trackedEntities.values().forEach(TrackedRecordingEntity::Update);
        if (frame % 20 == 0) {
            for (var tracked : trackedEntities.values()) {
                if (tracked instanceof TrackedRecordingPlayer trackedPlayer) {
                    trackedPlayer.SaveInventory();

                }
            }
        }

        ++frame;
        while (!scheduledInstructions.isEmpty()) {
            var instruction = scheduledInstructions.poll();
            instructions.add(instruction);
        }

        instructions.add(InstructionFrameEnd.INSTANCE);
    }

    @Override
    public synchronized void ScheduleInstruction(Instruction instruction) {
        EnsureValid();
        Objects.requireNonNull(instruction, "instruction");

        scheduledInstructions.add(instruction);
    }

    @Override
    public int GetTrackerId(Entity entity) {
        var ret = trackedEntities.get(entity.getUniqueId());
        if (ret == null) return -1;

        return ret.getTrackerId();
    }

    private void AddTrackedPlayer(Player player) {
        EnsureValid();

        var trackedPlayer = new TrackedRecordingPlayer(this, player);
        trackedEntities.put(player.getUniqueId(), trackedPlayer);

        ScheduleInstruction(new InstructionAddPlayer(player, trackedPlayer.getTrackerId()));
        ScheduleInstruction(new InstructionEntityPosition(player, trackedPlayer.getTrackerId()));
        trackedPlayer.SaveInventory();
    }

    public TrackedRecordingEntity AddTrackedEntity(Entity entity) {
        EnsureValid();
        if (trackedEntities.containsKey(entity.getUniqueId())) return null;

        var trackedEntity = new TrackedRecordingEntity(this, entity);
        trackedEntities.put(entity.getUniqueId(), trackedEntity);

        return trackedEntity;
    }

    private void RemoveTrackedPlayer(Player player) {
        EnsureValid();
        var trackedPlayer = trackedEntities.remove(player.getUniqueId());
        if (trackedPlayer == null) return;

        ScheduleInstruction(new InstructionRemovePlayer(player, trackedPlayer.getTrackerId()));
    }

    private void RemoveTrackedEntity(Entity entity) {
        EnsureValid();
        var trackedEntity = trackedEntities.remove(entity.getUniqueId());
        if (trackedEntity == null) return;

        ScheduleInstruction(new InstructionEntityDespawn(trackedEntity.getTrackerId()));
    }

    private void EnsureValid() {
        if (closed)
            throw new IllegalStateException("ReplayRecorder is closed");
    }

    private void EnsureIsRecording(boolean recording) {
        if (this.recording != recording)
            throw new IllegalStateException("ReplayRecorder is not " + (recording ? "recording" : "not recording"));
    }

    @Override
    public String toString() {
        return "ReplayRecorder(closed=%s, recording=%s, world=%s, frame=%s, players=%s, totalInstructions=%s".formatted(
            closed,
            recording,
            world.getName(),
            frame,
            players.size(),
            instructions.size()
        );
    }
}
