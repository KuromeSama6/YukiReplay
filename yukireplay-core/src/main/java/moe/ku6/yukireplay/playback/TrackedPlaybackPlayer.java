package moe.ku6.yukireplay.playback;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import moe.ku6.yukireplay.YukiReplay;
import moe.ku6.yukireplay.api.YukiReplayAPI;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionAddPlayer;
import moe.ku6.yukireplay.api.codec.impl.player.InstructionPlayerInventory;
import moe.ku6.yukireplay.api.nms.GameProfilePropertyWrapper;
import moe.ku6.yukireplay.api.nms.IClientPlayer;
import moe.ku6.yukireplay.api.nms.IGameProfile;
import moe.ku6.yukireplay.api.playback.EntityLifetime;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TrackedPlaybackPlayer extends TrackedPlaybackEntity implements IPlaybackPlayer {
    private final UUID uuid, originalUuid;
    private final String name;
    private final IGameProfile gameProfile;
    private final IClientPlayer clientPlayer;
    private final Map<InstructionPlayerInventory.Slot, ItemStack> inventory = new HashMap<>();

    public TrackedPlaybackPlayer(ReplayPlayback playback, InstructionAddPlayer instruction) {
        super(playback, instruction.getTrackerId());
//        uuid = instruction.getUuid();
        originalUuid = instruction.getUuid();
        uuid = UUID.randomUUID();
        name = instruction.getName();

        gameProfile = YukiReplay.getInstance().getVersionAdaptor().CreateGameProfile(uuid, name);
        gameProfile.SetProperty("textures", new GameProfilePropertyWrapper("textures", instruction.getSkinValue(), instruction.getSkinSignature()));
        clientPlayer = YukiReplay.getInstance().getVersionAdaptor().CreateClientPlayer(playback.GetWorld(), gameProfile);
    }

    @Override
    public UUID GetUUID() {
        return originalUuid;
    }

    @Override
    public String GetName() {
        return name;
    }

    @Override
    public IClientPlayer GetClientPlayer() {
        return clientPlayer;
    }

    @Override
    public void SetSkin(String value, String signature) {
        clientPlayer.SetSkin(value, signature);
        viewers.forEach(clientPlayer::ForceRefresh);
    }

    @Override
    public ItemStack GetInventory(InstructionPlayerInventory.Slot slot) {
        return inventory.get(slot);
    }

    @Override
    public void SetInventory(InstructionPlayerInventory.Slot slot, ItemStack item) {
        if (item == null) {
            inventory.remove(slot);
        } else {
            inventory.put(slot, item);
        }
    }

    @Override
    public void RefreshInventory() {
        var equipments = new ArrayList<Equipment>();
        for (var slot : inventory.keySet()) {
            ItemStack item = GetInventory(slot);
            if (item != null) {
                equipments.add(new Equipment(slot.getEquipmentSlot(), SpigotConversionUtil.fromBukkitItemStack(item)));
            }
        }

        if (equipments.isEmpty()) {
            return; // No equipment to send
        }

        var packet = new WrapperPlayServerEntityEquipment(clientPlayer.GetEntityId(), equipments);
        playback.GetViewers().forEach(c -> PacketEvents.getAPI().getPlayerManager().sendPacket(c, packet));
    }

    @Override
    protected void SpawnOnClient(Player viewer) {
        clientPlayer.SpawnTo(viewer);
        Bukkit.getScheduler().scheduleSyncDelayedTask(YukiReplayAPI.Get().GetProvidingPlugin(), this::RefreshInventory, 2);
    }

    @Override
    protected void DespawnOnClient(Player viewer) {
        clientPlayer.DespawnFrom(viewer);
    }

    @Override
    public Location GetLocation() {
        return clientPlayer.GetLocation();
    }

    @Override
    public void SetLocation(Location location) {
        clientPlayer.SetLocation(location);
    }
}
