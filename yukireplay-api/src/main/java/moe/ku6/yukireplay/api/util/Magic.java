package moe.ku6.yukireplay.api.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Magic {
    public static final short FORMAT_VERSION = 0x011a;
    //magic = 0d 00 07 21
    public static final byte[] FORMAT_MAGIC = new byte[]{0x0d, 0x00, 0x07, 0x21};
}
