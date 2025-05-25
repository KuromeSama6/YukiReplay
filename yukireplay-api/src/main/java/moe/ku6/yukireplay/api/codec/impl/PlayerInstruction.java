package moe.ku6.yukireplay.api.codec.impl;

import java.nio.ByteBuffer;

public abstract class PlayerInstruction extends EntityInstruction {
    public PlayerInstruction(ByteBuffer buf) {
        super(buf);
    }

    public PlayerInstruction(int trackerId) {
        super(trackerId);
    }
}
