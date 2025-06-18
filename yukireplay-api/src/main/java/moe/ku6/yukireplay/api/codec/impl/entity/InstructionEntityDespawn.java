package moe.ku6.yukireplay.api.codec.impl.entity;

import moe.ku6.yukireplay.api.codec.IEntityLifetimeEnd;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.EntityInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;

import java.nio.ByteBuffer;

public class InstructionEntityDespawn extends EntityInstruction implements IEntityLifetimeEnd {
    public InstructionEntityDespawn(ByteBuffer buf) {
        super(buf);
    }

    public InstructionEntityDespawn(int trackerId) {
        super(trackerId);
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.ENTITY_DESPAWN;
    }

    @Override
    public void Apply(IPlayback playback) {
    }

    @Override
    public int GetTrackerId() {
        return trackerId;
    }
}
