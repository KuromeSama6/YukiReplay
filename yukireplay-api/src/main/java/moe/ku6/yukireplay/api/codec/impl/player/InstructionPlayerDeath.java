package moe.ku6.yukireplay.api.codec.impl.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import lombok.Getter;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import org.bukkit.Sound;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class InstructionPlayerDeath extends PlayerInstruction {
    @Getter
    private final boolean dead;

    public InstructionPlayerDeath(ByteBuffer buf) {
        super(buf);
        dead = buf.get() == 1;
    }

    public InstructionPlayerDeath(int trackerId, boolean dead) {
        super(trackerId);
        this.dead = dead;
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.PLAYER_DEATH;
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        out.writeByte(dead ? 1 : 0);
    }

    @Override
    public void Apply(IPlayback playback) {
        IPlaybackPlayer player = playback.GetTracked(trackerId);
        if (player == null) {
            return;
        }

        var metadata = new EntityData(6, EntityDataTypes.FLOAT, dead ? 0.0f : 20.0f);
        var metadataPacket = new WrapperPlayServerEntityMetadata(player.GetClientPlayer().GetEntityId(), List.of(metadata));

        var packet = new WrapperPlayServerEntityStatus(player.GetClientPlayer().GetEntityId(), dead ? 3 : 2);
        playback.GetViewers().forEach(c -> {
            PacketEvents.getAPI().getPlayerManager().sendPacket(c, packet);
            PacketEvents.getAPI().getPlayerManager().sendPacket(c, metadataPacket);
            c.playSound(player.GetLocation(), Sound.HURT_FLESH, 1f, 1f);
        });
    }
}
