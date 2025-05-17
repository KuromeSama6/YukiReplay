package moe.ku6.yukireplay.api.codec.impl.util;

import moe.ku6.yukireplay.api.codec.Instruction;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.playback.IPlayback;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class InstructionFrameEnd extends Instruction {
    public static final InstructionFrameEnd INSTANCE = new InstructionFrameEnd();

    private InstructionFrameEnd() {
        super(ByteBuffer.allocate(0));
    }

    @Override
    public InstructionType GetType() {
        throw new UnsupportedOperationException("Cannot get type of InstructionNewFrame");
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        throw new UnsupportedOperationException("Cannot serialize InstructionNewFrame");
    }

    @Override
    public void Apply(IPlayback playback) {
        throw new UnsupportedOperationException("Cannot apply InstructionNewFrame");
    }

    @Override
    public String toString() {
        return "InstructionNewFrame()";
    }
}
