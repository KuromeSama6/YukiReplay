package moe.ku6.yukireplay.api.playback;

import moe.ku6.yukireplay.api.nms.IClientPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface IPlaybackPlayer extends IPlaybackEntity {
    UUID GetUUID();
    String GetName();
    IClientPlayer GetClientPlayer();
    void SetSkin(String value, String signature);
}
