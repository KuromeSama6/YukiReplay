package moe.ku6.yukireplay.api.codec.impl.entity;

import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.EntityInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;

import java.nio.ByteBuffer;

public class InstructionEntityDespawn extends EntityInstruction {
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
        var tracked = playback.GetTracked(trackerId);
        if (tracked != null) {
            playback.GetViewers().forEach(tracked::DespawnFor);
            playback.RemoveTrackedEntity(tracked);
        }
    }
}
