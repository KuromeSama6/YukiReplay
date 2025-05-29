package moe.ku6.yukireplay.api.playback;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public interface IPlaybackEntity {
    IPlayback GetPlayback();
    int GetTrackerId();
    Location GetLocation();
    void SetLocation(Location location);
    void SpawnFor(Player player);
    void DespawnFor(Player player);
    List<Player> GetViewers();

    default void Remove() {
        for (Player player : new ArrayList<>(GetViewers())) {
            DespawnFor(player);
        }
    }
}
