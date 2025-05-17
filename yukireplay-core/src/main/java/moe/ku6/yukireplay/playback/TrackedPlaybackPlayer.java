package moe.ku6.yukireplay.playback;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import moe.ku6.yukireplay.YukiReplay;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionAddPlayer;
import moe.ku6.yukireplay.api.nms.IClientPlayer;
import moe.ku6.yukireplay.api.nms.IGameProfile;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TrackedPlaybackPlayer extends AbstractTrackedPlaybackEntity implements IPlaybackPlayer {
    private final UUID uuid;
    private final IGameProfile gameProfile;
    private final IClientPlayer clientPlayer;

    public TrackedPlaybackPlayer(ReplayPlayback playback, InstructionAddPlayer instruction) {
        super(playback, instruction.getTrackerId());
        uuid = instruction.getUuid();

        gameProfile = YukiReplay.getInstance().getVersionAdaptor().CreateGameProfile(uuid, instruction.getName());
        clientPlayer = YukiReplay.getInstance().getVersionAdaptor().CreateClientPlayer(playback.getWorld(), gameProfile);
    }

    @Override
    public UUID GetUUID() {
        return uuid;
    }

    @Override
    public void SetSkin(String value, String signature) {
        clientPlayer.SetSkin(value, signature);
        viewers.forEach(clientPlayer::ForceRefresh);
    }

    @Override
    protected void SpawnOnClient(Player viewer) {
        clientPlayer.SpawnTo(viewer);
    }

    @Override
    protected void DespawnOnClient(Player viewer) {
        clientPlayer.DespawnFrom(viewer);
    }

    @Override
    public Location GetLocation() {
        return clientPlayer.GetLocation();
    }

    @Override
    public void SetLocation(Location location) {
        clientPlayer.SetLocation(location);
    }
}
