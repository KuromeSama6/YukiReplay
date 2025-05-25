package moe.ku6.yukireplay.nms.impl;

import moe.ku6.yukireplay.api.nms.entity.IClientSplashPotion;
import moe.ku6.yukireplay.api.util.SimpleLocation;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;

public class ClientSplashPotion_v1_8_R3 extends AbstractClientEntity_v1_8_R3 implements IClientSplashPotion {
    private final World world;
    private final ItemStack potionItem;
    private final EntityPotion entityPotion;
    public ClientSplashPotion_v1_8_R3(World world, SimpleLocation pos, ItemStack potionItem) {
        super(world);
        this.world = world;
        this.potionItem = potionItem;
        entityPotion = new EntityPotion(((CraftWorld)world).getHandle(), pos.getX(), pos.getY(), pos.getZ(), CraftItemStack.asNMSCopy(potionItem));
    }

    @Override
    protected Entity GetEntity() {
        return entityPotion;
    }

    @Override
    protected void SpawnClientEntity(Player viewer) {
        SendViewerPacket(new PacketPlayOutSpawnEntity(entityPotion, 73));
        SendViewerPacket(new PacketPlayOutEntityMetadata(entityPotion.getId(), entityPotion.getDataWatcher(), true));
    }

    @Override
    public ItemStack GetPotionItem() {
        return potionItem;
    }

    @Override
    public int GetEntityId() {
        return entityPotion.getId();
    }
}
