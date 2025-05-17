package moe.ku6.yukireplay.api.recorder;

import lombok.*;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecorderOptions {
    /**
     * The initial size of the recording buffer. Each tick, at least 2 instructions are added to the buffer. This is not a cap to the buffer size, but choosing an appropriate value can help reduce allocations and improve performance.
     */
    @Builder.Default
    private int initialSize = 8192;
    /**
     * Whether to automatically add loaded chunks to a recording upon creation and start.
     */
    @Builder.Default
    private boolean autoAddLoadedChunks = true;
    /**
     * Whether to automatically add players to a recording when they enter the chunks being captured by the recording.
     */
    @Builder.Default
    private boolean autoAddPlayers = true;
    /**
     * Whether to automatically remove players from a recording when they leave the chunks being captured by the recording.
     */
    @Builder.Default
    private boolean autoRemovePlayers = true;
    /**
     * Whether to automatically remove players from a recording when they go offline.
     */
    @Builder.Default
    private boolean autoRemoveOfflinePlayers = true;

    public static RecorderOptions Default() {
        return RecorderOptions.builder().build();
    }
}
