package moe.ku6.yukireplay.api.playback;

import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerInventory;
import moe.ku6.yukireplay.api.nms.IClientPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface IPlaybackPlayer extends IPlaybackEntity {
    UUID GetUUID();
    String GetName();
    IClientPlayer GetClientPlayer();
    void SetSkin(String value, String signature);
    ItemStack GetInventory(InstructionPlayerInventory.Slot slot);
    void SetInventory(InstructionPlayerInventory.Slot slot, ItemStack item);
    void RefreshInventory();
}
