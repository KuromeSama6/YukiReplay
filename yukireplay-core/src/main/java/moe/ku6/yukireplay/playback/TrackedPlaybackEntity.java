package moe.ku6.yukireplay.playback;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import moe.ku6.yukireplay.api.playback.EntityLifetime;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class TrackedPlaybackEntity implements IPlaybackEntity {
    protected final List<Player> viewers = new ArrayList<>();
    protected final ReplayPlayback playback;
    protected final int trackerId;
    @Getter
    protected EntityLifetime lifetime;

    @Getter
    protected boolean alive = false;

    public TrackedPlaybackEntity(ReplayPlayback playback, int trackerId) {
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
    public void SetLifetime(EntityLifetime lifetime) {
        Objects.requireNonNull(lifetime);
        this.lifetime = lifetime;
    }

    @Override
    public void Tick(int frame) {
        EnsureLifetime();
        boolean lifetimeAlive = lifetime.IsAlive(frame);

        if (lifetimeAlive != alive) {
            alive = lifetimeAlive;
            if (alive) {
                for (var viewer : playback.GetViewers()) {
                    SpawnOnClient(viewer);
                    viewers.add(viewer);
                }
            } else {
                for (var viewer : ImmutableList.copyOf(viewers)) {
                    DespawnOnClient(viewer);
                }
                viewers.clear();
            }
        }
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
    public List<Player> GetViewers() {
        return viewers;
    }

    protected final void EnsureLifetime() {
        Objects.requireNonNull(lifetime, "Lifetime must not be null for entity: " + this.getClass().getSimpleName());
    }
}
