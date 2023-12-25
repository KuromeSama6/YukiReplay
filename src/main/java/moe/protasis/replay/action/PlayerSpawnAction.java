package moe.protasis.replay.action;

import com.google.gson.JsonObject;
import lombok.Getter;
import moe.icegame.coreutils.GameUtil;
import moe.protasis.replay.npc.PlayerNPC;
import moe.protasis.replay.playback.Playback;
import moe.protasis.replay.replay.Replay;
import net.jitse.npclib.internal.NPCBase;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PlayerSpawnAction extends Action {
    @Getter
    private final String name;
    @Getter
    private final Location location;
    @Getter
    private final Material heldItem;

    public PlayerSpawnAction(Replay replay, Player player) {
        super(replay, player);

        name = player.getName();
        location = player.getLocation();
        heldItem = player.getItemInHand().getType();
    }

    public PlayerSpawnAction(JsonObject data) {
        super(data);

        name = data.get("name").getAsString();
        location = GameUtil.DeserializeLocation(data.get("loc").getAsJsonObject());
        heldItem = Material.valueOf(data.get("heldItem").getAsString());
    }

    @Override
    protected void SerializeInternal(JsonObject data) {
        data.addProperty("name", name);
        data.add("loc", GameUtil.SerializeLocationJson(location));
        data.addProperty("heldItem", heldItem.toString());
    }

    @Override
    public void Execute(Playback playback) {
        PlayerNPC npc = new PlayerNPC(getUuid(), location);
        playback.getRepresenters().put(getUuid(), npc);

        for (Player viewer : playback.getViewers()) {
            npc.getNpc().show(viewer);
        }
    }
}
