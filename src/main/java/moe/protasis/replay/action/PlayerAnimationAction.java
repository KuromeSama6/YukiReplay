package moe.protasis.replay.action;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.google.gson.JsonObject;
import lombok.Getter;
import moe.protasis.replay.playback.Playback;
import moe.protasis.replay.replay.Replay;
import org.bukkit.entity.Player;

public class PlayerAnimationAction extends Action{
    @Getter
    private int animation;

    public PlayerAnimationAction(Replay replay, Player player, int animation) {
        super(replay, player);
        this.animation = animation;
    }

    public PlayerAnimationAction(JsonObject data) {
        super(data);
        animation = data.get("animation").getAsInt();
    }

    @Override
    protected void SerializeInternal(JsonObject data) {
        data.addProperty("animation", animation);
    }

    @Override
    public void Execute(Playback playback) {
        int eid = playback.GetRepresenter(this).getEntityId();
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ANIMATION);
        packet.getIntegers().write(0, eid)
                        .write(1, animation);
        playback.SendPacket(packet);
    }
}
