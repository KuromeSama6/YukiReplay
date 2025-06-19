package moe.ku6.yukireplay.api.codec.impl.block;

import moe.ku6.yukireplay.api.codec.IBlockChange;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.BlockInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class InstructionBlockChange extends BlockInstruction implements IBlockChange {
    private final Material oldMaterial, newMaterial;
    private final byte oldData, newData;

    public InstructionBlockChange(ByteBuffer buf) {
        super(buf);
        oldMaterial = Material.getMaterial(buf.getInt());
        oldData = buf.get();
        newMaterial = Material.getMaterial(buf.getInt());
        newData = buf.get();
    }

    public InstructionBlockChange(Block block, Material oldMaterial, byte oldData) {
        super(block.getLocation());
        newMaterial = block.getType();
        newData = block.getData();
        this.oldMaterial = oldMaterial;
        this.oldData = oldData;
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.BLOCK_CHANGE;
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        out.writeInt(oldMaterial.getId());
        out.writeByte(oldData);
        out.writeInt(newMaterial.getId());
        out.writeByte(newData);
    }

    @Override
    public void Apply(IPlayback playback) {
        var pos = new Location(playback.GetWorld(), x, y, z);
        playback.GetViewers().forEach(c -> c.sendBlockChange(pos, newMaterial, newData));
    }

    @Override
    public void Rewind(IPlayback playback) {
        var pos = new Location(playback.GetWorld(), x, y, z);
        playback.GetViewers().forEach(c -> c.sendBlockChange(pos, oldMaterial, oldData));
    }
}
