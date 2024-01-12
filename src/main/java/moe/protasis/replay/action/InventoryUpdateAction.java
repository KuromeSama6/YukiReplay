package moe.protasis.replay.action;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.google.gson.JsonObject;
import lombok.Getter;
import moe.protasis.replay.playback.Playback;
import moe.protasis.replay.replay.Replay;
import moe.protasis.replay.util.InventorySnapshot;
import org.bukkit.entity.Player;

public class InventoryUpdateAction extends Action {
    @Getter
    private final InventorySnapshot inventory;

    public InventoryUpdateAction(Replay replay, Player player) {
        super(replay, player);
        inventory = new InventorySnapshot(player);
    }

    public InventoryUpdateAction(JsonObject data) {
        super(data);
        inventory = new InventorySnapshot(data.get("inventory").getAsJsonObject());
    }

    @Override
    protected void SerializeInternal(JsonObject data) {
        data.add("inventory", inventory.Serialize());
    }

    @Override
    public void Execute(Playback playback) {
        System.out.println(String.format("update inventory, %s", inventory));
        inventory.Apply(playback.GetRepresenter(this));
    }
}
