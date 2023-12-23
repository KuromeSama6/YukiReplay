package moe.protasis.replay.action;

import com.google.gson.JsonObject;
import lombok.Getter;
import moe.protasis.replay.util.JsonObjectBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * Represents an action that is captured by the replay.
 */
public abstract class Action {
    @Getter private final int frame;

    public Action(int frame) {
        this.frame = frame;
    }

    public Action(JsonObject data) {
        frame = data.get("frame").getAsInt();
    }

    public final JsonObject Serialize() {
        JsonObject ret = new JsonObject();
        ret.addProperty("_type", getClass().getName());
        ret.addProperty("frame", frame);
        SerializeInternal(ret);
        return ret;
    }

    protected abstract void SerializeInternal(JsonObject data);

    public abstract void Apply(Player player);

}
