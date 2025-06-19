package moe.ku6.yukireplay.api.codec.impl;

import moe.ku6.yukireplay.api.codec.Instruction;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.util.Vec3i;
import org.bukkit.Location;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class BlockInstruction extends Instruction {
    protected final int x, y, z;

    public BlockInstruction(ByteBuffer buf) {
        super();
        this.x = buf.getInt();
        this.y = buf.getInt();
        this.z = buf.getInt();
    }

    public BlockInstruction(Location pos) {
        super();
        x = pos.getBlockX();
        y = pos.getBlockY();
        z = pos.getBlockZ();
    }

    public Vec3i GetLocation() {
        return new Vec3i(x, y, z);
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
    }
}
