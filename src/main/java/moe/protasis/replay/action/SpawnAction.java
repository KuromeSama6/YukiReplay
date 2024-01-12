package moe.protasis.replay.action;

import com.google.gson.JsonObject;
import lombok.Getter;
import moe.icegame.coreutils.GameUtil;
import moe.protasis.replay.npc.PlayerNPC;
import moe.protasis.replay.playback.Playback;
import moe.protasis.replay.replay.Replay;
import moe.protasis.replay.util.InventorySnapshot;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SpawnAction extends Action {
    @Getter
    private final String name;
    @Getter
    private final Location location;
    @Getter
    private final InventorySnapshot inventory;
    @Getter
    private final double health;
    @Getter
    private final String displayName;

    public SpawnAction(Replay replay, Player player) {
        super(replay, player);

        name = player.getName();
        location = player.getLocation();
        inventory = new InventorySnapshot(player);
        health = player.getHealth();
        displayName = player.getDisplayName();
    }

    public SpawnAction(JsonObject data) {
        super(data);

        name = data.get("name").getAsString();
        location = GameUtil.DeserializeLocation(data.get("loc").getAsJsonObject());
        inventory = new InventorySnapshot(data.get("inventory").getAsJsonObject());
        health = data.get("health").getAsDouble();
        displayName = data.get("displayName").getAsString();
    }

    @Override
    protected void SerializeInternal(JsonObject data) {
        data.addProperty("name", name);
        data.add("loc", GameUtil.SerializeLocationJson(location));
        data.add("inventory", inventory.Serialize());
        data.addProperty("health", health);
        data.addProperty("displayName", displayName);
    }

    @Override
    public void Execute(Playback playback) {
        PlayerNPC npc = new PlayerNPC(getUuid(), location);
        npc.getNpc().setText(Arrays.asList(
                String.format("*%s", displayName),
                String.format("§c%s", health)
        ));

        inventory.Apply(npc);

        playback.getRepresenters().put(getUuid(), npc);

        for (Player viewer : playback.getViewers()) {
            npc.getNpc().show(viewer);
        }
    }
}
