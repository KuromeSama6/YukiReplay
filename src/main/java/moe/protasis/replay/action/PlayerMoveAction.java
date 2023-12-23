package moe.protasis.replay.action;

import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveAction extends Action {
    private double x, y, z;
    private float yaw, pitch;

    public PlayerMoveAction(int frame, PlayerMoveEvent e) {
        super(frame);

        Location to = e.getTo();
        x = to.getX();
        y = to.getY();
        z = to.getZ();
        yaw = to.getYaw();
        pitch = to.getPitch();
    }

    public PlayerMoveAction(JsonObject data) {
        super(data);

        x = data.get("x").getAsDouble();
        y = data.get("y").getAsDouble();
        z = data.get("z").getAsDouble();
        yaw = data.get("yaw").getAsFloat();
        pitch = data.get("pitch").getAsFloat();
    }

    @Override
    protected void SerializeInternal(JsonObject data) {
        data.addProperty("x", x);
        data.addProperty("y", y);
        data.addProperty("z", z);
        data.addProperty("yaw", yaw);
        data.addProperty("pitch", pitch);
    }

    @Override
    public void Apply(Player player) {
        player.teleport(new Location(player.getWorld(), x, y, z, yaw, pitch));
    }
}
