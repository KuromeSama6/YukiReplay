package moe.ku6.yukireplay.api.codec;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ku6.yukireplay.api.codec.impl.player.*;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum InstructionType {
    ADD_PLAYER((short)0x0001, InstructionAddPlayer.class),
    REMOVE_PLAYER((short)0x0002, InstructionRemovePlayer.class),
    PLAYER_POSITION((short)0x0003, InstructionPlayerPosition.class),
    PLAYER_MOTION_STATUS((short)0x0004, InstructionPlayerMotionStatus.class),
    PLAYER_ARM_SWING((short)0x0005, InstructionPlayerArmSwing.class),
    PLAYER_CHAT((short)0x0006, InstructionPlayerChat.class),

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
