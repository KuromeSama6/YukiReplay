package moe.ku6.yukireplay.recorder;

import moe.ku6.yukireplay.YukiReplay;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class BlockChangeWatcher implements Listener {
    private final ReplayRecorder recorder;

    public BlockChangeWatcher(ReplayRecorder recorder) {
        this.recorder = recorder;
        Bukkit.getPluginManager().registerEvents(this, YukiReplay.getInstance());
    }

    public void Close() {
        HandlerList.unregisterAll(this);
    }
}
