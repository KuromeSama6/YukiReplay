package moe.ku6.yukireplay.api.util;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@UtilityClass
public class CodecUtil {
    public static String ReadLengthPrefixed(ByteBuffer buf) {
        var size = buf.getInt();
        if (size < 0)
            throw new IllegalArgumentException("Invalid string size: " + size);

        var bytes = new byte[size];
        buf.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void WriteLengthPrefixed(DataOutputStream out, String str) {
        var bytes = str.getBytes(StandardCharsets.UTF_8);
        try {
            out.writeInt(bytes.length);
            out.write(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static UUID ReadUUID(ByteBuffer buf) {
        long mostSigBits = buf.getLong();
        long leastSigBits = buf.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    public static void WriteUUID(DataOutputStream out, UUID uuid) {
        try {
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
