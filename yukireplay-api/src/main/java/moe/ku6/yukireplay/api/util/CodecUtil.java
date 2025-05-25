package moe.ku6.yukireplay.api.util;

import lombok.experimental.UtilityClass;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.UUID;

@UtilityClass
public class CodecUtil {
    public static String ReadLengthPrefixed(ByteBuffer buf) {
        var size = buf.getInt();
        if (size < 0)
            throw new IllegalArgumentException("Invalid string size: " + size);
        else if (size == 0)
            return "";

        var bytes = new byte[size];


        buf.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void WriteLengthPrefixed(DataOutputStream out, String str) {
        if (str == null) {
            str = "";
        }

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

    public static byte[] ToFixedLengthBytes(BitSet bitSet, int byteLength) {
        byte[] raw = bitSet.toByteArray(); // May be shorter than byteLength
        byte[] result = new byte[byteLength];

        // Copy raw bytes into result (up to min length)
        System.arraycopy(raw, 0, result, 0, Math.min(raw.length, byteLength));

        return result;
    }

    public static PotionEffect ReadPotionEffect(ByteBuffer buf) {
        int typeId = buf.getInt();
        int duration = buf.getInt();
        int amplifier = buf.getInt();

        // particles, ambient (byte flag)
        byte flag = buf.get();
        boolean hasParticles = (flag & 0x01) != 0;
        boolean isAmbient = (flag & 0x02) != 0;

        PotionEffectType type = PotionEffectType.getById(typeId);
        if (type == null) {
            throw new IllegalArgumentException("Invalid potion effect type ID: " + typeId);
        }

        return new PotionEffect(type, duration, amplifier, isAmbient, hasParticles);
    }

    public static void WritePotionEffect(DataOutputStream out, PotionEffect effect) {
        try {
            out.writeInt(effect.getType().getId());
            out.writeInt(effect.getDuration());
            out.writeInt(effect.getAmplifier());

            // particles, ambient (byte flag)
            byte flag = 0;
            if (effect.hasParticles()) {
                flag |= 0x01;
            }
            if (effect.isAmbient()) {
                flag |= 0x02;
            }
            out.writeByte(flag);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
