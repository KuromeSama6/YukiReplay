package moe.ku6.yukireplay.api.codec.impl.entity;

import lombok.Getter;
import moe.ku6.yukireplay.api.YukiReplayAPI;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.util.CodecUtil;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class InstructionPotSpawn extends InstructionProjectileSpawn {
    @Getter
    private final ItemStack item;

    public InstructionPotSpawn(ByteBuffer buf) {
        super(buf);
        var adapter = YukiReplayAPI.Get().GetVersionAdaptor();
        item = adapter.DeserializeItemStack(CodecUtil.ReadLengthPrefixed(buf));
    }

    public InstructionPotSpawn(int trackerId, int launcherId, ThrownPotion pot) {
        super(trackerId, launcherId, pot);
        item = pot.getItem().clone();
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        var adapter = YukiReplayAPI.Get().GetVersionAdaptor();
        CodecUtil.WriteLengthPrefixed(out, adapter.SerializeItemStack(item));
    }

    @Override
    protected EntityType GetEntityType() {
        return EntityType.SPLASH_POTION;
    }

    @Override
    public void Apply(IPlayback playback) {
        var pot = YukiReplayAPI.Get().CreateSplashPotion(playback, this);
        playback.AddTrackedEntity(pot);
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.POT_SPAWN;
    }
}
