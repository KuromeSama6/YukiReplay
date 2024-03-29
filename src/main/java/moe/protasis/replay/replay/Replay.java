package moe.protasis.replay.replay;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import moe.protasis.replay.YukiReplay;
import moe.protasis.replay.action.*;
import moe.protasis.replay.util.CompressionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
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
    private final List<PacketListener> packetListeners = new ArrayList<>();

    public Replay() {
        Bukkit.getPluginManager().registerEvents(this, YukiReplay.getInstance());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(YukiReplay.getInstance(), this::TickFrame, 0, 1);

        AddPacketListener(new PacketAdapter(YukiReplay.getInstance(),
                PacketType.Play.Client.ARM_ANIMATION) {
            @Override
            public void onPacketReceiving(PacketEvent e) {
                if (!EnsureThis(e)) return;
                actions.add(new AnimationAction(Replay.this, e.getPlayer(), 0));
            }
        });
    }

    public void AddPlayer(Player player) {
        players.add(player);
        actions.add(new SpawnAction(this, player));
    }

    public void RemovePlayer(Player player) {
        players.add(player);
    }

    public byte[] SaveToBytes() throws IOException {
        JsonObject ret = new JsonObject();
        ret.addProperty("version", YukiReplay.REPLAY_FORMAT_VERSION);
        ret.addProperty("timestamp", DateTime.now().getMillis());

        {
            JsonArray arr = new JsonArray();
            for (Action action : actions) arr.add(action.Serialize());
            ret.add("frames", arr);
        }

        return CompressionUtil.CompressToByteArray(ret.toString());
    }

    public void Save() throws IOException { Save(DateTime.now().toString("yyyy-dd-M-HH-mm-ss")); }
    public void Save(String name) throws IOException {
        File dir = new File(YukiReplay.getInstance().getDataFolder() + "/replays");
        dir.mkdirs();
        Save(new File(dir + String.format("/%s.repl", name)));
    }
    public void Save(File file) throws IOException{
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(SaveToBytes());
        }
    }

    public void Close() {
        for (PacketListener listener : packetListeners)
            ProtocolLibrary.getProtocolManager().removePacketListener(listener);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnPlayerMove(PlayerMoveEvent e) {
        if (!EnsureThis(e)) return;
        actions.add(new MoveAction(this, e));
        actions.add(new HeadRotationAction(this, e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnPlayerQuit(PlayerQuitEvent e) {
        if (!EnsureThis(e)) return;
        actions.add(new LeaveAction(this, e.getPlayer()));
        players.remove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnPlayerChangeWorld(PlayerChangedWorldEvent e) {
        if (!EnsureThis(e)) return;
        actions.add(new LeaveAction(this, e.getPlayer()));
        players.remove(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnEntityDamage(EntityDamageEvent e) {
        if (!EnsureThis(e)) return;
        Player player = (Player) e.getEntity();
        actions.add(new DamageAction(this, player));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnPlayerItemHeld(PlayerItemHeldEvent e) {
        if (!EnsureThis(e)) return;
        actions.add(new InventoryUpdateAction(this, e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnInventoryClick(InventoryInteractEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (!EnsureThis(player)) return;
        actions.add(new InventoryUpdateAction(this, player));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnPlayerDropItem(PlayerDropItemEvent e) {
        if (!EnsureThis(e)) return;
        actions.add(new InventoryUpdateAction(this, e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnPlayerPickupItem(PlayerPickupItemEvent e) {
        if (!EnsureThis(e)) return;
        actions.add(new InventoryUpdateAction(this, e.getPlayer()));
    }

    private boolean EnsureThis(PlayerEvent e) {
        return EnsureThis(e.getPlayer());
    }
    private boolean EnsureThis(EntityEvent e) {
        return recording && e.getEntity() instanceof Player && players.contains((Player) e.getEntity());
    }
    private boolean EnsureThis(PacketEvent e) {
        return EnsureThis(e.getPlayer());
    }
    private boolean EnsureThis(Player player) {
        return recording && players.contains(player);
    }

    private void TickFrame() {
        if (!recording) return;

        ++frame;
    }

    private void AddPacketListener(PacketListener packetListener) {
        packetListeners.add(packetListener);
        ProtocolLibrary.getProtocolManager().addPacketListener(packetListener);
    }

}