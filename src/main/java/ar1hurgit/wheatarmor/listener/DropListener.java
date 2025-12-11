package ar1hurgit.wheatarmor.listener;

import ar1hurgit.wheatarmor.WheatArmor;
import ar1hurgit.wheatarmor.config.ArmorSet;
import ar1hurgit.wheatarmor.manager.ConfigManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class DropListener implements Listener {
    private final WheatArmor plugin;
    private final Random random = new Random();

    public DropListener(WheatArmor plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        ConfigManager.DropInfo dropInfo = plugin.getConfigManager().getDropInfo(event.getBlock().getType());

        if (dropInfo != null) {
            double roll = random.nextDouble() * 100;
            if (roll <= dropInfo.getChance()) {
                ArmorSet set = plugin.getConfigManager().getArmorSet(dropInfo.getFragmentType());
                if (set != null) {
                    ItemStack fragment = createFragment(set);
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), fragment);
                }
            }
        }
    }

    private ItemStack createFragment(ArmorSet set) {
        ItemStack item = new ItemStack(set.getFragmentMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(set.getFragmentName().replace("&", "§"));
            meta.setCustomModelData(set.getFragmentModelData());
            if (set.getFragmentLore() != null) {
                meta.setLore(set.getFragmentLore().stream().map(s -> s.replace("&", "§")).toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
