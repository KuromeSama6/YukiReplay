package moe.ku6.yukireplay.playback.block;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ku6.yukireplay.api.codec.impl.block.InstructionBlockChange;
import org.bukkit.Location;
import org.bukkit.Material;

@AllArgsConstructor
@Getter
public class TrackedBlockChange {
    private final Location location;
    private final Material material;
    private final byte data;
}
