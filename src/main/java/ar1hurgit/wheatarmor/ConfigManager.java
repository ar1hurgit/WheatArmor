package ar1hurgit.wheatarmor;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigManager {
    private final WheatArmor plugin;
    private final Map<String, ArmorSet> armorSets = new HashMap<>();
    private final Map<Material, DropInfo> blockDrops = new HashMap<>();

    public ConfigManager(WheatArmor plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        armorSets.clear();
        blockDrops.clear();

        // Load Armor Sets
        ConfigurationSection setsSection = config.getConfigurationSection("armor-sets");
        if (setsSection != null) {
            for (String key : setsSection.getKeys(false)) {
                ConfigurationSection setSection = setsSection.getConfigurationSection(key);
                if (setSection == null)
                    continue;

                String fragmentName = setSection.getString("fragment.name", "&fFragment");
                Material fragmentMaterial = Material
                        .getMaterial(setSection.getString("fragment.material", "GHAST_TEAR"));
                if (fragmentMaterial == null)
                    fragmentMaterial = Material.GHAST_TEAR;

                int fragmentModelData = setSection.getInt("fragment.model-data", 0);
                List<String> fragmentLore = setSection.getStringList("fragment.lore");

                armorSets.put(key, new ArmorSet(
                        key,
                        fragmentName,
                        fragmentMaterial,
                        fragmentModelData,
                        fragmentLore,
                        loadPiece(setSection.getConfigurationSection("head"), Material.LEATHER_HELMET),
                        loadPiece(setSection.getConfigurationSection("chest"), Material.LEATHER_CHESTPLATE),
                        loadPiece(setSection.getConfigurationSection("legs"), Material.LEATHER_LEGGINGS),
                        loadPiece(setSection.getConfigurationSection("boots"), Material.LEATHER_BOOTS)));
            }
        }

        // Load Block Drops
        ConfigurationSection dropsSection = config.getConfigurationSection("drops");
        if (dropsSection != null) {
            for (String key : dropsSection.getKeys(false)) {
                ConfigurationSection dropSection = dropsSection.getConfigurationSection(key);
                if (dropSection == null)
                    continue;

                try {
                    Material mat = Material.valueOf(dropSection.getString("block", "STONE"));
                    double chance = dropSection.getDouble("chance", 10.0);
                    String fragmentType = dropSection.getString("fragment-type", "wheat"); // Refers to armor set key

                    blockDrops.put(mat, new DropInfo(chance, fragmentType));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in drops config: " + key);
                }
            }
        }
    }

    private ArmorSet.ArmorPiece loadPiece(ConfigurationSection section, Material defaultMat) {
        if (section == null)
            return new ArmorSet.ArmorPiece(defaultMat, "Unknown", new ArrayList<>(), 0, 0);

        Material mat = Material.getMaterial(section.getString("material", defaultMat.name()));
        if (mat == null)
            mat = defaultMat;

        return new ArmorSet.ArmorPiece(
                mat,
                section.getString("name", "&fArmor Piece"),
                section.getStringList("lore"),
                section.getInt("price", 10),
                section.getInt("model-data", 0));
    }

    public ArmorSet getArmorSet(String name) {
        return armorSets.get(name);
    }

    public Map<String, ArmorSet> getArmorSets() {
        return armorSets;
    }

    public DropInfo getDropInfo(Material material) {
        return blockDrops.get(material);
    }

    public static class DropInfo {
        private final double chance;
        private final String fragmentType;

        public DropInfo(double chance, String fragmentType) {
            this.chance = chance;
            this.fragmentType = fragmentType;
        }

        public double getChance() {
            return chance;
        }

        public String getFragmentType() {
            return fragmentType;
        }
    }
}
