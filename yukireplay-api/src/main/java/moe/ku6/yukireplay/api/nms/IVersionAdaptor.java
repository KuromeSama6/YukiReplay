package moe.ku6.yukireplay.api.nms;

import moe.ku6.yukireplay.api.nms.entity.IClientArmorStand;
import moe.ku6.yukireplay.api.util.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface IVersionAdaptor {
    String SerializeItemStack(ItemStack item);
    ItemStack DeserializeItemStack(String item);
    IGameProfile CreateGameProfile(UUID uuid, String name);
    IGameProfile GetGameProfile(Player player);
    IClientPlayer CreateClientPlayer(World world, IGameProfile gameProfile);
    IClientArmorStand CreateClientArmorStand(World world, SimpleLocation pos, SimpleLocation offset);
    void PlayPotionSplashEffect(Player viewer, Location pos, ItemStack potion);

    static IVersionAdaptor Get() {
        var versionStr = GetNMSVersion();

        try {
            var ret = (IVersionAdaptor)Class.forName("moe.ku6.yukireplay.nms.impl.VersionAdaptor_" + versionStr)
                .getConstructor()
                .newInstance();
            return ret;

        } catch (Exception e) {
            throw new UnsupportedOperationException("An error occured while acquiring a version adaptor. The current server version (%s) may not be supported.".formatted(versionStr));
        }
    }

    static String GetNMSVersion() {
        String v = Bukkit.getServer().getClass().getPackage().getName();
        return v.substring(v.lastIndexOf('.') + 1);
    }
}
