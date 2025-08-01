package moe.ku6.yukireplay.api.codec.impl.player;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import lombok.Getter;
import lombok.ToString;
import moe.ku6.yukireplay.api.YukiReplayAPI;
import moe.ku6.yukireplay.api.codec.IEntityLifetimeEnd;
import moe.ku6.yukireplay.api.codec.IEntityLifetimeStart;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackEntity;
import moe.ku6.yukireplay.api.util.CodecUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

@ToString
public class InstructionAddPlayer extends PlayerInstruction implements IEntityLifetimeStart {
    @Getter
    private final UUID uuid;
    @Getter
    private final String name;
    @Getter
    private final String skinValue, skinSignature;

    public InstructionAddPlayer(ByteBuffer buf) {
        super(buf);
        uuid = CodecUtil.ReadUUID(buf);
        name = CodecUtil.ReadLengthPrefixed(buf);
        skinValue = CodecUtil.ReadLengthPrefixed(buf);
        skinSignature = CodecUtil.ReadLengthPrefixed(buf);
    }

    public InstructionAddPlayer(Player player, int trackerId) {
        super(trackerId);
        uuid = player.getUniqueId();
        name = player.getName();

        var profile = YukiReplayAPI.Get().GetVersionAdaptor().GetGameProfile(player);
        var texture = profile.GetProperty("textures");
        if (texture != null) {
            skinValue = texture.getValue();
            skinSignature = texture.getSignature();
        } else {
            skinValue = "";
            skinSignature = "";
        }
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.ADD_PLAYER;
    }

    @Override
    public IPlaybackEntity CreateEntity(IPlayback playback) {
        return YukiReplayAPI.Get().CreatePlaybackPlayer(playback, this);
    }

    @Override
    public void Apply(IPlayback playback) {
//        System.out.println("add player, " + this);
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        CodecUtil.WriteUUID(out, uuid);
        CodecUtil.WriteLengthPrefixed(out, name);
        CodecUtil.WriteLengthPrefixed(out, skinValue);
        CodecUtil.WriteLengthPrefixed(out, skinSignature);
    }

    @Override
    public int GetTrackerId() {
        return trackerId;
    }
}
