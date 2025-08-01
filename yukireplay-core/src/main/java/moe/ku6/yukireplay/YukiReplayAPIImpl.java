package moe.ku6.yukireplay;

import lombok.AllArgsConstructor;
import moe.ku6.yukireplay.api.IYukiReplayAPI;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionItemProjectileSpawn;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionAddPlayer;
import moe.ku6.yukireplay.api.exception.PlaybackLoadException;
import moe.ku6.yukireplay.api.nms.IVersionAdaptor;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import moe.ku6.yukireplay.api.playback.IPlaybackItemProjectile;
import moe.ku6.yukireplay.api.recorder.IRecorder;
import moe.ku6.yukireplay.api.recorder.RecorderOptions;
import moe.ku6.yukireplay.api.playback.EntityLifetime;
import moe.ku6.yukireplay.playback.ReplayPlayback;
import moe.ku6.yukireplay.playback.TrackedPlaybackPlayer;
import moe.ku6.yukireplay.playback.entity.TrackedPlaybackItemProjectile;
import moe.ku6.yukireplay.recorder.ReplayRecorder;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

@AllArgsConstructor
public class YukiReplayAPIImpl implements IYukiReplayAPI {
    private final YukiReplay plugin;

    @Override
    public JavaPlugin GetProvidingPlugin() {
        return plugin;
    }

    @Override
    public IVersionAdaptor GetVersionAdaptor() {
        return plugin.getVersionAdaptor();
    }

    @Override
    public IRecorder CreateRecorder(World world, RecorderOptions options) {
        return new ReplayRecorder(world, options);
    }

    @Override
    public IPlayback CreatePlayback(World world, byte[] data) throws PlaybackLoadException {
        return new ReplayPlayback(world, data);
    }

    @Override
    public IPlaybackPlayer CreatePlaybackPlayer(IPlayback playback, InstructionAddPlayer instruction) {
        return new TrackedPlaybackPlayer((ReplayPlayback)playback, instruction);
    }

    @Override
    public IPlaybackItemProjectile CreateItemProjectile(IPlayback playback, InstructionItemProjectileSpawn instruction) {
        return new TrackedPlaybackItemProjectile((ReplayPlayback)playback, instruction);
    }
}
