package moe.ku6.yukireplay.playback;

import moe.ku6.yukireplay.YukiReplay;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionAddPlayer;
import moe.ku6.yukireplay.api.nms.GameProfilePropertyWrapper;
import moe.ku6.yukireplay.api.nms.IClientPlayer;
import moe.ku6.yukireplay.api.nms.IGameProfile;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TrackedPlaybackPlayer extends TrackedPlaybackEntity implements IPlaybackPlayer {
    private final UUID uuid;
    private final String name;
    private final IGameProfile gameProfile;
    private final IClientPlayer clientPlayer;

    public TrackedPlaybackPlayer(ReplayPlayback playback, InstructionAddPlayer instruction) {
        super(playback, instruction.getTrackerId());
//        uuid = instruction.getUuid();
        uuid = UUID.randomUUID();
        name = instruction.getName();

        gameProfile = YukiReplay.getInstance().getVersionAdaptor().CreateGameProfile(uuid, name);
        gameProfile.SetProperty("textures", new GameProfilePropertyWrapper("textures", instruction.getSkinValue(), instruction.getSkinSignature()));
        clientPlayer = YukiReplay.getInstance().getVersionAdaptor().CreateClientPlayer(playback.GetWorld(), gameProfile);
    }

    @Override
    public UUID GetUUID() {
        return uuid;
    }

    @Override
    public String GetName() {
        return name;
    }

    @Override
    public IClientPlayer GetClientPlayer() {
        return clientPlayer;
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
