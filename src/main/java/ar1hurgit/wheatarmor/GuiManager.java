package ar1hurgit.wheatarmor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GuiManager implements Listener {
    private final WheatArmor plugin;

    public GuiManager(WheatArmor plugin) {
        this.plugin = plugin;
    }

    public void openShop(Player player, String armorSetKey) {
        ArmorSet set = plugin.getConfigManager().getArmorSet(armorSetKey);
        if (set == null) {
            player.sendMessage("§cShop not found: " + armorSetKey);
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "§8Shop: " + set.getName());

        // Slots: 10 (Head), 12 (Chest), 14 (Legs), 16 (Boots)
        inv.setItem(10, createDisplayItem(set.getHead()));
        inv.setItem(12, createDisplayItem(set.getChest()));
        inv.setItem(14, createDisplayItem(set.getLegs()));
        inv.setItem(16, createDisplayItem(set.getBoots()));

        player.openInventory(inv);
    }

    public void giveItem(Player player, ArmorSet.ArmorPiece piece) {
        giveArmor(player, piece);
    }

    private ItemStack createDisplayItem(ArmorSet.ArmorPiece piece) {
        ItemStack item = new ItemStack(piece.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(piece.getName().replace("&", "§"));
            meta.setCustomModelData(piece.getModelData());
            List<String> lore = new ArrayList<>();
            if (piece.getLore() != null) {
                for (String l : piece.getLore()) {
                    lore.add(l.replace("&", "§"));
                }
            }
            lore.add("");
            lore.add("§7Price: §e" + piece.getPrice() + " Fragments");
            lore.add("§aClick to buy!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        if (event.getView().getTitle().startsWith("§8Shop: ")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR)
                return;

            String title = event.getView().getTitle();
            ArmorSet foundSet = null;

            // Find the set based on title
            for (ArmorSet set : plugin.getConfigManager().getArmorSets().values()) {
                if (title.equals("§8Shop: " + set.getName())) {
                    foundSet = set;
                    break;
                }
            }

            if (foundSet == null)
                return;

            ArmorSet.ArmorPiece targetPiece = null;
            int slot = event.getSlot();
            if (slot == 10)
                targetPiece = foundSet.getHead();
            else if (slot == 12)
                targetPiece = foundSet.getChest();
            else if (slot == 14)
                targetPiece = foundSet.getLegs();
            else if (slot == 16)
                targetPiece = foundSet.getBoots();

            if (targetPiece == null)
                return;

            // Transaction Logic
            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage("§cYour inventory is full!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                return;
            }

            int price = targetPiece.getPrice();
            if (hasFragments(player, foundSet, price)) {
                removeFragments(player, foundSet, price);
                giveArmor(player, targetPiece);
                player.sendMessage("§aYou purchased " + targetPiece.getName() + "!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            } else {
                player.sendMessage("§cYou don't have enough fragments!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            }
        }
    }

    private boolean hasFragments(Player player, ArmorSet set, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isFragment(item, set)) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }

    private void removeFragments(Player player, ArmorSet set, int amount) {
        int leftToRemove = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isFragment(item, set)) {
                if (item.getAmount() <= leftToRemove) {
                    leftToRemove -= item.getAmount();
                    item.setAmount(0);
                } else {
                    item.setAmount(item.getAmount() - leftToRemove);
                    leftToRemove = 0;
                }
                if (leftToRemove <= 0)
                    break;
            }
        }
    }

    private boolean isFragment(ItemStack item, ArmorSet set) {
        if (item.getType() == set.getFragmentMaterial()) {
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasCustomModelData() && meta.getCustomModelData() == set.getFragmentModelData()) {
                    return true;
                }
                if (meta.hasDisplayName() && meta.getDisplayName().equals(set.getFragmentName().replace("&", "§"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void giveArmor(Player player, ArmorSet.ArmorPiece piece) {
        ItemStack item = new ItemStack(piece.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(piece.getName().replace("&", "§"));
            meta.setCustomModelData(piece.getModelData());
            if (piece.getLore() != null) {
                List<String> lore = new ArrayList<>();
                for (String l : piece.getLore()) {
                    lore.add(l.replace("&", "§"));
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        player.getInventory().addItem(item);
    }
}
