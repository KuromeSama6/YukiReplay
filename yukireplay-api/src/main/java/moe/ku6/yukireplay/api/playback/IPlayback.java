package moe.ku6.yukireplay.api.playback;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public interface IPlayback {
    World GetWorld();
    void StepPlayback();
    void RewindPlayback();
    String GetMetadataAsString();
    void Close();
    void AddViewers(Player... players);
    void RemoveViewers(Player... players);
    <T extends IPlaybackEntity> T GetTracked(int trackerId);
    List<IPlaybackPlayer> GetTrackedPlayers();
    void AddTrackedEntity(IPlaybackEntity entity);
    void AddTrackedPlayer(IPlaybackPlayer player);
    boolean IsPlaying();
    void SetPlaying(boolean playing);
    int GetPlayhead();
    int GetTotalFrames();
    Collection<Player> GetViewers();
    void SendViewerPacket(PacketWrapper<?> packet);
    double GetSpeed();
    void SetSpeed(double speed);
    boolean IsRewinding();
    void SetRewinding(boolean rewinding);
    void Restart();
}
