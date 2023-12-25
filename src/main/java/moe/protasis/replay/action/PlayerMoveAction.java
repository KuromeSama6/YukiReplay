package moe.protasis.replay.action;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.google.gson.JsonObject;
import moe.protasis.replay.playback.Playback;
import moe.protasis.replay.replay.Replay;
import moe.protasis.replay.util.PacketUtil;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import oracle.net.ns.Packet;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveAction extends Action {
    private double x, y, z;
    private float yaw, pitch;

    public PlayerMoveAction(Replay replay, PlayerMoveEvent e) {
        super(replay, e.getPlayer());

        Location to = e.getTo();
        x = to.getX();
        y = to.getY();
        z = to.getZ();
        yaw = to.getYaw();
        pitch = to.getPitch();
    }

    public PlayerMoveAction(JsonObject data) {
        super(data);

        x = data.get("x").getAsDouble();
        y = data.get("y").getAsDouble();
        z = data.get("z").getAsDouble();
        yaw = data.get("yaw").getAsFloat();
        pitch = data.get("pitch").getAsFloat();
    }

    @Override
    protected void SerializeInternal(JsonObject data) {
        data.addProperty("x", x);
        data.addProperty("y", y);
        data.addProperty("z", z);
        data.addProperty("yaw", yaw);
        data.addProperty("pitch", pitch);
    }

    @Override
    public void Execute(Playback playback) {
        int eid = playback.GetRepresenter(this).getEntityId();

        {
            // teleport
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
            packet.getIntegers()
                    .write(0, eid)
                    .write(1, (int) (x * 32))
                    .write(2, (int) (y * 32))
                    .write(3, (int) (z * 32));
            playback.SendPacket(packet);
        }

        {
            // head yaw
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
            packet.getIntegers()
                    .write(0, eid);

            packet.getBytes()
                    .write(0, (byte) PacketUtil.GetCompressedAngle(yaw));

            playback.SendPacket(packet);
        }

        {
            // head pitch
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
            packet.getIntegers()
                    .write(0, eid);

            packet.getBytes()
                    .write(0, (byte) PacketUtil.GetCompressedAngle(yaw))
                    .write(1, (byte) PacketUtil.GetCompressedAngle(pitch));

            playback.SendPacket(packet);
        }
    }
}
