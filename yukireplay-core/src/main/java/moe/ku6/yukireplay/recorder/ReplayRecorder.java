package moe.ku6.yukireplay.recorder;

import lombok.Getter;
import moe.ku6.yukireplay.codec.Instruction;
import moe.ku6.yukireplay.codec.impl.player.InstructionAddPlayer;
import moe.ku6.yukireplay.codec.impl.player.InstructionRemovePlayer;
import moe.ku6.yukireplay.codec.impl.util.InstructionFrameEnd;
import moe.ku6.yukireplay.util.Magic;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * A ReplayRecorder records the game state of a world and its players. ReplayRecorders are designed to work best with small maps in minigame or PVP servers.
 * <br>
 * Thread safety: ReplayRecorder is not thread safe. It is designed to be used on the main thread only.
 */
public class ReplayRecorder implements Listener {
    private final JavaPlugin plugin;
    private final RecorderOptions options;
    private final World world;
    private final List<Integer> schedulerHandles = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private final Set<Chunk> chunks = new HashSet<>();
    private final Queue<Instruction> scheduledInstructions = new ArrayDeque<>();
    private final List<Instruction> instructions;
    private final DataOutputStream outputStream = new DataOutputStream(new ByteArrayOutputStream());
    @Getter
    private boolean closed;
    @Getter
    private boolean recording;
    /**
     * A frame is a single tick of the game.
     */
    @Getter
    private int frame;

    public ReplayRecorder(JavaPlugin plugin, World world, RecorderOptions options) {
        this.plugin = plugin;
        this.world = world;
        this.options = options;

        if (options.isAutoAddPlayers()) AddChunks(world.getLoadedChunks());

        instructions = new ArrayList<>(options.getInitialSize());

        Bukkit.getPluginManager().registerEvents(this, plugin);
        schedulerHandles.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::Update, 0, 1));
    }

    public ReplayRecorder(JavaPlugin plugin, World world) {
        this(plugin, world, RecorderOptions.Default());
    }

    public void AddChunks(Chunk... chunks) {
        EnsureValid();
        for (var chunk : chunks) {
            if (!chunk.isLoaded())
                throw new IllegalStateException("Chunk " + chunk + " is not loaded");
        }
    }

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

    public void StartRecording() {
        EnsureValid();
        EnsureIsRecording(false);

        if (chunks.isEmpty())
            throw new IllegalStateException("No chunks added. Add chunks before starting recording.");

        recording = true;
    }

    public void StopRecording() {
        EnsureValid();
        EnsureIsRecording(true);
        recording = false;
    }

    public void SetRecording(boolean recording) {
        EnsureValid();
        if (this.recording == recording) return;
        if (recording) {
            StartRecording();
        } else {
            StopRecording();
        }
    }

    public void Close() {
        if (closed) return;
        closed = true;
        schedulerHandles.forEach(Bukkit.getScheduler()::cancelTask);
        schedulerHandles.clear();
        players.clear();
        chunks.clear();
        instructions.clear();
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HandlerList.unregisterAll(this);
    }

    /**
     * Serialize all frames recorded so far, and return the serialized data in a byte array.
     * <br>
     * Note 1: Upon calling this method, all recorded frames will be cleared and serialized. These frames will still be kept in memory, but no longer modifiable. Subsequent calls will return the same data in addition to the new frames recorded until flushed.
     * <br>
     * Note 2: To discard the recorded frames after returning them, pass true to the flush parameter. After flushing, subsequent calls will only return new frames recorded, and will no longer contain headers and metadata. Use this if you want to flush the recorded frames to a file or a database.
     */
    public byte[] Serialize(boolean flush) throws IOException{
        EnsureValid();

        if (outputStream.size() == 0) {
            // write headers and metadata
            {
                // headers
                outputStream.write(Magic.FORMAT_MAGIC);
            }
        }

        throw new NotImplementedException();
    }

    private void Update() {
        if (closed) return;
        if (!recording) return;

        ++frame;
        while (!scheduledInstructions.isEmpty()) {
            var instruction = scheduledInstructions.poll();
            instructions.add(instruction);
        }

        instructions.add(InstructionFrameEnd.INSTANCE);
    }

    private void ScheduleInstruction(Instruction instruction) {
        EnsureValid();
        if (!recording) throw new IllegalStateException("ReplayRecorder is not recording");
        Objects.requireNonNull(instruction, "instruction");

        scheduledInstructions.add(instruction);
    }

    private void AddTrackedPlayer(Player player) {
        EnsureValid();

        ScheduleInstruction(new InstructionAddPlayer(player));
    }

    private void RemoveTrackedPlayer(Player player) {
        EnsureValid();

        ScheduleInstruction(new InstructionRemovePlayer(player));
    }

    private void EnsureValid() {
        if (closed)
            throw new IllegalStateException("ReplayRecorder is closed");
    }

    private void EnsureIsRecording(boolean recording) {
        if (this.recording != recording)
            throw new IllegalStateException("ReplayRecorder is not " + (recording ? "recording" : "not recording"));
    }
}
