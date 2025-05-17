package moe.ku6.yukireplay.api.codec.impl.player;

import lombok.ToString;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.util.CodecUtil;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

@ToString
public class InstructionRemovePlayer extends PlayerInstruction {
    private final UUID uuid;
    public InstructionRemovePlayer(ByteBuffer buf) {
        super(buf);
        uuid = CodecUtil.ReadUUID(buf);
    }

    public InstructionRemovePlayer(Player player, int trackerId) {
        super(trackerId);
        uuid = player.getUniqueId();
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        CodecUtil.WriteUUID(out, uuid);
    }

    @Override
    public void Apply(IPlayback playback) {
        var player = playback.GetTrackedPlayer(trackerId);
        if (player != null) {
            playback.RemoveTrackedPlayer(player);
        }
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.REMOVE_PLAYER;
    }


}
