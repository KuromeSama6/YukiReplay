package moe.ku6.yukireplay.recorder;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import moe.ku6.yukireplay.YukiReplay;
import moe.ku6.yukireplay.api.codec.impl.block.InstructionBlockChange;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionEntityDespawn;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionItemProjectileSpawn;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionPotionSplash;
import moe.ku6.yukireplay.api.codec.impl.player.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class RecorderListener implements Listener, PacketListener {
    private final ReplayRecorder recorder;
    private final PacketListenerCommon handle;

    public RecorderListener(ReplayRecorder recorder) {
        this.recorder = recorder;
        Bukkit.getPluginManager().registerEvents(this, YukiReplay.getInstance());
        handle = PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.MONITOR);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void OnMove(PlayerMoveEvent e) {
        var to = e.getTo();
        var tracked = recorder.GetTrackedPlayer(e.getPlayer());
        if (tracked == null) return;
        tracked.UpdatePosition(to);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void OnChat(AsyncPlayerChatEvent e) {
        var tracked = recorder.GetTrackedPlayer(e.getPlayer());
        if (tracked == null) return;
        recorder.ScheduleInstruction(new InstructionPlayerChat(tracked.getTrackerId(), e.getMessage()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        var tracked = recorder.GetTrackedPlayer(player);
        if (tracked == null) return;
        recorder.ScheduleInstruction(new InstructionPlayerDamage(tracked.getTrackerId()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnDeath(PlayerDeathEvent e) {
        var tracked = recorder.GetTrackedPlayer(e.getEntity());
        if (tracked == null) return;
        recorder.ScheduleInstruction(new InstructionPlayerDeath(tracked.getTrackerId(), true));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnRespawn(PlayerRespawnEvent e) {
        var tracked = recorder.GetTrackedPlayer(e.getPlayer());
        if (tracked == null) return;
        recorder.ScheduleInstruction(new InstructionPlayerDeath(tracked.getTrackerId(), false));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void BlockPlaceEvent(BlockPlaceEvent e) {
        var pos = e.getBlockPlaced().getLocation();
        Bukkit.getScheduler().scheduleSyncDelayedTask(YukiReplay.getInstance(), () -> recorder.ScheduleInstruction(new InstructionBlockChange(pos)), 1);

        if (!e.isCancelled()) {
            var tracked = recorder.GetTrackedPlayer(e.getPlayer());
            if (tracked == null) return;
            recorder.ScheduleInstruction(new InstructionPlayerArmSwing(tracked.getTrackerId()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void OnBlockBreak(BlockBreakEvent e) {
        var pos = e.getBlock().getLocation();
        Bukkit.getScheduler().scheduleSyncDelayedTask(YukiReplay.getInstance(), () -> recorder.ScheduleInstruction(new InstructionBlockChange(pos)), 1);

        if (!e.isCancelled()) {
            var tracked = recorder.GetTrackedPlayer(e.getPlayer());
            if (tracked == null) return;
            recorder.ScheduleInstruction(new InstructionPlayerArmSwing(tracked.getTrackerId()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void OnBlockDamage(BlockDamageEvent e) {
        var pos = e.getBlock().getLocation();
        var tracked = recorder.GetTrackedPlayer(e.getPlayer());
        if (tracked == null) return;
//        recorder.ScheduleInstruction(new InstructionBlockBreakProgress(pos, tracked.getTrackerId(), e.getBlock().get));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void OnItemHeld(PlayerItemHeldEvent e) {
        var tracked = recorder.GetTrackedPlayer(e.getPlayer());
        if (tracked == null) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(YukiReplay.getInstance(), tracked::SaveInventory, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnProjectileLaunch(ProjectileLaunchEvent e) {
        var shooter = e.getEntity().getShooter();
        if (!(shooter instanceof Player shooterPlayer)) return;
        var tracked = recorder.GetTrackedPlayer(shooterPlayer);
        if (tracked == null) return;

        var projectile = e.getEntity();

        if (projectile instanceof ThrownPotion thrownPotion) {
            var trackedEntity = recorder.AddTrackedEntity(projectile);
            recorder.ScheduleInstruction(new InstructionItemProjectileSpawn(trackedEntity.getTrackerId(), tracked.getTrackerId(), thrownPotion, thrownPotion.getItem()));
            trackedEntity.UpdatePosition();
        }

        if (projectile instanceof EnderPearl pearl) {
            var trackedEntity = recorder.AddTrackedEntity(projectile);
            recorder.ScheduleInstruction(new InstructionItemProjectileSpawn(trackedEntity.getTrackerId(), tracked.getTrackerId(), pearl, new ItemStack(Material.ENDER_PEARL)));
            trackedEntity.UpdatePosition();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnPotionSplash(PotionSplashEvent e) {
        var potion = e.getPotion();
        var tracked = recorder.GetTrackedEntity(potion);
        if (tracked == null) return;

        recorder.ScheduleInstruction(new InstructionPotionSplash(tracked.getTrackerId(), potion));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void OnProjectileHit(ProjectileHitEvent e) {
        var entity = e.getEntity();
        var tracked = recorder.GetTrackedEntity(entity);
        if (tracked == null) return;

        if (entity instanceof EnderPearl enderPearl && enderPearl.getShooter() instanceof Player shooter) {
            var trackedThrower = recorder.GetTrackedPlayer(shooter);
            if (trackedThrower != null) {
                recorder.ScheduleInstruction(new InstructionPearlTeleport(trackedThrower.getTrackerId(), tracked.getTrackerId(), enderPearl));
            }
        }

        recorder.ScheduleInstruction(new InstructionEntityDespawn(tracked.getTrackerId()));
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        var type = event.getPacketType();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.isCancelled()) return;
        var tracked = recorder.GetTrackedPlayer(event.getPlayer());
        if (tracked == null) return;

        var type = event.getPacketType();

        if (type == PacketType.Play.Client.ENTITY_ACTION) {
            var packet = new WrapperPlayClientEntityAction(event);
            switch (packet.getAction()) {
                case START_SNEAKING -> {
                    tracked.SetSprintSneak(null, true);
                }
                case STOP_SNEAKING -> {
                    tracked.SetSprintSneak(null, false);
                }
                case START_SPRINTING -> {
                    tracked.SetSprintSneak(true, null);
                }
                case STOP_SPRINTING -> {
                    tracked.SetSprintSneak(false, null);
                }
            }
        }

        if (type == PacketType.Play.Client.ANIMATION) {
            var packet = new WrapperPlayClientAnimation(event);
            if (packet.getHand() == InteractionHand.MAIN_HAND) {
                recorder.ScheduleInstruction(new InstructionPlayerArmSwing(tracked.getTrackerId()));
            }
        }

        if (type == PacketType.Play.Client.PLAYER_DIGGING) {
            var packet = new WrapperPlayClientPlayerDigging(event);
            switch (packet.getAction()) {
                case START_DIGGING -> {
                    tracked.SetDigging(true);
                }
                case CANCELLED_DIGGING, FINISHED_DIGGING -> {
                    tracked.SetDigging(false);
                }
            }
        }

    }

    public void Close() {
        HandlerList.unregisterAll(this);
        PacketEvents.getAPI().getEventManager().unregisterListener(handle);
    }
}
