package moe.ku6.yukireplay.api.codec.impl.player;

import lombok.ToString;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * Contains status about the player's motion: sprinting, sneaking, etc.
 */
@ToString
public class InstructionPlayerMotionStatus extends PlayerInstruction {
    /**
     * Flag for the player's motion status.
     * 0: sneaking
     * 1: sprinting
     */
    private final BitSet flag;

    public InstructionPlayerMotionStatus(ByteBuffer buf) {
        super(buf);
        flag = new BitSet(buf.get());
    }

    public InstructionPlayerMotionStatus(int trackerId, boolean sneaking, boolean flying) {
        super(trackerId);
        flag = new BitSet(2);
        flag.set(0, sneaking);
        flag.set(1, flying);
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        out.write(flag.toByteArray());
    }

    @Override
    public void Apply(IPlayback playback) {

    }

    @Override
    public InstructionType GetType() {
        return InstructionType.PLAYER_MOTION_STATUS;
    }
}
