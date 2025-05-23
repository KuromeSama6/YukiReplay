package moe.ku6.yukireplay.recorder;

import lombok.Getter;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerArmSwing;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerInventory;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerMotionStatus;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerPosition;
import moe.ku6.yukireplay.api.util.ItemUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
    private boolean sprinting, sneaking, digging;
    private int diggingCounter;
    private ItemStack held, helmet, chestplate, leggings, boots;

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

    public void SaveInventory() {
        var inv = player.getInventory();

        if (!ItemUtil.ItemsEqual(held, inv.getItemInHand())) {
            held = inv.getItemInHand();
            recorder.ScheduleInstruction(new InstructionPlayerInventory(trackerId, InstructionPlayerInventory.Slot.MAINHAND, held));
        }

        if (!ItemUtil.ItemsEqual(helmet, inv.getHelmet())) {
            helmet = inv.getHelmet();
            recorder.ScheduleInstruction(new InstructionPlayerInventory(trackerId, InstructionPlayerInventory.Slot.HELMET, helmet));
        }

        if (!ItemUtil.ItemsEqual(chestplate, inv.getChestplate())) {
            chestplate = inv.getChestplate();
            recorder.ScheduleInstruction(new InstructionPlayerInventory(trackerId, InstructionPlayerInventory.Slot.CHESTPLATE, chestplate));
        }

        if (!ItemUtil.ItemsEqual(leggings, inv.getLeggings())) {
            leggings = inv.getLeggings();
            recorder.ScheduleInstruction(new InstructionPlayerInventory(trackerId, InstructionPlayerInventory.Slot.LEGGINGS, leggings));
        }

        if (!ItemUtil.ItemsEqual(boots, inv.getBoots())) {
            boots = inv.getBoots();
            recorder.ScheduleInstruction(new InstructionPlayerInventory(trackerId, InstructionPlayerInventory.Slot.BOOTS, boots));
        }
    }

    public void SetDigging(boolean digging) {
        this.digging = digging;
        diggingCounter = 0;
    }

    public void Update() {
        {
            if (digging) {
                if (diggingCounter == 0) {
                    recorder.ScheduleInstruction(new InstructionPlayerArmSwing(trackerId));
                }
                ++diggingCounter;
                if (diggingCounter >= 4) diggingCounter = 0;
            }
        }
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
