package moe.protasis.replay.command;

import lombok.Getter;
import moe.icegame.coreutils.classes.LintedCommand;
import moe.icegame.coreutils.classes.PlayerCommandListener;
import moe.protasis.replay.YukiReplay;
import moe.protasis.replay.packetwrapper.WrapperPlayServerNamedEntitySpawn;
import moe.protasis.replay.playback.Playback;
import moe.protasis.replay.replay.Replay;
import net.jitse.npclib.api.NPC;
import net.minecraft.server.v1_8_R3.EntityTracker;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
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
                debugReplay.setRecording(false);
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
            if (debugPlayback != null) debugPlayback.Close();

            try {
                debugPlayback = Playback.LoadFromDirectory("debug", player.getWorld());
                debugPlayback.AddViewer(player);
                debugPlayback.setPlaying(true);
                sender.sendMessage("playing");
            } catch (IOException e) {
                sender.sendMessage("§cIOException");
                e.printStackTrace();
            }

        }

        if (cmd.HasParam("-spawn")) {
            NPC npc = YukiReplay.getNpcLib().createNPC();
            npc.setLocation(player.getLocation());
            npc.create();
            npc.show(player);
        }

    }
}
