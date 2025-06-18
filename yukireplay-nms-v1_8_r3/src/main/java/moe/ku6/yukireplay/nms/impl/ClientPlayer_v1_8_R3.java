package moe.ku6.yukireplay.nms.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import moe.ku6.yukireplay.api.YukiReplayAPI;
import moe.ku6.yukireplay.api.nms.GameProfilePropertyWrapper;
import moe.ku6.yukireplay.api.nms.IClientPlayer;
import moe.ku6.yukireplay.api.nms.IGameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClientPlayer_v1_8_R3 extends AbstractClientEntity_v1_8_R3 implements IClientPlayer {
    private final PlayerInteractManager interactManager;
    private final IGameProfile gameProfile;
    private final EntityPlayer player;

    public ClientPlayer_v1_8_R3(World world, IGameProfile gameProfile) {
        super(world);
        var server = ((CraftServer)Bukkit.getServer()).getServer();
        var nmsWorld = ((CraftWorld)world).getHandle();
        this.gameProfile = gameProfile;
        interactManager = new PlayerInteractManager(nmsWorld);

        var profile = new GameProfile(gameProfile.getUuid(), gameProfile.getName());
        for (var prop : gameProfile.GetProperties().keySet()) {
            var property = gameProfile.GetProperty(prop);
            profile.getProperties().put(prop, new Property(property.getName(), property.getValue(), property.getSignature()));
        }

        player = new EntityPlayer(server, nmsWorld, profile, interactManager);
    }

    @Override
    protected Entity GetEntity() {
        return player;
    }

    @Override
    protected void SpawnClientEntity(Player viewer) {
        var handle = ((CraftPlayer)viewer).getHandle();

        var playerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, player);
        handle.playerConnection.sendPacket(playerInfo);

        var spawnPacket = new PacketPlayOutNamedEntitySpawn(player);
        handle.playerConnection.sendPacket(spawnPacket);
    }

    @Override
    protected void DespawnClientEntity(Player viewer) {
        var handle = ((CraftPlayer)viewer).getHandle();
        var playerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, player);
        handle.playerConnection.sendPacket(playerInfo);
        var despawnPacket = new PacketPlayOutEntityDestroy(player.getId());
        handle.playerConnection.sendPacket(despawnPacket);
    }

    @Override
    public int GetEntityId() {
        return player.getId();
    }

    @Override
    public Location GetLocation() {
        return player.getBukkitEntity().getLocation();
    }

    @Override
    public void SetLocation(Location location) {
        player.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        var teleportPacket = new PacketPlayOutEntityTeleport(player);
        for (var viewer : viewers) {
            var handle = ((CraftPlayer)viewer).getHandle();
            handle.playerConnection.sendPacket(teleportPacket);
        }

        var headRotationPacket = new PacketPlayOutEntityHeadRotation(player, (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
        for (var viewer : viewers) {
            var handle = ((CraftPlayer)viewer).getHandle();
            handle.playerConnection.sendPacket(headRotationPacket);
        }
    }

    @Override
    public IGameProfile GetGameProfile() {
        return gameProfile;
    }

    @Override
    public void SetSkin(String value, String signature) {
        var property = new GameProfilePropertyWrapper("textures", value, signature);
        gameProfile.SetProperty("textures", property);
    }

    @Override
    public void SetSneaking(boolean sneaking) {
        var watcher = new DataWatcher(null);
        watcher.a(0, (byte) (sneaking ? 0x02 : 0x00));
        var packet = new PacketPlayOutEntityMetadata(player.getId(), watcher, true);
        for (var viewer : viewers) {
            var handle = ((CraftPlayer)viewer).getHandle();
            handle.playerConnection.sendPacket(packet);
        }
    }

    @Override
    public void SetHealth(float health) {
        player.setHealth(health);
    }

    @Override
    public int PlayDeathAnimation() {// fake death packet
        var pos = GetLocation();
        var profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().putAll("textures", player.getProfile().getProperties().get("textures"));
        var world = player.world.getWorld().getHandle();
        var entityPlayer = new EntityPlayer(((CraftServer)Bukkit.getServer()).getServer(), world, profile, new PlayerInteractManager(world));
        entityPlayer.setLocation(pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());
        var playerListPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer);

        SendViewerPacket(playerListPacket);
        SendViewerPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        SendViewerPacket(new PacketPlayOutEntityMetadata());


        Bukkit.getScheduler().scheduleSyncDelayedTask(YukiReplayAPI.Get().GetProvidingPlugin(), () -> {
            SendViewerPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer));
            SendViewerPacket(new PacketPlayOutEntityDestroy(entityPlayer.getId()));
        }, 20);
        return entityPlayer.getId();
    }

}
