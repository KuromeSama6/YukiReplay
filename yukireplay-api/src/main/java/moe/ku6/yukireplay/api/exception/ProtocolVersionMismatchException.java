package moe.ku6.yukireplay.api.exception;

public class ProtocolVersionMismatchException extends PlaybackLoadException {
    public ProtocolVersionMismatchException(String currentVersion, String correctVersion) {
        super("Server protocol version mismatch. Version: %04X, expected: %04X".formatted(currentVersion, correctVersion));
    }
}
