package moe.ku6.yukireplay.nms.impl;

import moe.ku6.yukireplay.api.nms.IClientPlayer;
import moe.ku6.yukireplay.api.nms.IGameProfile;
import moe.ku6.yukireplay.api.nms.IVersionAdaptor;
import moe.ku6.yukireplay.api.nms.entity.IClientSplashPotion;
import moe.ku6.yukireplay.api.util.SimpleLocation;
import net.minecraft.server.v1_8_R3.BlockState;
import net.minecraft.server.v1_8_R3.MojangsonParseException;
import net.minecraft.server.v1_8_R3.MojangsonParser;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class VersionAdaptor_v1_8_R3 implements IVersionAdaptor {
    @Override
    public String SerializeItemStack(ItemStack item) {
        if (item == null) {
            return null;
        }
        var ret = CraftItemStack.asNMSCopy(item);
        if (ret == null) return null;
        return ret.save(new NBTTagCompound()).toString();
    }

    @Override
    public ItemStack DeserializeItemStack(String item) {
        if (item == null || item.isEmpty()) {
            return null;
        }



        try {
            return CraftItemStack.asBukkitCopy(net.minecraft.server.v1_8_R3.ItemStack.createStack(MojangsonParser.parse(item)));
        } catch (MojangsonParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IGameProfile CreateGameProfile(UUID uuid, String name) {
        return new GameProfile_v1_8_R3(uuid, name);
    }

    @Override
    public IGameProfile GetGameProfile(Player player) {
        var handle = ((CraftPlayer)player).getHandle();
        return new GameProfile_v1_8_R3(handle.getProfile());
    }

    @Override
    public IClientPlayer CreateClientPlayer(World world, IGameProfile gameProfile) {
        return new ClientPlayer_v1_8_R3(world, gameProfile);
    }

    @Override
    public IClientSplashPotion CreateClientSplashPotion(World world, SimpleLocation pos, ItemStack potionItem) {
        return new ClientSplashPotion_v1_8_R3(world, pos, potionItem);
    }
}
