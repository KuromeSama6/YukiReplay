package moe.ku6.yukireplay.recorder;

import moe.ku6.yukireplay.YukiReplay;
import moe.ku6.yukireplay.api.codec.impl.block.InstructionBlockChange;
import moe.ku6.yukireplay.api.util.Vec3i;
import moe.ku6.yukireplay.recorder.block.TrackedBlockChange;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.Map;

public class BlockChangeWatcher implements Listener {
    private final ReplayRecorder recorder;
    private final Map<Vec3i, TrackedBlockChange> tracked = new HashMap<>();

    public BlockChangeWatcher(ReplayRecorder recorder) {
        this.recorder = recorder;
        Bukkit.getPluginManager().registerEvents(this, YukiReplay.getInstance());
    }


    @EventHandler(priority = EventPriority.MONITOR)
    private void BlockPlaceEvent(BlockPlaceEvent e) {
        var pos = e.getBlockPlaced().getLocation();
        var block = e.getBlock();
        ScheduleBlockChange(block, block.getType(), block.getData());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void OnBlockBreak(BlockBreakEvent e) {
        var pos = e.getBlock().getLocation();
        var block = e.getBlock();
        ScheduleBlockChange(block, block.getType(), block.getData());
    }

    private void ScheduleBlockChange(Block block, Material originalType, byte origialData) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(YukiReplay.getInstance(), () -> {
//            if (block.getType() == originalType && block.getData() == origialData) return;
            recorder.ScheduleInstruction(new InstructionBlockChange(block, originalType, origialData));

            var pos = new Vec3i(block.getLocation());
            tracked.put(pos, new TrackedBlockChange(pos, block.getType(), block.getData()));
        }, 1);
    }

    public void CheckTrackedChanges() {
        for (var pos : tracked.keySet()) {
            var entry = tracked.get(pos);
            var block = pos.ToLocation(recorder.getWorld()).getBlock();

            if (block.getType() != entry.getMaterial() || block.getData() != entry.getData()) {
                recorder.ScheduleInstruction(new InstructionBlockChange(block, entry.getMaterial(), entry.getData()));

                tracked.put(pos, new TrackedBlockChange(pos, block.getType(), block.getData()));
            }
        }
    }

    public void Close() {
        HandlerList.unregisterAll(this);
    }
}
