package moe.ku6.yukireplay.api.playback;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface IPlayback {
    World GetWorld();
    void StepPlayback();
    String GetMetadataAsString();
    void Close();
    void AddViewers(Player... players);
    void RemoveViewers(Player... players);
    <T extends IPlaybackEntity> T GetTracked(int trackerId);
    void AddTrackedEntity(IPlaybackEntity entity);
    void AddTrackedPlayer(IPlaybackPlayer player);
    void RemoveTrackedEntity(IPlaybackEntity entity);
    void RemoveTrackedPlayer(IPlaybackPlayer player);
    boolean IsPlaying();
    void SetPlaying(boolean playing);
    int GetPlayhead();
    int GetTotalFrames();
    Collection<Player> GetViewers();
}
