package moe.ku6.yukireplay.nms.impl;

import moe.ku6.yukireplay.api.nms.IClientEntity;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractClientEntity_v1_8_R3 implements IClientEntity {
    protected final Set<Player> viewers = new HashSet<>();
    protected final World world;

    public AbstractClientEntity_v1_8_R3(World world) {
        this.world = world;
    }

    protected abstract Entity GetEntity();
    protected abstract void SpawnClientEntity(Player viewer);
    protected void DespawnClientEntity(Player viewer) {
        var packet = new PacketPlayOutEntityDestroy(GetEntityId());
        ((CraftPlayer)viewer).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    public int GetEntityId() {
        return GetEntity().getId();
    }

    @Override
    public Location GetLocation() {
        return GetEntity().getBukkitEntity().getLocation();
    }

    @Override
    public void SetLocation(Location location) {
        var entity = GetEntity();
        entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        var packet = new PacketPlayOutEntityTeleport(entity);
        SendViewerPacket(packet);
    }

    @Override
    public void SpawnTo(Player viewer) {
        if (viewers.contains(viewer)) {
            return;
        }
        viewers.add(viewer);
        SpawnClientEntity(viewer);
    }

    @Override
    public void DespawnFrom(Player viewer) {
        if (!viewers.contains(viewer)) {
            return;
        }
        viewers.remove(viewer);
        DespawnClientEntity(viewer);
    }

    protected void SendViewerPacket(Packet packet) {
        for (Player viewer : viewers) {
            ((CraftPlayer)viewer).getHandle().playerConnection.sendPacket(packet);
        }
    }
}
