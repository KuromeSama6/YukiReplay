package moe.ku6.yukireplay.api.nms;

import org.bukkit.entity.Player;

public interface IClientPlayer extends IClientEntity {
    IGameProfile GetGameProfile();
    void SetSkin(String value, String signature);
    void SetSneaking(boolean sneaking);
    void SetHealth(float health);
    int PlayDeathAnimation();
}
