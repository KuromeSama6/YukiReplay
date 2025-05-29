package moe.ku6.yukireplay.api.codec.impl.entity;

import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import lombok.Getter;
import moe.ku6.yukireplay.api.YukiReplayAPI;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.util.CodecUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class InstructionItemProjectileSpawn extends InstructionProjectileSpawn {
    @Getter
    private final ItemStack item;

    public InstructionItemProjectileSpawn(ByteBuffer buf) {
        super(buf);
        var adapter = YukiReplayAPI.Get().GetVersionAdaptor();
        var str = CodecUtil.ReadLengthPrefixed(buf);
//        System.out.println("read item: %s".formatted(str));
        item = adapter.DeserializeItemStack(str);
    }

    public InstructionItemProjectileSpawn(int trackerId, int launcherId, Projectile projectile, ItemStack item) {
        super(trackerId, launcherId, projectile);
        this.item = item.clone();
        this.item.setAmount(Math.max(item.getAmount(), 1));
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        var adapter = YukiReplayAPI.Get().GetVersionAdaptor();
        CodecUtil.WriteLengthPrefixed(out, adapter.SerializeItemStack(item));
    }
    @Override
    public void Apply(IPlayback playback) {
        var proj = YukiReplayAPI.Get().CreateItemProjectile(playback, this);
        playback.AddTrackedEntity(proj);

        playback.GetViewers().forEach(c -> c.playSound(location.ToBukkitLocation(playback.GetWorld()), Sound.SHOOT_ARROW, 1f, 0.5f));
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.ITEM_PROJECTILE_SPAWN;
    }
}
