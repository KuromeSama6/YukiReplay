package moe.ku6.yukireplay.api.codec.impl.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import org.bukkit.Sound;

import java.nio.ByteBuffer;

public class InstructionPlayerDamage extends PlayerInstruction {
    public InstructionPlayerDamage(ByteBuffer buf) {
        super(buf);
    }

    public InstructionPlayerDamage(int trackerId) {
        super(trackerId);
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.PLAYER_DAMAGE;
    }

    @Override
    public void Apply(IPlayback playback) {
        var player = playback.GetTrackedPlayer(trackerId);
        var packet = new WrapperPlayServerEntityAnimation(player.GetClientPlayer().GetEntityId(), WrapperPlayServerEntityAnimation.EntityAnimationType.HURT);

        for (var viewer : playback.GetViewers()) {
            PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
            viewer.playSound(player.GetLocation(), Sound.HURT_FLESH, 1f, 1f);
        }
    }
}
