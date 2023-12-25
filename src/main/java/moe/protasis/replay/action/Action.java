package moe.protasis.replay.action;

import com.google.gson.JsonObject;
import lombok.Getter;
import moe.protasis.replay.playback.Playback;
import moe.protasis.replay.replay.Replay;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents an action that is captured by the replay.
 */
public abstract class Action {
    @Getter private final int frame;
    @Getter private final UUID uuid;

    public Action(Replay replay, Player player) {
        this.frame = replay.getFrame();
        uuid = player.getUniqueId();
    }

    public Action(JsonObject data) {
        frame = data.get("frame").getAsInt();
        uuid = UUID.fromString(data.get("uuid").getAsString());
    }

    public final JsonObject Serialize() {
        JsonObject ret = new JsonObject();
        ret.addProperty("_type", getClass().getName());
        ret.addProperty("frame", frame);
        ret.addProperty("uuid", uuid.toString());
        SerializeInternal(ret);
        return ret;
    }

    protected abstract void SerializeInternal(JsonObject data);
    public abstract void Execute(Playback playback);

}
