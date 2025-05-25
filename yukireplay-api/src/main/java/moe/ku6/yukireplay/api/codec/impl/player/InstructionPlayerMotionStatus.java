package moe.ku6.yukireplay.api.codec.impl.player;

import lombok.ToString;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import moe.ku6.yukireplay.api.util.CodecUtil;

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
//        System.out.println("buffer len: " + buf.remaining());
        var flags = new byte[1];
        buf.get(flags);
        flag = BitSet.valueOf(flags);
    }

    public InstructionPlayerMotionStatus(int trackerId, boolean sneaking, boolean sprinting) {
        super(trackerId);
        flag = new BitSet(2);
        flag.set(0, sneaking);
        flag.set(1, sprinting);
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        out.write(CodecUtil.ToFixedLengthBytes(flag, 1));
    }

    @Override
    public void Apply(IPlayback playback) {
        IPlaybackPlayer player = playback.GetTracked(trackerId);
        player.GetClientPlayer().SetSneaking(flag.get(0));
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.PLAYER_MOTION_STATUS;
    }
}
