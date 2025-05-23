package moe.ku6.yukireplay.api.codec.impl.player;

import lombok.ToString;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.util.CodecUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;

@ToString
public class InstructionPlayerPosition extends PlayerInstruction {
    /**
     * A bitset indicating which aspects of the player's position is modified, in the following order:
     * <br>
     * Absolute X, Absolute Y, Absolute Z, Absolute Yaw, Absolute Pitch
     * <br>
     * Only the fields with the corresponding bit set to true will be present in the serialized data.
     */
    private final BitSet flags;
    private final double x, y, z;
    private final float yaw, pitch;

    public InstructionPlayerPosition(ByteBuffer buf) {
        super(buf);

        var flags = new byte[1];
        buf.get(flags);
        this.flags = BitSet.valueOf(flags);

        x = this.flags.get(0) ? buf.getDouble() : 0;
        y = this.flags.get(1) ? buf.getDouble() : 0;
        z = this.flags.get(2) ? buf.getDouble() : 0;
        yaw = this.flags.get(3) ? buf.getFloat() : 0;
        pitch = this.flags.get(4) ? buf.getFloat() : 0;
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.PLAYER_POSITION;
    }

    public InstructionPlayerPosition(int trackerId, Double x, Double y, Double z, Float yaw, Float pitch) {
        super(trackerId);

        this.x = x == null ? 0 : x;
        this.y = y == null ? 0 : y;
        this.z = z == null ? 0 : z;
        this.yaw = yaw == null ? 0 : yaw;
        this.pitch = pitch == null ? 0 : pitch;

        flags = new BitSet(5);
        flags.set(0, x != null);
        flags.set(1, y != null);
        flags.set(2, z != null);
        flags.set(3, yaw != null);
        flags.set(4, pitch != null);
    }

    public InstructionPlayerPosition(Player player, int trackerId) {
        this(trackerId, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
    }

    public boolean IsComplete() {
        return flags.get(0) && flags.get(1) && flags.get(2) && flags.get(3) && flags.get(4);
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);

        out.write(CodecUtil.ToFixedLengthBytes(flags, 1));

        if (flags.get(0)) out.writeDouble(x);
        if (flags.get(1)) out.writeDouble(y);
        if (flags.get(2)) out.writeDouble(z);
        if (flags.get(3)) out.writeFloat(yaw);
        if (flags.get(4)) out.writeFloat(pitch);
        
    }

    @Override
    public void Apply(IPlayback playback) {
        var player = playback.GetTrackedPlayer(trackerId);
        var pos = player.GetLocation();
        if (flags.get(0)) pos.setX(x);
        if (flags.get(1)) pos.setY(y);
        if (flags.get(2)) pos.setZ(z);
        if (flags.get(3)) pos.setYaw(yaw);
        if (flags.get(4)) pos.setPitch(pitch);
        player.SetLocation(pos);
    }
}
