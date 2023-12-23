package moe.protasis.replay.action;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

public class PlayerLeaveAction extends Action {
    public PlayerLeaveAction(int frame) {
        super(frame);
    }

    public PlayerLeaveAction(JsonObject data) {
        super(data);
    }

    @Override
    protected void SerializeInternal(JsonObject data) {

    }

    @Override
    public void Apply(Player player) {

    }
}
