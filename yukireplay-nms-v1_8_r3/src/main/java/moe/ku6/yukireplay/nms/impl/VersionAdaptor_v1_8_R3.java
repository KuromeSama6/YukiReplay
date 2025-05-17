package moe.ku6.yukireplay.nms.impl;

import moe.ku6.yukireplay.api.nms.IClientPlayer;
import moe.ku6.yukireplay.api.nms.IGameProfile;
import moe.ku6.yukireplay.api.nms.IVersionAdaptor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VersionAdaptor_v1_8_R3 implements IVersionAdaptor {
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
}
