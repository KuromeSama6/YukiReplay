package moe.ku6.yukireplay.api.codec;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ku6.yukireplay.api.codec.impl.block.InstructionBlockBreakProgress;
import moe.ku6.yukireplay.api.codec.impl.block.InstructionBlockChange;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionEntityDespawn;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionEntityPosition;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionItemProjectileSpawn;
import moe.ku6.yukireplay.api.codec.impl.entity.InstructionPotionSplash;
import moe.ku6.yukireplay.api.codec.impl.player.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum InstructionType {
    ADD_PLAYER((short)0x0001, InstructionAddPlayer.class),
    REMOVE_PLAYER((short)0x0002, InstructionRemovePlayer.class),
    ENTITY_POSITION((short)0x0003, InstructionEntityPosition.class),
    PLAYER_MOTION_STATUS((short)0x0004, InstructionPlayerMotionStatus.class),
    PLAYER_ARM_SWING((short)0x0005, InstructionPlayerArmSwing.class),
    PLAYER_CHAT((short)0x0006, InstructionPlayerChat.class),
    PLAYER_DAMAGE((short)0x0007, InstructionPlayerDamage.class),
    PLAYER_DEATH((short)0x0008, InstructionPlayerDeath.class),
    PLAYER_INVENTORY((short)0x0009, InstructionPlayerInventory.class),
    BLOCK_CHANGE((short)0x000A, InstructionBlockChange.class),
    BLOCK_BREAK_PROGRESS((short)0x000B, InstructionBlockBreakProgress.class),
    ITEM_PROJECTILE_SPAWN((short)0x000C, InstructionItemProjectileSpawn.class),
    ENTITY_DESPAWN((short)0x000D, InstructionEntityDespawn.class),
    POTION_SPLASH((short)0x000E, InstructionPotionSplash.class),
    PEARL_TELEPORT((short)0x000F, InstructionPearlTeleport.class),

    ;
    private static final Map<Short, InstructionType> map = new HashMap<>();
    static {
        for (InstructionType type : InstructionType.values()) {
            map.put(type.getId(), type);
        }
    }

    private final short id;
    private final Class<? extends Instruction> clazz;

    public Instruction CreateInstance(ByteBuffer data) {
        try {
            return clazz.getDeclaredConstructor(ByteBuffer.class).newInstance(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static InstructionType ById(short id) {
        return map.get(id);
    }
}
