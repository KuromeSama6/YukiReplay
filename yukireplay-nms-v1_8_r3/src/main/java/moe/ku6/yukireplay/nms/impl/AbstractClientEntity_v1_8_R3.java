package moe.ku6.yukireplay.nms.impl;

import moe.ku6.yukireplay.api.nms.IClientEntity;
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

    protected abstract void SpawnClientEntity(Player viewer);
    protected abstract void DespawnClientEntity(Player viewer);

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
        var handle = ((CraftPlayer)viewer).getHandle();
    }
}
