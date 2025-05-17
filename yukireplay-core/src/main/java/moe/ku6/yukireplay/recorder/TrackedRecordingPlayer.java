package moe.ku6.yukireplay.recorder;

import lombok.Getter;
import lombok.Setter;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerMotionStatus;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerPosition;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TrackedRecordingPlayer {
    private final ReplayRecorder recorder;
    @Getter
    private final int trackerId;
    @Getter
    private final Player player;
    @Getter
    private double lastX, lastY, lastZ;
    @Getter
    private float lastYaw, lastPitch;
    private boolean sprinting, sneaking;

    public TrackedRecordingPlayer(ReplayRecorder recorder, Player player) {
        trackerId = recorder.GetNextTrackerId();
        this.recorder = recorder;
        this.player = player;

        var pos = player.getLocation();
        lastX = pos.getX();
        lastY = pos.getY();
        lastZ = pos.getZ();
        lastYaw = pos.getYaw();
        lastPitch = pos.getPitch();
    }

    public synchronized void SetSprintSneak(Boolean sprinting, Boolean sneaking) {
        if (sprinting != null) {
            this.sprinting = sprinting;
        }
        if (sneaking != null) {
            this.sneaking = sneaking;
        }

        recorder.ScheduleInstruction(new InstructionPlayerMotionStatus(trackerId, this.sneaking, this.sprinting));
    }

    public void UpdatePosition(Location newPos) {
        var x = newPos.getX();
        var y = newPos.getY();
        var z = newPos.getZ();
        var yaw = newPos.getYaw();
        var pitch = newPos.getPitch();

        var ret = new InstructionPlayerPosition(
            trackerId, x != lastX ? x : null,
            y != lastY ? y : null,
            z != lastZ ? z : null,
            yaw != lastYaw ? yaw : null,
            pitch != lastPitch ? pitch : null
        );
        lastX = x;
        lastY = y;
        lastZ = z;
        lastYaw = yaw;
        lastPitch = pitch;

        recorder.ScheduleInstruction(ret);
    }

}
