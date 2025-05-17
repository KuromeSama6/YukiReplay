package moe.ku6.yukireplay.api.playback;

import java.util.UUID;

public interface IPlaybackPlayer extends IPlaybackEntity {
    UUID GetUUID();
    void SetSkin(String value, String signature);
}
