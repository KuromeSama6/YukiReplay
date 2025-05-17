package moe.ku6.yukireplay.api.codec.impl.player;

import lombok.ToString;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.util.CodecUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@ToString
public class InstructionPlayerChat extends PlayerInstruction {
    private final String message;

    public InstructionPlayerChat(ByteBuffer buf) {
        super(buf);
        message = CodecUtil.ReadLengthPrefixed(buf);
    }

    public InstructionPlayerChat(int playerId, String message) {
        super(playerId);
        this.message = message;
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        CodecUtil.WriteLengthPrefixed(out, message);
    }

    @Override
    public void Apply(IPlayback playback) {

    }

    @Override
    public InstructionType GetType() {
        return InstructionType.PLAYER_CHAT;
    }
}
