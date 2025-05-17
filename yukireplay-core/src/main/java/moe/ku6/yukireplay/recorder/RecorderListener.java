package moe.ku6.yukireplay.recorder;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import moe.ku6.yukireplay.YukiReplay;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerArmSwing;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerChat;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

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

    }

    public void Close() {
        HandlerList.unregisterAll(this);
        PacketEvents.getAPI().getEventManager().unregisterListener(handle);
    }
}
