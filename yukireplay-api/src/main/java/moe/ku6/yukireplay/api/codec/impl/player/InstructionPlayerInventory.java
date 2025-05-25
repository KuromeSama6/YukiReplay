package moe.ku6.yukireplay.api.codec.impl.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ku6.yukireplay.api.YukiReplayAPI;
import moe.ku6.yukireplay.api.codec.InstructionType;
import moe.ku6.yukireplay.api.codec.impl.PlayerInstruction;
import moe.ku6.yukireplay.api.playback.IPlayback;
import moe.ku6.yukireplay.api.playback.IPlaybackPlayer;
import moe.ku6.yukireplay.api.util.CodecUtil;
import org.bukkit.inventory.ItemStack;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class InstructionPlayerInventory extends PlayerInstruction {
    @Getter
    private final Slot slot;
    @Getter
    private final ItemStack item;

    public InstructionPlayerInventory(ByteBuffer buf) {
        super(buf);
        var adapter = YukiReplayAPI.Get().GetVersionAdaptor();

        slot = Slot.values()[buf.get()];
        item = adapter.DeserializeItemStack(CodecUtil.ReadLengthPrefixed(buf));
    }

    public InstructionPlayerInventory(int trackerId, Slot slot, ItemStack item) {
        super(trackerId);
        this.slot = slot;
        this.item = item;
    }

    @Override
    public InstructionType GetType() {
        return InstructionType.PLAYER_INVENTORY;
    }

    @Override
    public void Serialize(DataOutputStream out) throws IOException {
        super.Serialize(out);
        var adapter = YukiReplayAPI.Get().GetVersionAdaptor();
        out.write(slot.ordinal());
        CodecUtil.WriteLengthPrefixed(out, adapter.SerializeItemStack(item));
    }

    @Override
    public void Apply(IPlayback playback) {
        IPlaybackPlayer player = playback.GetTracked(trackerId);

        var equipments = List.of(new Equipment(slot.equipmentSlot, SpigotConversionUtil.fromBukkitItemStack(item)));

        var packet = new WrapperPlayServerEntityEquipment(player.GetClientPlayer().GetEntityId(), equipments);

        playback.GetViewers().forEach(c -> PacketEvents.getAPI().getPlayerManager().sendPacket(c, packet));
    }

    @AllArgsConstructor
    public enum Slot {
        MAINHAND(EquipmentSlot.MAIN_HAND),
        HELMET(EquipmentSlot.HELMET),
        CHESTPLATE(EquipmentSlot.CHEST_PLATE),
        LEGGINGS(EquipmentSlot.LEGGINGS),
        BOOTS(EquipmentSlot.BOOTS);

        private final EquipmentSlot equipmentSlot;
    }
}
