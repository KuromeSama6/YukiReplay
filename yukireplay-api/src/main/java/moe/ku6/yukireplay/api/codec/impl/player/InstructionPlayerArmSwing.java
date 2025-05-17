package moe.ku6.yukireplay.api.codec.impl.player;

import lombok.ToString;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;

@ToString
public class InstructionPlayerArmSwing extends PlayerInstruction {
    public InstructionPlayerArmSwing(ByteBuffer buf) {
        super(buf);
    }

    public InstructionPlayerArmSwing(int trackerId) {
        super(trackerId);
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.PLAYER_ARM_SWING;
    }

    @Override
    public void Apply(IPlayback playback) {

    }
}
