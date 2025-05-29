package moe.ku6.yukireplay.api.codec.impl.player;

import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class InstructionPearlTeleport extends PlayerInstruction {
    private final int pearlTrackerId;
    private final double posX, posY, posZ;

    public InstructionPearlTeleport(ByteBuffer buf) {
        super(buf);
        pearlTrackerId = buf.getInt();
        posX = buf.getDouble();
        posY = buf.getDouble();
        posZ = buf.getDouble();
    }

    public InstructionPearlTeleport(int trackerId, int pearlTrackerId, EnderPearl pearl) {
        super(trackerId);
        this.pearlTrackerId = pearlTrackerId;

        var pos = pearl.getLocation();
        posX = pos.getX();
        posY = pos.getY();
        posZ = pos.getZ();
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        out.writeInt(pearlTrackerId);
        out.writeDouble(posX);
        out.writeDouble(posY);
        out.writeDouble(posZ);
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.PEARL_TELEPORT;
    }

    @Override
    public void Apply(IPlayback playback) {
        var tracked = playback.GetTracked(pearlTrackerId);
        if (tracked != null) playback.RemoveTrackedEntity(tracked);
        var world = playback.GetWorld();
        var pos = new Location(world, posX, posY, posZ);

        for (var viewer : playback.GetViewers()) {
            viewer.playSound(pos, Sound.ENDERMAN_TELEPORT, 1, 1);
            viewer.playEffect(pos, Effect.ENDER_SIGNAL, null);
        }
    }
}
