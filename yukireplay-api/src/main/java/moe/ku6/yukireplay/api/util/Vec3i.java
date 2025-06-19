package moe.ku6.yukireplay.api.util;

import com.github.retrooper.packetevents.util.Vector3i;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Vec3i {
    private final int x, y, z;

    public Vec3i(Location location) {
        this(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Vec3i(Vector3i vec) {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    public Location ToLocation(World world) {
        return new Location(world, x, y, z);
    }
}
