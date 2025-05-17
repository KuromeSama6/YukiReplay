package moe.ku6.yukireplay.api.playback;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface IPlayback {
    void StepPlayback();
    String GetMetadataAsString();
    void Close();
    void AddViewers(Player... players);
    void RemoveViewers(Player... players);
    IPlaybackPlayer GetTrackedPlayer(int trackerId);
    void AddTrackedPlayer(IPlaybackPlayer player);
    void RemoveTrackedPlayer(IPlaybackPlayer player);
    boolean IsPlaying();
    void SetPlaying(boolean playing);
    int GetPlayhead();
    int GetTotalFrames();
}
