package moe.ku6.yukireplay.playback.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import moe.ku6.yukireplay.api.YukiReplayAPI;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionItemProjectileSpawn;
import moe.ku6.yukireplay.api.nms.entity.IClientArmorStand;
import moe.ku6.yukireplay.api.playback.EntityLifetime;
import moe.ku6.yukireplay.api.playback.IPlaybackItemProjectile;
import moe.ku6.yukireplay.api.util.SimpleLocation;
import moe.ku6.yukireplay.playback.ReplayPlayback;
import moe.ku6.yukireplay.playback.TrackedPlaybackEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.List;

public class TrackedPlaybackItemProjectile extends TrackedPlaybackEntity implements IPlaybackItemProjectile {
    private final ItemStack item;
    private final IClientArmorStand armorStand;
    public TrackedPlaybackItemProjectile(ReplayPlayback playback, InstructionItemProjectileSpawn instruction) {
        super(playback, instruction.getTrackerId());
        item = instruction.getItem();

        armorStand = YukiReplayAPI.Get().GetVersionAdaptor().CreateClientArmorStand(playback.GetWorld(), instruction.getLocation(),new SimpleLocation(0, -0.5, 0));

        var bukkitArmorStand = armorStand.GetBukkitEntity();
        bukkitArmorStand.setArms(true);
        bukkitArmorStand.setMarker(true);
        bukkitArmorStand.setSmall(true);
        bukkitArmorStand.setBasePlate(false);
        bukkitArmorStand.setGravity(false);
        bukkitArmorStand.setVisible(false);

        bukkitArmorStand.setRightArmPose(new EulerAngle(0, 90, 90));
//        armorStand.GetBukkitEntity().setVisible(false);

    }

    @Override
    protected void SpawnOnClient(Player viewer) {
        armorStand.SpawnTo(viewer);

        var packet = new WrapperPlayServerEntityEquipment(armorStand.GetEntityId(), List.of(
            new Equipment(EquipmentSlot.MAIN_HAND, SpigotConversionUtil.fromBukkitItemStack(item))
        ));
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }

    @Override
    protected void DespawnOnClient(Player viewer) {
        armorStand.DespawnFrom(viewer);
    }

    @Override
    public Location GetLocation() {
        return armorStand.GetLocation();
    }

    @Override
    public void SetLocation(Location location) {
        armorStand.SetLocation(location);
    }

    @Override
    public ItemStack GetItem() {
        return item;
    }
}
