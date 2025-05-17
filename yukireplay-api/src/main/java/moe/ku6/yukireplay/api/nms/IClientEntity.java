package moe.ku6.yukireplay.api.nms;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IClientEntity {
    int GetEntityId();
    Location GetLocation();
    void SetLocation(Location location);
    void SpawnTo(Player viewer);
    void DespawnFrom(Player viewer);

    default void ForceRefresh(Player viewer) {
        DespawnFrom(viewer);
        SpawnTo(viewer);
    }
}
