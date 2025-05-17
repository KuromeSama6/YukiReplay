package moe.ku6.yukireplay.api.exception;

public class VersionMismatchException extends PlaybackLoadException {
    public VersionMismatchException(int currentVersion, int correctVersion) {
        super("Version number mismatch. Replays are not forward compatible. Version: %04X, expected: %04X".formatted(currentVersion, correctVersion));
    }
}
