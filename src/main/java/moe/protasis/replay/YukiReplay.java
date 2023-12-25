package moe.protasis.replay;

import lombok.Getter;
import moe.icegame.coreutils.classes.PlayerCommandListener;
import moe.protasis.replay.command.DebugCommand;
import net.jitse.npclib.NPCLib;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.NPC;
import org.bukkit.plugin.java.JavaPlugin;

public class YukiReplay extends JavaPlugin {
    public static final int REPLAY_FORMAT_VERSION = 1;
    public static final int ENTITY_ID_OFFSET = 1000;
    @Getter private static YukiReplay instance;
    @Getter private static NPCLib npcLib;

    private PlayerCommandListener commandListener;

    @Override
    public void onEnable() {
        instance = this;
        npcLib = new NPCLib(this);

        commandListener = new PlayerCommandListener(this);
        commandListener.RegisterHandler(new DebugCommand());
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandListener.HandlerExisting(sender, command, label, args);
    }

    public YukiReplayAPI GetAPI(JavaPlugin plugin) {
        return new YukiReplayAPI(plugin);
    }

}
