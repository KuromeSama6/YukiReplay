package moe.ku6.yukireplay.api.nms.entity;

import moe.ku6.yukireplay.api.nms.IClientEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

public interface IClientArmorStand extends IClientEntity {
    ArmorStand GetBukkitEntity();
}
