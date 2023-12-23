package moe.protasis.replay.replay;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import moe.protasis.replay.YukiReplay;
import moe.protasis.replay.action.Action;
import moe.protasis.replay.action.PlayerLeaveAction;
import moe.protasis.replay.action.PlayerMoveAction;
import moe.protasis.replay.util.CompressionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an ongoing replay that is actively recording activities.
 */
public class Replay implements Listener {
    /**
     * Whether this replay is actively recording.
     */
    @Getter @Setter
    private boolean recording;

    /**
     * The size limit of this replay. Only the latest <code>sizeLimit</code> frames of the replay
     * will be saved. The rest will be deleted. Set to -1 to disable.
     */
    @Getter@Setter
    private int sizeLimit = -1;

    @Getter
    private int frame;
    private final List<Player> players = new ArrayList<>();
    private final List<Action> actions = new ArrayList<>();

    public Replay() {
        Bukkit.getPluginManager().registerEvents(this, YukiReplay.getInstance());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(YukiReplay.getInstance(), this::TickFrame, 0, 1);
    }

    public void AddPlayer(Player player) {
        players.add(player);
    }

    public void RemovePlayer(Player player) {
        players.add(player);
    }

    public byte[] SaveToBytes() throws IOException {
        JsonArray arr = new JsonArray();
        for (Action action : actions) arr.add(action.Serialize());

        return CompressionUtil.CompressToByteArray(arr.toString());
    }

    public void Save() throws IOException { Save(DateTime.now().toString("yyyy-dd-M-HH-mm-ss")); }
    public void Save(String name) throws IOException {
        File dir = new File(YukiReplay.getInstance().getDataFolder() + "/replays");
        dir.mkdirs();
        File file = new File(dir + String.format("/%s.repl", name));
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(SaveToBytes());
        }
    }

    @EventHandler
    private void OnPlayerMove(PlayerMoveEvent e) {
        if (!EnsureThis(e)) return;
        actions.add(new PlayerMoveAction(frame, e));
    }

    @EventHandler
    private void OnPlayerQuit(PlayerQuitEvent e) {
        if (!EnsureThis(e)) return;
        actions.add(new PlayerLeaveAction(frame));
        players.remove(e.getPlayer());
    }

    @EventHandler
    private void OnPlayerChangeWorld(PlayerChangedWorldEvent e) {
        if (!EnsureThis(e)) return;
        actions.add(new PlayerLeaveAction(frame));
        players.remove(e.getPlayer());
    }

    private boolean EnsureThis(PlayerEvent e) {
        return recording && players.contains(e.getPlayer());
    }

    private void TickFrame() {
        if (!recording) return;

        ++frame;
    }

}