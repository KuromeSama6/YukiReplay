package moe.ku6.yukireplay.api.nms;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;

import java.util.Map;
import java.util.UUID;

public interface IGameProfile {
    UUID getUuid();
    String getName();
    boolean isLegacy();
    IGameProfileProperty GetProperty(String key);
    Map<String, IGameProfileProperty> GetProperties();
    void SetProperty(String key, IGameProfileProperty value);
}
