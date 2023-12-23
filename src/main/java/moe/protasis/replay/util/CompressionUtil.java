package moe.protasis.replay.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.nio.charset.StandardCharsets;

public class CompressionUtil {
    public static byte[] CompressToByteArray(String input) throws IOException{
        // Convert string to bytes
        byte[] inputData = input.getBytes(StandardCharsets.UTF_8);

        // Create a ByteArrayOutputStream to hold the compressed data
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(inputData.length);

        // Create a GZIPOutputStream to compress the data
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
            gzipOutputStream.write(inputData);
        }

        // Get the compressed data as byte[]
        return outputStream.toByteArray();
    }

    public static String DecompressToString(byte[] compressedInput) {
        try {
            // Create a GZIPInputStream to decompress the data
            try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(compressedInput))) {
                // Read the decompressed data
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipInputStream.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
                // Convert the decompressed data to a string
                return baos.toString(StandardCharsets.UTF_8.name());
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
            return null;
        }
    }
}
