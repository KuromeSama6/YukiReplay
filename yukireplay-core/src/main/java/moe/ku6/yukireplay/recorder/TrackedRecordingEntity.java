package moe.ku6.yukireplay.recorder;

import lombok.Getter;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionEntityPosition;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class TrackedRecordingEntity {
    protected final ReplayRecorder recorder;
    @Getter
    protected final Entity entity;
    @Getter
    protected final int trackerId;
    @Getter
    protected double lastX, lastY, lastZ;
    @Getter
    protected float lastYaw, lastPitch;

    public TrackedRecordingEntity(ReplayRecorder recorder, Entity entity) {
        this.recorder = recorder;
        this.entity = entity;
        trackerId = recorder.GetNextTrackerId();
    }

    public void UpdatePosition() {
        UpdatePosition(entity.getLocation());
    }

    public void UpdatePosition(Location newPos) {
        var x = newPos.getX();
        var y = newPos.getY();
        var z = newPos.getZ();
        var yaw = newPos.getYaw();
        var pitch = newPos.getPitch();

        var ret = new InstructionEntityPosition(
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

    public void Update() {

    }
}
