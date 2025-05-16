package moe.ku6.yukireplay.codec.impl.player;

import lombok.ToString;
import moe.ku6.yukireplay.codec.PlayerInstruction;
import moe.ku6.yukireplay.util.CodecUtil;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@ToString
public class InstructionAddPlayer extends PlayerInstruction {
    private final String name;

    public InstructionAddPlayer(ByteBuffer buf) {
        super(buf);
        name = CodecUtil.ReadLengthPrefixed(buf);
    }

    public InstructionAddPlayer(Player player) {
        super(player);
        name = player.getName();
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        CodecUtil.WriteLengthPrefixed(out, name);
    }
}
