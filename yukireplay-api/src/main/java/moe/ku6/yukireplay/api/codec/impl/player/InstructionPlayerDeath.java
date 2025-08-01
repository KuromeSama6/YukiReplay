package moe.ku6.yukireplay.api.codec.impl.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import lombok.Getter;
import moe.ku6.yukireplay.api.codec.IEntityLifetimeEnd;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class InstructionPlayerDeath extends PlayerInstruction implements IEntityLifetimeEnd {
    public InstructionPlayerDeath(ByteBuffer buf) {
        super(buf);
    }

    public InstructionPlayerDeath(int trackerId) {
        super(trackerId);
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.PLAYER_DEATH;
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
    }

    @Override
    public void Apply(IPlayback playback) {
        IPlaybackPlayer player = playback.GetTracked(trackerId);
        if (player == null) {
            return;
        }
        var mgr = PacketEvents.getAPI().getPlayerManager();

        var eid = player.GetClientPlayer().PlayDeathAnimation();

        var healthData = new EntityData(6, EntityDataTypes.FLOAT, 0f);
        var healthPacket = new WrapperPlayServerEntityMetadata(eid, List.of(healthData));
        playback.GetViewers().forEach(c -> {
            mgr.sendPacket(c, healthPacket);
            c.playSound(player.GetLocation(), Sound.HURT_FLESH, 1f, 1f);
        });
    }

    @Override
    public int GetTrackerId() {
        return trackerId;
    }
}
