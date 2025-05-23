package moe.ku6.yukireplay.api.util;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;

@UtilityClass
public class ItemUtil {
    public static boolean ItemsEqual(ItemStack a, ItemStack b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.isSimilar(b);
    }
}
