package moe.protasis.replay.action;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.google.gson.JsonObject;
import moe.protasis.replay.playback.Playback;
import moe.protasis.replay.replay.Replay;
import org.bukkit.entity.Player;

public class DamageAction extends Action {
    public DamageAction(Replay replay, Player player) {
        super(replay, player);
    }

    public DamageAction(JsonObject data) {
        super(data);
    }

    @Override
    protected void SerializeInternal(JsonObject data) {

    }

    @Override
    public void Execute(Playback playback) {
        int eid = playback.GetRepresenter(this).getEntityId();
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ANIMATION);
        packet.getIntegers()
                .write(0, eid)
                .write(1, 1);

        playback.SendPacket(packet);
    }
}
