package moe.ku6.yukireplay.codec;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ku6.yukireplay.codec.impl.player.InstructionAddPlayer;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum InstructionType {
    ADD_PLAYER((short)0x01, InstructionAddPlayer.class),
    REMOVE_PLAYER((short)0x02, InstructionAddPlayer.class),
    PLAYER_POSITION((short)0x03, InstructionAddPlayer.class)

    ;
    private static final Map<Short, InstructionType> map = new HashMap<>();
    static {
        for (InstructionType type : InstructionType.values()) {
            map.put(type.getId(), type);
        }
    }

    private final short id;
    private final Class<? extends Instruction> clazz;

    public static InstructionType ById(short id) {
        return map.get(id);
    }
}
