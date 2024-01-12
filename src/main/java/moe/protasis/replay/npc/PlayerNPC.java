package moe.protasis.replay.npc;

import lombok.Getter;
import moe.protasis.replay.YukiReplay;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.internal.NPCBase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Represents a NPC that is representing a player during a playback.
 */
public class PlayerNPC {
    @Getter private final UUID uuid;
    @Getter private NPC npc;
    @Getter private int entityId;
    @Getter private List<String> text = new ArrayList<>();

    public PlayerNPC(UUID uuid, Location location) {
        this.uuid = uuid;

        npc = CreateNpc(location);

        // use reflections to read eid
        try {
            Field eid = NPCBase.class.getDeclaredField("entityId");
            eid.setAccessible(true);
            entityId = (int)eid.get(npc);
            eid.setAccessible(false);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public NPC CreateNpc(Location location) {
        NPC ret = YukiReplay.getNpcLib().createNPC();
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        ret.setLocation(location);
        ret.create();

        return ret;
    }

    public void SetText(String... text) {
        this.text = Arrays.asList(text);
        npc.setText(this.text);
    }

    public void Destroy() {
        npc.destroy();
    }

}
