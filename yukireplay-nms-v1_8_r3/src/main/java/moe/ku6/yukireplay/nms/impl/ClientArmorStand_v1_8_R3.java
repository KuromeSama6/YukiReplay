package moe.ku6.yukireplay.nms.impl;

import moe.ku6.yukireplay.api.nms.entity.IClientArmorStand;
import moe.ku6.yukireplay.api.util.SimpleLocation;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ClientArmorStand_v1_8_R3 extends AbstractClientEntity_v1_8_R3 implements IClientArmorStand {
    private final World world;
    private final EntityArmorStand armorStand;
    private final SimpleLocation offset;

    public ClientArmorStand_v1_8_R3(World world, SimpleLocation pos, SimpleLocation offset) {
        super(world);
        this.world = world;
        this.offset = offset;

        armorStand = new EntityArmorStand(((CraftWorld)world).getHandle(), pos.getX() + offset.getX(), pos.getY() + offset.getY(), pos.getZ() + offset.getZ());
    }

    @Override
    protected Entity GetEntity() {
        return armorStand;
    }

    @Override
    protected void SpawnClientEntity(Player viewer) {
        SendViewerPacket(new PacketPlayOutSpawnEntity(armorStand, 78));
        SendViewerPacket(new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true));
    }

    @Override
    public void SetLocation(Location location) {
        var pos = location.clone();
//        System.out.println("po1: " + pos + "offset: " + offset);
        pos.add(offset.getX(), offset.getY(), offset.getZ());
//        System.out.println("po2: " + pos);
        super.SetLocation(pos);
    }

    @Override
    public int GetEntityId() {
        return armorStand.getId();
    }

    @Override
    public ArmorStand GetBukkitEntity() {
        return (ArmorStand)armorStand.getBukkitEntity();
    }
}
