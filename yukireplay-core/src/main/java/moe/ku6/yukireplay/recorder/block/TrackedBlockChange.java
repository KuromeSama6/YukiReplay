package moe.ku6.yukireplay.recorder.block;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ku6.yukireplay.api.codec.impl.block.InstructionBlockChange;
import moe.ku6.yukireplay.api.util.Vec2i;
import moe.ku6.yukireplay.api.util.Vec3i;
import org.bukkit.Location;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public class TrackedBlockChange {
    private final Vec3i location;
    private final Material material;
    private final byte data;
}
