package moe.protasis.replay.playback;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import moe.icegame.coreutils.classes.PooledScheduler;
import moe.protasis.replay.YukiReplay;
import moe.protasis.replay.action.Action;
import moe.protasis.replay.npc.PlayerNPC;
import moe.protasis.replay.packetwrapper.util.Removed;
import moe.protasis.replay.util.CompressionUtil;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.*;

public class Playback {
    @Getter
    private final Map<UUID, PlayerNPC> representers = new HashMap<>();
    private final Map<Integer, List<Action>> actions = new HashMap<>();
    @Getter
    private final List<Player> viewers = new ArrayList<>();
    @Getter
    private final World world;
    private final PooledScheduler scheduler = new PooledScheduler(YukiReplay.getInstance());

    @Getter
    private int length;
    @Getter
    private int frame;
    @Getter @Setter
    private boolean playing;

    public Playback(World world, JsonObject data) {
        this.world = world;

        for (JsonElement e : data.get("frames").getAsJsonArray()) {
            if (!e.isJsonObject()) continue;
            JsonObject object = e.getAsJsonObject();
            String type = object.get("_type").getAsString();
            int frame = object.get("frame").getAsInt();

            // reflections
            try {
                Class<?> clazz = Class.forName(type);
                Action action = (Action) clazz
                        .getDeclaredConstructor(JsonObject.class)
                        .newInstance(object);

                if (!actions.containsKey(frame)) actions.put(frame, new ArrayList<>());
                actions.get(frame).add(action);

            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException ex) {
                YukiReplay.getInstance().getLogger().severe(String.format("A bad frame is encountered during replay parsing at frame %s", frame));
                ex.printStackTrace();
            }

        }

        length = actions.keySet().stream()
                    .mapToInt(Integer::intValue)
                    .max().orElse(0);

        if (length == 0) {
            YukiReplay.getInstance().getLogger().warning("Tried to play an empty replay???");
            return;
        }

        scheduler.AddRepeating(this::Tick, 0, 1);
    }

    private void Tick() {
        if (!playing) return;
        if (actions.containsKey(frame)) {
            for (Action action : actions.get(frame)) {
                try {
                    action.Execute(this);
                } catch (Exception e) {
                    YukiReplay.getInstance().getLogger().severe(String.format("An error occured on frame %s while executing %s:", frame, action.getClass().getName()));
                    e.printStackTrace();
                }
            }
        }

        ++frame;
        if (frame >= length) {
            playing = false;
        }
    }

    public PlayerNPC GetRepresenter(Action action) {
        return representers.get(action.getUuid());
    }

    public void AddViewer(Player player) {
        viewers.add(player);

        for (PlayerNPC npc : representers.values()) npc.getNpc().show(player);
    }

    public void RemoveViewer(Player player) {
        viewers.remove(player);

        for (PlayerNPC npc : representers.values()) npc.getNpc().hide(player);
    }

    public void SendPacket(Packet<?> packet) {
        for (Player player : viewers) SendPacket(player, packet);
    }

    public void SendPacket(Player target, Packet<?> packet) {
        ((CraftPlayer) target).getHandle().playerConnection.sendPacket(packet);
    }

    public void SendPacket(PacketContainer packet) {
        for (Player player : viewers) SendPacket(player, packet);
    }

    public void SendPacket(Player target, PacketContainer packet) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(target, packet);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public static Playback LoadFromDirectory(String name, World world) throws IOException {
        File file = new File(YukiReplay.getInstance().getDataFolder() + "/replays/" + name + ".repl");
        if (!file.exists()) return null;

        byte[] data = Files.readAllBytes(file.toPath());
        return new Playback(world, new Gson().fromJson(CompressionUtil.DecompressToString(data), JsonObject.class));
    }

    public void Close() {
        playing = false;
        scheduler.Free();

        for (Player viewer : viewers.toArray(new Player[0])) RemoveViewer(viewer);
        for (PlayerNPC npc : representers.values()) npc.Destroy();
    }
}
