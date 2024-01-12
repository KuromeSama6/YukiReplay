package moe.protasis.replay.action;

import com.google.gson.JsonObject;
import moe.protasis.replay.npc.PlayerNPC;
import moe.protasis.replay.playback.Playback;
import moe.protasis.replay.replay.Replay;
import org.bukkit.entity.Player;

public class LeaveAction extends Action {
    public LeaveAction(Replay replay, Player player) {
        super(replay, player);
    }

    public LeaveAction(JsonObject data) {
        super(data);
    }

    @Override
    protected void SerializeInternal(JsonObject data) {

    }

    @Override
    public void Execute(Playback playback) {
        PlayerNPC npc = playback.getRepresenters().get(getUuid());
        if (npc == null) return;
        for (Player viewer : playback.getViewers()) npc.getNpc().hide(viewer);

        playback.getRepresenters().remove(getUuid());
    }
}
