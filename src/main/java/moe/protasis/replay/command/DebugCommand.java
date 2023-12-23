package moe.protasis.replay.command;

import lombok.Getter;
import moe.icegame.coreutils.classes.LintedCommand;
import moe.icegame.coreutils.classes.PlayerCommandListener;
import moe.protasis.replay.playback.Playback;
import moe.protasis.replay.replay.Replay;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public class DebugCommand extends PlayerCommandListener.PlayerCommandHandler {
    @Getter
    private Replay debugReplay;
    @Getter
    private Playback debugPlayback;

    public DebugCommand() {
        super("replaydebug");
    }

    @Override
    public void Handle(CommandSender sender, LintedCommand cmd, boolean isPlayer, Player player) {
        if (!isPlayer) {
            sender.sendMessage("EACCES");
            return;
        }

        if (cmd.HasParam("-record")) {
            if (debugReplay == null) {
                debugReplay = new Replay();
                debugReplay.AddPlayer(player);
            }

            debugReplay.setRecording(!debugReplay.isRecording());
            if (debugReplay.isRecording()) sender.sendMessage("§anow recording");
            else sender.sendMessage("§cpaused");
        }

        if (cmd.HasParam("-save")) {
            try {
                debugReplay.Save("debug");
                sender.sendMessage(String.format("saved at frame %s", debugReplay.getFrame()));

            } catch (IOException e) {
                sender.sendMessage("§cIOException");
                e.printStackTrace();
            }
        }

        if (cmd.HasParam("-stop")) {
            debugReplay.setRecording(false);
            debugReplay = null;
            sender.sendMessage("stopped");
        }

        if (cmd.HasParam("-play")) {
            try {
                debugPlayback = Playback.LoadFromDirectory("debug");
            } catch (IOException e) {
                sender.sendMessage("§cIOException");
                e.printStackTrace();
            }

        }

    }
}
