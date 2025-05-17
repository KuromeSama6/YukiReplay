package moe.ku6.yukireplay;

import lombok.Getter;
import moe.ku6.yukireplay.api.YukiReplayAPI;
import moe.ku6.yukireplay.api.nms.IVersionAdaptor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class YukiReplay extends JavaPlugin {
    @Getter
    private static YukiReplay instance;
    @Getter
    private YukiReplayAPIImpl api;
    @Getter
    private IVersionAdaptor versionAdaptor;
    @Getter
    private Logger log;

    @Override
    public void onEnable() {
        instance = this;
        api = new YukiReplayAPIImpl(this);
        YukiReplayAPI.Init(api);

        log = getLogger();

        versionAdaptor = IVersionAdaptor.Get();
        log.info("Version adaptor set to " + versionAdaptor.getClass().getName());
    }
}
