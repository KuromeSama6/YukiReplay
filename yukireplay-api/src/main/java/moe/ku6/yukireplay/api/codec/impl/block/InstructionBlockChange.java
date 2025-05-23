package moe.ku6.yukireplay.api.codec.impl.block;

import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.BlockInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import org.bukkit.Location;
import org.bukkit.Material;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class InstructionBlockChange extends BlockInstruction {
    private final Material material;
    private final byte blockData;

    public InstructionBlockChange(ByteBuffer buf) {
        super(buf);
        material = Material.getMaterial(buf.getInt());
        blockData = buf.get();
    }

    public InstructionBlockChange(Location pos) {
        super(pos);
        var block = pos.getBlock();
        material = block.getType();
        blockData = block.getData();
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.BLOCK_CHANGE;
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        out.writeInt(material.getId());
        out.writeByte(blockData);
    }

    @Override
    public void Apply(IPlayback playback) {
        var block = playback.GetWorld().getBlockAt(x, y, z);
        block.setType(material);
        block.setData(blockData);
    }
}
