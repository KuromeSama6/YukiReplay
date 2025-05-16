package moe.ku6.yukireplay.codec.impl.player;

import lombok.ToString;
import moe.ku6.yukireplay.codec.PlayerInstruction;
import org.bukkit.entity.Player;

import java.nio.ByteBuffer;

@ToString
public class InstructionRemovePlayer extends PlayerInstruction {
    public InstructionRemovePlayer(ByteBuffer buf) {
        super(buf);
    }

    public InstructionRemovePlayer(Player player) {
        super(player);
    }
}
