package moe.ku6.yukireplay.api;

public class YukiReplayAPI {
    private static IYukiReplayAPI instance;

    public static IYukiReplayAPI Get() {
        if (instance == null)
            throw new IllegalStateException("YukiReplayAPI not initialized");

        return instance;
    }

    public static void Init(IYukiReplayAPI api) {
        if (instance != null)
            throw new IllegalStateException("YukiReplayAPI already initialized");

        instance = api;
    }
}
