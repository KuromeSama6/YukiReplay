package moe.ku6.yukireplay.recorder;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import moe.ku6.yukireplay.YukiReplay;
import moe.ku6.yukireplay.api.codec.impl.block.InstructionBlockBreakProgress;
import moe.ku6.yukireplay.api.codec.impl.block.InstructionBlockChange;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerArmSwing;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerChat;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerDamage;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerDeath;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

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
