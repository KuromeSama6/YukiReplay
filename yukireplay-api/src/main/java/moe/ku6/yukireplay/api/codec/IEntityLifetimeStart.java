package moe.ku6.yukireplay.api.codec;

import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackEntity;

public interface IEntityLifetimeStart {
    int GetTrackerId();

    /**
     * Called when an entity's lifetime starts for the first time to create that entity.
     * Called only during playback load.
     */
    IPlaybackEntity CreateEntity(IPlayback playback);
}
