package moe.protasis.replay.action;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.google.gson.JsonObject;
import lombok.Getter;
import moe.protasis.replay.playback.Playback;
import moe.protasis.replay.replay.Replay;
import moe.protasis.replay.util.PacketUtil;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Player;

public class PlayerHeadRotAction extends Action{
    @Getter
    private byte rotation;

    public PlayerHeadRotAction(Replay replay, Player player) {
        super(replay, player);

        float rot = ((CraftLivingEntity)player).getHandle().getHeadRotation();
        rotation = (byte) PacketUtil.CompressAngle(rot);
    }

    public PlayerHeadRotAction(JsonObject data) {
        super(data);
        rotation = data.get("rotation").getAsByte();
    }

    @Override
    protected void SerializeInternal(JsonObject data) {
        data.addProperty("rotation", rotation);
    }

    @Override
    public void Execute(Playback playback) {
        int eid = playback.GetRepresenter(this).getEntityId();
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        packet.getIntegers().write(0, eid);
        packet.getBytes().write(0, rotation);
        playback.SendPacket(packet);
    }
}
