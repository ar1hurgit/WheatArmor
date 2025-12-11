package ar1hurgit.wheatarmor.listener;

import ar1hurgit.wheatarmor.WheatArmor;
import ar1hurgit.wheatarmor.config.ArmorSet;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class ArmorChangeListener implements Listener {

    private final WheatArmor plugin;
    private final Map<Player, String> lastState = new HashMap<>();

    public ArmorChangeListener(WheatArmor plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        checkArmor(event.getPlayer(), true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        // Only react if clicked slot is an armor slot
        if (event.getSlot() < 36 || event.getSlot() > 39) return;

        Player p = (Player) event.getWhoClicked();
        Bukkit.getScheduler().runTask(plugin, () -> checkArmor(p, false));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Only react if the player right-clicks with armor in hand
        if (!event.getAction().name().contains("RIGHT_CLICK")) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        Material type = item.getType();
        if (!isArmor(type)) return;

        Bukkit.getScheduler().runTask(plugin, () -> checkArmor(event.getPlayer(), false));
    }

    private boolean isArmor(Material m) {
        return m.name().endsWith("_HELMET") ||
                m.name().endsWith("_CHESTPLATE") ||
                m.name().endsWith("_LEGGINGS") ||
                m.name().endsWith("_BOOTS");
    }

    private void checkArmor(Player player, boolean forceSend) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        Map<String, Integer> counts = new HashMap<>();

        for (ItemStack item : armor) {
            if (item == null || item.getType() == Material.AIR) continue;

            for (Map.Entry<String, ArmorSet> entry : plugin.getConfigManager().getArmorSets().entrySet()) {
                if (isPiece(item, entry.getValue())) {
                    counts.put(entry.getKey(), counts.getOrDefault(entry.getKey(), 0) + 1);
                    break;
                }
            }
        }

        // Build current state string
        StringBuilder sb = new StringBuilder();
        for (String key : plugin.getConfigManager().getArmorSets().keySet()) {
            int c = counts.getOrDefault(key, 0);
            sb.append("[").append(key).append(" ,").append(c).append("] ");
        }
        String newState = sb.toString().trim();

        // Prevent spam: only notify if state changed
        String oldState = lastState.get(player);
        if (!forceSend && newState.equals(oldState)) return;

        lastState.put(player, newState);

        player.sendMessage(newState);
        // TODO: connect to other plugin
    }

    private boolean isPiece(ItemStack item, ArmorSet set) {
        return match(item, set.getHead()) ||
                match(item, set.getChest()) ||
                match(item, set.getLegs()) ||
                match(item, set.getBoots());
    }

    private boolean match(ItemStack item, ArmorSet.ArmorPiece piece) {
        if (item.getType() != piece.getMaterial()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.hasCustomModelData() && meta.getCustomModelData() == piece.getModelData();
    }
}
