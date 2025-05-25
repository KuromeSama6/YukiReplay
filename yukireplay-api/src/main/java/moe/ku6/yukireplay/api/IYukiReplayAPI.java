package moe.ku6.yukireplay.api;

import moe.ku6.yukireplay.api.codec.impl.entity.InstructionPotSpawn;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionAddPlayer;
import moe.ku6.yukireplay.api.exception.PlaybackLoadException;
import moe.ku6.yukireplay.api.nms.IVersionAdaptor;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import moe.ku6.yukireplay.api.playback.IPlaybackSplashPotion;
import moe.ku6.yukireplay.api.recorder.IRecorder;
import moe.ku6.yukireplay.api.recorder.RecorderOptions;
import org.bukkit.World;

public interface IYukiReplayAPI {
    IVersionAdaptor GetVersionAdaptor();
    IRecorder CreateRecorder(World world, RecorderOptions options);
    default IRecorder CreateRecorder(World world) {
        return CreateRecorder(world, RecorderOptions.Default());
    }

    IPlayback CreatePlayback(World world, byte[] data) throws PlaybackLoadException;
    IPlaybackPlayer CreatePlaybackPlayer(IPlayback playback, InstructionAddPlayer instruction);
    IPlaybackSplashPotion CreateSplashPotion(IPlayback playback, InstructionPotSpawn instruction);
}
