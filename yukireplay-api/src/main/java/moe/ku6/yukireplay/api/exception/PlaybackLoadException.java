package moe.ku6.yukireplay.api.exception;

public class PlaybackLoadException extends Exception {
    public PlaybackLoadException() {
        super();
    }

    public PlaybackLoadException(String message) {
        super(message);
    }

    public PlaybackLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlaybackLoadException(Throwable cause) {
        super(cause);
    }

    protected PlaybackLoadException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
