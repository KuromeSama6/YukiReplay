package moe.ku6.yukireplay.api.codec.impl.entity;

import lombok.Getter;
import moe.ku6.yukireplay.api.codec.IEntityLifetimeStart;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.EntityInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.util.SimpleLocation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.util.Vector;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Getter
public abstract class InstructionProjectileSpawn extends EntityInstruction implements IEntityLifetimeStart {
    protected final int launcherId;
    protected final SimpleLocation location;
    protected final Vector velocity;

    public InstructionProjectileSpawn(ByteBuffer buf) {
        super(buf);
        launcherId = buf.getInt();
        location = new SimpleLocation(buf);
        velocity = new Vector(buf.getDouble(), buf.getDouble(), buf.getDouble());
    }

    public InstructionProjectileSpawn(int trackerId, int launcherId, Projectile projectile) {
        super(trackerId);
        this.launcherId = launcherId;
        location = new SimpleLocation(projectile.getLocation());
        velocity = projectile.getVelocity();
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        out.writeInt(launcherId);

        location.Serialize(out);

        out.writeDouble(velocity.getX());
        out.writeDouble(velocity.getY());
        out.writeDouble(velocity.getZ());
    }
}
