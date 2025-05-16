package moe.ku6.yukireplay.codec;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import moe.ku6.yukireplay.playback.ReplayPlayback;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Instruction {
    public Instruction(ByteBuffer buf) {
    }

    public abstract void Serialize(DataOutputStream out) throws IOException;

    public abstract void Apply(ReplayPlayback playback);

    @Override
    public String toString() {
        return "Instruction(???)";
    }
}
