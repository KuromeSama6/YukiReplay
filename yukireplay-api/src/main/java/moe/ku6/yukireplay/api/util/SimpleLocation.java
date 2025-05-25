package moe.ku6.yukireplay.api.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;

@AllArgsConstructor
@Getter
public class SimpleLocation {
    private final double x, y, z;
    private final float yaw, pitch;

    public SimpleLocation(ByteBuffer buf) {
        x = buf.getDouble();
        y = buf.getDouble();
        z = buf.getDouble();
        yaw = buf.getFloat();
        pitch = buf.getFloat();
    }

    public SimpleLocation(Location location) {
        this(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public Location ToBukkitLocation(World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public void Serialize(DataOutputStream out) {
        try {
            out.writeDouble(x);
            out.writeDouble(y);
            out.writeDouble(z);
            out.writeFloat(yaw);
            out.writeFloat(pitch);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
