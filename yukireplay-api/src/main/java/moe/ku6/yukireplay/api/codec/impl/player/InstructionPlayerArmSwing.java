package moe.ku6.yukireplay.api.codec.impl.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import lombok.ToString;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;

import java.nio.ByteBuffer;

@ToString
public class InstructionPlayerArmSwing extends PlayerInstruction {
    public InstructionPlayerArmSwing(ByteBuffer buf) {
        super(buf);
    }

    public InstructionPlayerArmSwing(int trackerId) {
        super(trackerId);
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.PLAYER_ARM_SWING;
    }

    @Override
    public void Apply(IPlayback playback) {
        IPlaybackPlayer player = playback.GetTracked(trackerId);
        var packet = new WrapperPlayServerEntityAnimation(player.GetClientPlayer().GetEntityId(), WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM);
        playback.GetViewers().forEach(c -> PacketEvents.getAPI().getPlayerManager().sendPacket(c, packet));
    }
}
