package moe.ku6.yukireplay.codec.impl.util;

import moe.ku6.yukireplay.codec.Instruction;
import moe.ku6.yukireplay.playback.ReplayPlayback;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class InstructionFrameEnd extends Instruction {
    public static final InstructionFrameEnd INSTANCE = new InstructionFrameEnd();

    private InstructionFrameEnd() {
        super(ByteBuffer.allocate(0));
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        throw new UnsupportedOperationException("Cannot serialize InstructionNewFrame");
    }

    @Override
    public void Apply(ReplayPlayback playback) {
        throw new UnsupportedOperationException("Cannot apply InstructionNewFrame");
    }

    @Override
    public String toString() {
        return "InstructionNewFrame()";
    }
}
