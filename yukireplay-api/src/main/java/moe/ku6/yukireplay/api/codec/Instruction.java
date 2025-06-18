package moe.ku6.yukireplay.api.codec;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import moe.ku6.yukireplay.api.playback.IPlayback;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Instruction {
    public Instruction(ByteBuffer buf) {
    }

    public abstract InstructionType GetType();
    public abstract void Serialize(DataOutputStream out) throws IOException;
    public abstract void Apply(IPlayback playback);
    public void Rewind(IPlayback playback) {
        Apply(playback); // implementation is optional, default is to reapply the instruction
    }

    @Override
    public String toString() {
        return "Instruction(???)";
    }
}
