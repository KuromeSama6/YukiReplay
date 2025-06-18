package moe.ku6.yukireplay.api.codec.impl.block;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockBreakAnimation;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.BlockInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import org.bukkit.Location;

import java.nio.ByteBuffer;

public class InstructionBlockBreakProgress extends BlockInstruction {
    private final int trackerId;
    private final byte progress;

    public InstructionBlockBreakProgress(ByteBuffer buf) {
        super(buf);
        trackerId = buf.getInt();
        progress = buf.get();
    }

    public InstructionBlockBreakProgress(Location pos, int trackerId, byte progress) {
        super(pos);
        this.trackerId = trackerId;
        this.progress = progress;
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.BLOCK_BREAK_PROGRESS;
    }

    @Override
    public void Apply(IPlayback playback) {
        IPlaybackPlayer tracked = playback.GetTracked(trackerId);
        var packet = new WrapperPlayServerBlockBreakAnimation(tracked.GetClientPlayer().GetEntityId(), new Vector3i(x, y, z), progress);

        playback.GetViewers().forEach(c -> PacketEvents.getAPI().getPlayerManager().sendPacket(c, packet));
    }
}