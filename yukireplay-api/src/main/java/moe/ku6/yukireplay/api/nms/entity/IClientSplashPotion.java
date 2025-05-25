package moe.ku6.yukireplay.api.nms.entity;

import moe.ku6.yukireplay.api.nms.IClientEntity;
import org.bukkit.inventory.ItemStack;

public interface IClientSplashPotion extends IClientEntity {
    ItemStack GetPotionItem();
}
