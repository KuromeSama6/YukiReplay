package moe.ku6.yukireplay.api.nms;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GameProfilePropertyWrapper implements IGameProfileProperty {
    private final String name, value, signature;
}
