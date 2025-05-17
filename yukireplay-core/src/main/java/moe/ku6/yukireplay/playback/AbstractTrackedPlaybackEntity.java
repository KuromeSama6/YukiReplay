package moe.ku6.yukireplay.playback;

import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTrackedPlaybackEntity implements IPlaybackEntity {
    protected final List<Player> viewers = new ArrayList<>();
    protected final ReplayPlayback playback;
    protected final int trackerId;

    public AbstractTrackedPlaybackEntity(ReplayPlayback playback, int trackerId) {
        this.playback = playback;
        this.trackerId = trackerId;
    }

    @Override
    public IPlayback GetPlayback() {
        return playback;
    }

    @Override
    public int GetTrackerId() {
        return trackerId;
    }

    @Override
    public void SpawnFor(Player player) {
        if (viewers.contains(player)) {
            return;
        }
        viewers.add(player);
        SpawnOnClient(player);
    }

    @Override
    public void DespawnFor(Player player) {
        if (!viewers.contains(player)) {
            return;
        }
        viewers.remove(player);
        DespawnOnClient(player);
    }

    protected abstract void SpawnOnClient(Player viewer);
    protected abstract void DespawnOnClient(Player viewer);

    @Override
    public List<Player> Getviewers() {
        return viewers;
    }
}
