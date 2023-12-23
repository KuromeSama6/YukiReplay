package moe.protasis.replay.playback;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import moe.protasis.replay.YukiReplay;
import moe.protasis.replay.action.Action;
import moe.protasis.replay.util.CompressionUtil;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Playback {
    private final List<Action> actions = new ArrayList<>();
    private final List<Player> viewers = new ArrayList<>();

    public Playback(JsonArray data) {
        for (JsonElement e : data) {
            if (!e.isJsonObject()) continue;
            JsonObject object = e.getAsJsonObject();
            String type = object.get("_type").getAsString();
            int frame = object.get("frame").getAsInt();

            // reflections
            try {
                Class<?> clazz = Class.forName(type);
                actions.add((Action) clazz
                        .getDeclaredConstructor(JsonObject.class)
                        .newInstance(object));

            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException ex) {
                YukiReplay.getInstance().getLogger().severe(String.format("A bad frame is encountered during replay parsing at frame %s", frame));
                ex.printStackTrace();
            }

        }
    }

    public void AddViewer(Player player) {
        viewers.add(player);
    }

    public void RemoveViewer(Player player) {
        viewers.remove(player);
    }

    public static Playback LoadFromDirectory(String name) throws IOException {
        File file = new File(YukiReplay.getInstance().getDataFolder() + "/replays/" + name + ".repl");
        if (!file.exists()) return null;

        byte[] data = Files.readAllBytes(file.toPath());
        return new Playback(new Gson().fromJson(CompressionUtil.DecompressToString(data), JsonArray.class));
    }
}
