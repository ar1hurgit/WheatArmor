package ar1hurgit.wheatarmor.config;

import org.bukkit.Material;
import java.util.List;

public class ArmorSet {
    private final String name;
    private final String fragmentName;
    private final Material fragmentMaterial;
    private final int fragmentModelData;
    private final List<String> fragmentLore;
    private final ArmorPiece head;
    private final ArmorPiece chest;
    private final ArmorPiece legs;
    private final ArmorPiece boots;

    public ArmorSet(String name, String fragmentName, Material fragmentMaterial, int fragmentModelData,
            List<String> fragmentLore, ArmorPiece head, ArmorPiece chest, ArmorPiece legs, ArmorPiece boots) {
        this.name = name;
        this.fragmentName = fragmentName;
        this.fragmentMaterial = fragmentMaterial;
        this.fragmentModelData = fragmentModelData;
        this.fragmentLore = fragmentLore;
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.boots = boots;
    }

    public String getName() {
        return name;
    }

    public String getFragmentName() {
        return fragmentName;
    }

    public Material getFragmentMaterial() {
        return fragmentMaterial;
    }

    public int getFragmentModelData() {
        return fragmentModelData;
    }

    public List<String> getFragmentLore() {
        return fragmentLore;
    }

    public ArmorPiece getHead() {return head;}

    public ArmorPiece getChest() {
        return chest;
    }

    public ArmorPiece getLegs() {
        return legs;
    }

    public ArmorPiece getBoots() {
        return boots;
    }

    public static class ArmorPiece {
        private final Material material;
        private final String name;
        private final List<String> lore;
        private final int price;
        private final int modelData;

        public ArmorPiece(Material material, String name, List<String> lore, int price, int modelData) {
            this.material = material;
            this.name = name;
            this.lore = lore;
            this.price = price;
            this.modelData = modelData;
        }

        public Material getMaterial() {
            return material;
        }

        public String getName() {
            return name;
        }

        public List<String> getLore() {
            return lore;
        }

        public int getPrice() {
            return price;
        }

        public int getModelData() {
            return modelData;
        }
    }
}
