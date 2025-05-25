package moe.ku6.yukireplay.api.codec.impl;

import lombok.Getter;
import moe.ku6.yukireplay.api.codec.Instruction;
import moe.ku6.yukireplay.api.playback.IPlayback;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class EntityInstruction extends Instruction {
    @Getter
    protected final int trackerId;
    public EntityInstruction(ByteBuffer buf) {
        super(buf);
        trackerId = buf.getInt();
    }

    public EntityInstruction(int trackerId) {
        super();
        this.trackerId = trackerId;
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        out.writeInt(trackerId);
    }

    @Override
    public String toString() {
        return "PlayerInstruction(id=%s, ???)".formatted(trackerId);
    }
}
