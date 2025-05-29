package moe.ku6.yukireplay.api.util;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;

@UtilityClass
public class RandomUtil {
    private static final SecureRandom random = new SecureRandom();

    public static boolean RandomBool() {
        return random.nextBoolean();
    }
}
