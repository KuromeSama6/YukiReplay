package moe.ku6.yukireplay.api.codec.impl.entity;

import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleData;
import com.github.retrooper.packetevents.protocol.particle.data.ParticleItemStackData;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import moe.ku6.yukireplay.api.YukiReplayAPI;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.EntityInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackItemProjectile;
import moe.ku6.yukireplay.api.util.RandomUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class InstructionPotionSplash extends EntityInstruction {
    private final double posX, posY, posZ;

    public InstructionPotionSplash(ByteBuffer buf) {
        super(buf);
        posX = buf.getDouble();
        posY = buf.getDouble();
        posZ = buf.getDouble();

    }

    public InstructionPotionSplash(int trackerId, ThrownPotion potion) {
        super(trackerId);
        var pos = potion.getLocation();
        posX = pos.getX();
        posY = pos.getY();
        posZ = pos.getZ();

        var potionMeta = (PotionMeta)potion.getItem().getItemMeta();
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        out.writeDouble(posX);
        out.writeDouble(posY);
        out.writeDouble(posZ);
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.POTION_SPLASH;
    }

    @Override
    public void Apply(IPlayback playback) {
        var world = playback.GetWorld();
        var pos = new Location(world, posX, posY, posZ);
        IPlaybackItemProjectile entity = playback.GetTracked(trackerId);

        playback.SendViewerPacket(new WrapperPlayServerSoundEffect(Sounds.ENTITY_SPLASH_POTION_BREAK, SoundCategory.NEUTRAL, new Vector3i((int) posX, (int) posY, (int) posZ), 1f, 1f));

        playback.GetViewers().forEach(c -> YukiReplayAPI.Get().GetVersionAdaptor().PlayPotionSplashEffect(c, pos, entity.GetItem()));
    }
}
