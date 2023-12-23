package moe.protasis.replay;

import lombok.Getter;
import moe.icegame.coreutils.classes.PlayerCommandListener;
import moe.protasis.replay.command.DebugCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class YukiReplay extends JavaPlugin {
    @Getter private static YukiReplay instance;

    private PlayerCommandListener commandListener;

    @Override
    public void onEnable() {
        instance = this;

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
