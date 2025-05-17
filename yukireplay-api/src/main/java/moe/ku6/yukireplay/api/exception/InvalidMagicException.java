package moe.ku6.yukireplay.api.exception;

public class InvalidMagicException extends PlaybackLoadException {
    public InvalidMagicException() {
        super("Invalid replay magic number");
    }
}
