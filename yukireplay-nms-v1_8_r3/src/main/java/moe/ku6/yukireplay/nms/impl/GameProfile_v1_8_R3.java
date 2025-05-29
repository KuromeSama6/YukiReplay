package moe.ku6.yukireplay.nms.impl;

import com.mojang.authlib.GameProfile;
import lombok.Data;
import moe.ku6.yukireplay.api.nms.GameProfilePropertyWrapper;
import moe.ku6.yukireplay.api.nms.IGameProfile;
import moe.ku6.yukireplay.api.nms.IGameProfileProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class GameProfile_v1_8_R3 implements IGameProfile {
    private UUID uuid;
    private String name;
    private boolean legacy;
    private final Map<String, IGameProfileProperty> properties = new HashMap<>();

    public GameProfile_v1_8_R3(GameProfile gameProfile) {
        uuid = gameProfile.getId();
        name = gameProfile.getName();
        legacy = gameProfile.isLegacy();

        var props = gameProfile.getProperties();
        for (var key : props.keySet()) {
            var prop = props.get(key);
            if (prop != null) {
                for (var property : prop) {
                    properties.put(key, new GameProfilePropertyWrapper(property.getName(), property.getValue(), property.getSignature()));
                }
            }
        }
    }

    public GameProfile_v1_8_R3(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public IGameProfileProperty GetProperty(String key) {
        return properties.get(key);
    }

    @Override
    public Map<String, IGameProfileProperty> GetProperties() {
        return properties;
    }

    @Override
    public void SetProperty(String key, IGameProfileProperty value) {
        properties.put(key, value);
    }
}
