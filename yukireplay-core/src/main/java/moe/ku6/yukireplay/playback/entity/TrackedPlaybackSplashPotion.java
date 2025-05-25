package moe.ku6.yukireplay.playback.entity;

import moe.ku6.yukireplay.api.YukiReplayAPI;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionPotSpawn;
import moe.ku6.yukireplay.api.nms.entity.IClientSplashPotion;
import moe.ku6.yukireplay.api.playback.IPlaybackSplashPotion;
import moe.ku6.yukireplay.playback.ReplayPlayback;
import moe.ku6.yukireplay.playback.TrackedPlaybackEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TrackedPlaybackSplashPotion extends TrackedPlaybackEntity implements IPlaybackSplashPotion {
    private final ItemStack potionItem;
    private final IClientSplashPotion splashPotion;
    public TrackedPlaybackSplashPotion(ReplayPlayback playback, InstructionPotSpawn instruction) {
        super(playback, instruction.getTrackerId());
        potionItem = instruction.getItem();

        splashPotion = YukiReplayAPI.Get().GetVersionAdaptor().CreateClientSplashPotion(playback.GetWorld(), instruction.getLocation(), potionItem);
    }

    @Override
    protected void SpawnOnClient(Player viewer) {
        splashPotion.SpawnTo(viewer);
    }

    @Override
    protected void DespawnOnClient(Player viewer) {
        splashPotion.DespawnFrom(viewer);
    }

    @Override
    public Location GetLocation() {
        return splashPotion.GetLocation();
    }

    @Override
    public void SetLocation(Location location) {
        splashPotion.SetLocation(location);
    }

    @Override
    public ItemStack GetPotionItem() {
        return potionItem;
    }
}
