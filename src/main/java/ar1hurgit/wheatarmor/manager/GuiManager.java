package ar1hurgit.wheatarmor.manager;

import ar1hurgit.wheatarmor.WheatArmor;
import ar1hurgit.wheatarmor.config.ArmorSet;
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

    public void openShop(Player player, String armorSetKey, boolean isAdmin) {
        ArmorSet set = plugin.getConfigManager().getArmorSet(armorSetKey);
        if (set == null) {
            player.sendMessage("§cShop not found: " + armorSetKey);
            return;
        }

        String title = isAdmin ? "§8Admin Shop: " + set.getName() : "§8Shop: " + set.getName();
        Inventory inv = Bukkit.createInventory(null, 27, title);

        inv.setItem(4, createFragmentDisplay(set, isAdmin));
        inv.setItem(10, createDisplayItem(set.getHead(), isAdmin));
        inv.setItem(12, createDisplayItem(set.getChest(), isAdmin));
        inv.setItem(14, createDisplayItem(set.getLegs(), isAdmin));
        inv.setItem(16, createDisplayItem(set.getBoots(), isAdmin));

        player.openInventory(inv);
    }

    private ItemStack createFragmentDisplay(ArmorSet set, boolean isAdmin) {
        ItemStack item = new ItemStack(set.getFragmentMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(set.getFragmentName().replace("&", "§"));
            meta.setCustomModelData(set.getFragmentModelData());

            List<String> lore = new ArrayList<>();
            if (set.getFragmentLore() != null) {
                for (String l : set.getFragmentLore())
                    lore.add(l.replace("&", "§"));
            }
            lore.add("");
            if (isAdmin) {
                lore.add("§c§lADMIN MODE");
                lore.add("§7Click to get fragments");
            } else {
                lore.add("§7Required currency");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createDisplayItem(ArmorSet.ArmorPiece piece, boolean isAdmin) {
        ItemStack item = new ItemStack(piece.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(piece.getName().replace("&", "§"));
            meta.setCustomModelData(piece.getModelData());

            List<String> lore = new ArrayList<>();
            if (piece.getLore() != null) {
                for (String l : piece.getLore())
                    lore.add(l.replace("&", "§"));
            }
            lore.add("");
            if (isAdmin) {
                lore.add("§c§lADMIN MODE");
                lore.add("§7Price: §aFREE");
            } else {
                lore.add("§7Price: §e" + piece.getPrice() + " Fragments");
            }
            lore.add("§aClick to buy");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        boolean isAdminShop = title.startsWith("§8Admin Shop: ");
        if (!title.startsWith("§8Shop: ") && !isAdminShop)
            return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        ArmorSet set = null;
        String prefix = isAdminShop ? "§8Admin Shop: " : "§8Shop: ";
        for (ArmorSet s : plugin.getConfigManager().getArmorSets().values()) {
            if (title.equals(prefix + s.getName()))
                set = s;
        }
        if (set == null)
            return;

        int slot = event.getSlot();

        // ADMIN FRAGMENT GIVE
        if (slot == 4 && isAdminShop) {
            ItemStack frag = new ItemStack(set.getFragmentMaterial());
            ItemMeta meta = frag.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(set.getFragmentName().replace("&", "§"));
                meta.setCustomModelData(set.getFragmentModelData());

                List<String> lore = new ArrayList<>();
                if (set.getFragmentLore() != null) {
                    for (String l : set.getFragmentLore()) {
                        lore.add(l.replace("&", "§"));
                    }
                }
                meta.setLore(lore);
                frag.setItemMeta(meta);
            }

            player.getInventory().addItem(frag);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            return;
        }

        ArmorSet.ArmorPiece piece = null;
        if (slot == 10)
            piece = set.getHead();
        else if (slot == 12)
            piece = set.getChest();
        else if (slot == 14)
            piece = set.getLegs();
        else if (slot == 16)
            piece = set.getBoots();

        if (piece == null)
            return;

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage("§cYour inventory is full!");
            return;
        }

        if (isAdminShop) {
            giveArmor(player, piece);
            return;
        }

        int price = piece.getPrice();
        if (hasFragments(player, set, price)) {
            removeFragments(player, set, price);
            giveArmor(player, piece);
        } else {
            player.sendMessage("§cNot enough fragments!");
        }
    }

    private boolean hasFragments(Player player, ArmorSet set, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isFragment(item, set))
                count += item.getAmount();
        }
        return count >= amount;
    }

    private void removeFragments(Player player, ArmorSet set, int amount) {
        int left = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isFragment(item, set)) {
                if (item.getAmount() <= left) {
                    left -= item.getAmount();
                    item.setAmount(0);
                } else {
                    item.setAmount(item.getAmount() - left);
                    left = 0;
                }
                if (left <= 0)
                    break;
            }
        }
    }

    private boolean isFragment(ItemStack item, ArmorSet set) {
        if (item.getType() != set.getFragmentMaterial())
            return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return false;
        if (meta.hasCustomModelData() && meta.getCustomModelData() == set.getFragmentModelData())
            return true;
        return meta.hasDisplayName() && meta.getDisplayName().equals(set.getFragmentName().replace("&", "§"));
    }

    public void giveItem(Player player, ArmorSet.ArmorPiece piece) {
        giveArmor(player, piece);
    }

    private void giveArmor(Player player, ArmorSet.ArmorPiece piece) {
        ItemStack item = new ItemStack(piece.getMaterial());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(piece.getName().replace("&", "§"));
        meta.setCustomModelData(piece.getModelData());

        if (piece.getLore() != null) {
            List<String> lore = new ArrayList<>();
            for (String l : piece.getLore())
                lore.add(l.replace("&", "§"));
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
        player.getInventory().addItem(item);
    }
}
