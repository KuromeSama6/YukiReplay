package moe.protasis.replay.util;

public class PacketUtil {
    public static int CompressAngle(float angle) {
        return (int) (angle * 256.0F / 360.0F);
    }
}
