package moe.protasis.replay.util;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

@UtilityClass
public final class ReflectionUtil {
    public static void SetField(Object obj, String field, Object value) {
        try {
            Field fieldObject = obj.getClass().getDeclaredField(field);

            fieldObject.setAccessible(true);
            fieldObject.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
