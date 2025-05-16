package moe.ku6.yukireplay.codec;

import moe.ku6.yukireplay.playback.ReplayPlayback;
import moe.ku6.yukireplay.util.CodecUtil;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

public abstract class PlayerInstruction extends Instruction {
    private final UUID uuid;
    public PlayerInstruction(ByteBuffer buf) {
        super(buf);
        uuid = CodecUtil.ReadUUID(buf);
    }

    public PlayerInstruction(Player player) {
        uuid = player.getUniqueId();
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        CodecUtil.WriteUUID(out, uuid);
    }

    @Override
    public void Apply(ReplayPlayback playback) {

    }

    @Override
    public String toString() {
        return "PlayerInstruction(uuid=%s, ???)".formatted(uuid);
    }
}
