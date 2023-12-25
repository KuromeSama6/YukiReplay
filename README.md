# YukiReplay

中文： `README_CN.md`

---

YukiReplay is a lightweight replay API plugin built and tested on version 1.8.8, and theoretically supports versions 1.8.8 through 1.16.5. The underlying NPC provider that YukiReplay uses **npclib-api** by JitseB. You can find it here on GitHub:
https://github.com/JitseB/NPCLib .

Note that this plugin is an API plugin - it has no functionality on its own, nor will it record player actions on its own. If you are not planning to implement this in your own plugin, there are plenty of other replay plugins available on the market.

**This plugin is still in development.** You will see placeholders in this readme document. Releases will be accessible through Jitpack. You are also welcome to build the plugin yourself.

## Installation

Add the following to your maven dependency:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```xml
<dependency>
    <groupId>com.github.kuromesama6</groupId>
    <artifactId>YukiReplay</artifactId>
    <version>...</version>
    <scope>provided</scope>
</dependency>
```

Add dependencies in your plugin.yml accordingly. Drop the plugin jar file in your server's `plugins` directory, and you're all set.


## Getting Started
### Replays

Replays are "cameras" that captures player action, and provides the ability to save them to either a file or as a byte array.

To start a replay, start with creating a `Replay` object.

```java
import moe.protasis.replay.replay.Replay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MyPlugin extends JavaPlugin {
    public void startReplay(List<Player> targets) {
        // Create a replay object
        Replay replay = new Replay();

        // Add targets to this replay.
        // Only targets and their respective actions will be captured by the replay.
        for (Player player : targets)
            replay.AddPlayer(player);

        // Start the capture when you're ready.
        replay.setRecording(true);

        // End the capture when you have finished.
        replay.setRecording(false);

        // Save the capture to either a file, or as a byte array, so you can implement your own saving logic.

        // Saves the capture as a byte array.
        byte[] capture = replay.SaveToBytes();

        // Saves the capture to a file.
        replay.Save(new File("your/path/replay"));

        // Saves the capture to the "replays" directory under the YukiReplay plugin directory in your server. Saved replays have a .repl extension.
        replay.Save("MyCapture");

        // Saves the capture to the replays directory, using the current timestamp as its file name.
        replay.Save();

        // Close the replay when you're done. Any schedulers and timers are freed.
        replay.Close();

    }
}
```

The saved byte array/file can be used later for playback.

### Playback

Playback plays a certain replay to designated players of your choice. Start by creating a `Playback` object.

Because YukiReplay uses the GSON library to save replay data as JSON, the `Playback` object has a constructor that accepts a JsonObject representation of the replay. To load a replay from a file, call `Playback#LoadFromDirectory` (do not included the file extension in the replay name) to load a replay from the replay directory, or `Playback#LoadFromFile` to load a replay from a file.

```java
import moe.protasis.replay.playback.Playback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MyPlugin extends JavaPlugin {
    public void startPlayback(File file, Player... viewers) {
        // Load a playback from a file. A world must be specified, in which the playback will occur.
        Playback playback = Playback.LoadFromFile(new File("your/path"), Bukkit.getWorld("world"));

        // Add viewers to this playback before it starts.
        // NPCs in the playback are client-side, so only viewers will be able to see them.
        for (Player viewer : viewers)
            playback.AddViewer(viewer);

        // Start the playback when you're ready.
        // Playback starts from the first meaningful frame (if the first frame in the replay is frame 75, it will start from there, instead of starting from frame 0 and waiting 75 frames.
        playback.setPlaying(true);
        
        // Playback automatically pauses when it reaches the end. You can also pause it by calling setPlaying(false).
        playback.setPlaying(false);
        
        // Close the playback and release related schedulers and timers when you're finished. Calling Close() also removes all client-side NPCs from all viewers. Once closed, a playback cannot be used again. Calling setPlaying(true) leads to undefined behavior.
        playback.Close();
    }
}
```

## Features and TODOs
- [x] Spawning and despawning
- [x] Player movement and rotation
- [x] Arm animation
- [x] Hurt animation
- [ ] Held items
- [ ] Pots
- [ ] Block place and break
- [ ] Player skins
- [ ] Player names and stats under name