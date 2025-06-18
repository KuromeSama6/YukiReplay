package moe.ku6.yukireplay.api.codec.impl.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import moe.ku6.yukireplay.api.YukiReplayAPI;
import moe.ku6.yukireplay.api.codec.IEntityLifetimeEnd;
import moe.ku6.yukireplay.api.codec.IEntityLifetimeStart;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackEntity;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class InstructionPlayerRespawn extends PlayerInstruction implements IEntityLifetimeStart {
    public InstructionPlayerRespawn(ByteBuffer buf) {
        super(buf);
    }

    public InstructionPlayerRespawn(int trackerId) {
        super(trackerId);
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.PLAYER_RESPAWN;
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
        playback.GetViewers().forEach(player.GetClientPlayer()::ForceRefresh);

        Bukkit.getScheduler().scheduleSyncDelayedTask(YukiReplayAPI.Get().GetProvidingPlugin(),  player::RefreshInventory, 2);
    }

    @Override
    public int GetTrackerId() {
        return trackerId;
    }

    @Override
    public IPlaybackEntity CreateEntity(IPlayback playback) {
        throw new UnsupportedOperationException("Player respawn does not create an entity.");
    }
}
