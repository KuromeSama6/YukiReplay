package moe.ku6.yukireplay.api.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Vec2i {
    private final int x, y;

    public boolean Contains(int t) {
        return t >= x && t <= y;
    }
}
