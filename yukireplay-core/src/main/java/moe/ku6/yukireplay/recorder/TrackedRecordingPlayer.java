package moe.ku6.yukireplay.recorder;

import lombok.Getter;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerArmSwing;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerInventory;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerMotionStatus;
import moe.ku6.yukireplay.api.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TrackedRecordingPlayer extends TrackedRecordingEntity {
    @Getter
    private final Player player;
    private boolean sprinting, sneaking, digging;
    private int diggingCounter;
    private ItemStack held, helmet, chestplate, leggings, boots;

    public TrackedRecordingPlayer(ReplayRecorder recorder, Player player) {
        super(recorder, player);
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

    @Override
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
}
