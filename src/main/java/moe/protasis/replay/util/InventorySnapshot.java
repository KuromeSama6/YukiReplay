package moe.protasis.replay.util;

import com.google.gson.JsonObject;
import lombok.Getter;
import moe.protasis.replay.npc.PlayerNPC;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.state.NPCSlot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventorySnapshot {
    @Getter
    private Material mainhand, helmet, body, leggings, boots;

    public InventorySnapshot(Player player) {
        PlayerInventory inv = player.getInventory();

        mainhand = SafeGetMaterial(player.getItemInHand());
        helmet = SafeGetMaterial(inv.getHelmet());
        body = SafeGetMaterial(inv.getChestplate());
        leggings = SafeGetMaterial(inv.getLeggings());
        boots = SafeGetMaterial(inv.getBoots());
    }

    public InventorySnapshot(JsonObject data) {
        mainhand = Material.getMaterial(data.get("mainhand").getAsString());
        helmet = Material.getMaterial(data.get("helmet").getAsString());
        body = Material.getMaterial(data.get("body").getAsString());
        leggings = Material.getMaterial(data.get("leggings").getAsString());
        boots = Material.getMaterial(data.get("boots").getAsString());
    }

    public JsonObject Serialize() {
        return new JsonObjectBuilder()
                .put("mainhand", mainhand.toString())
                .put("helmet", helmet.toString())
                .put("body", body.toString())
                .put("leggings", leggings.toString())
                .put("boots", leggings.toString())
                .finish();
    }

    public void Apply(PlayerNPC npc) {
        NPC nativeNpc = npc.getNpc();

        nativeNpc.setItem(NPCSlot.MAINHAND, new ItemStack(mainhand, 1));
        nativeNpc.setItem(NPCSlot.HELMET, new ItemStack(helmet, 1));
        nativeNpc.setItem(NPCSlot.CHESTPLATE, new ItemStack(body, 1));
        nativeNpc.setItem(NPCSlot.LEGGINGS, new ItemStack(leggings, 1));
        nativeNpc.setItem(NPCSlot.BOOTS, new ItemStack(boots, 1));
    }

    @Override
    public String toString() {
        return "InventorySnapshot{" +
                "mainhand=" + mainhand +
                ", helmet=" + helmet +
                ", body=" + body +
                ", leggings=" + leggings +
                ", boots=" + boots +
                '}';
    }

    private static Material SafeGetMaterial(ItemStack item) {
        return item == null ? Material.AIR : item.getType();
    }

}
