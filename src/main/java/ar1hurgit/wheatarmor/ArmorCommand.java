package ar1hurgit.wheatarmor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ArmorCommand implements CommandExecutor {
    private final WheatArmor plugin;

    public ArmorCommand(WheatArmor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        // Handle specific shop commands
        if (label.equalsIgnoreCase("wheatarmor")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
                handleAdmin(player, args, "wheat");
            } else {
                plugin.getGuiManager().openShop(player, "wheat");
            }
            return true;
        } else if (label.equalsIgnoreCase("woodarmor")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
                handleAdmin(player, args, "wood");
            } else {
                plugin.getGuiManager().openShop(player, "wood");
            }
            return true;
        } else if (label.equalsIgnoreCase("cavearmor")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
                handleAdmin(player, args, "cave");
            } else {
                plugin.getGuiManager().openShop(player, "cave");
            }
            return true;
        }

        return false;
    }

    private void handleAdmin(Player player, String[] args, String setKey) {
        if (!player.hasPermission("wheatarmor.admin")) {
            player.sendMessage("§cYou don't have permission.");
            return;
        }

        if (args.length < 3) {
            player.sendMessage("§cUsage: /" + setKey + "armor admin give <player> <head|chest|legs|boots>");
            return;
        }

        if (args[1].equalsIgnoreCase("give")) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                player.sendMessage("§cPlayer not found.");
                return;
            }

            if (args.length < 4) {
                player.sendMessage("§cSpecify piece: head, chest, legs, boots");
                return;
            }

            String pieceType = args[3].toLowerCase();
            ArmorSet set = plugin.getConfigManager().getArmorSet(setKey);

            if (set == null) {
                player.sendMessage("§cArmor set " + setKey + " not configured.");
                return;
            }

            ArmorSet.ArmorPiece piece = null;
            switch (pieceType) {
                case "head" -> piece = set.getHead();
                case "chest" -> piece = set.getChest();
                case "legs" -> piece = set.getLegs();
                case "boots" -> piece = set.getBoots();
                default -> {
                    player.sendMessage("§cInvalid piece. Use: head, chest, legs, boots");
                    return;
                }
            }

            // Give item using logic similar to Gui
            // Need a public method or just replicate creation logic?
            // Replicating for now or I can expose createDisplayItem in GuiManager but that
            // adds price lore.
            // I should make a helper in ArmorSet?
            // Actually, let's just make the item creation logic specific here or refactor.
            // For admin give, usually we want the item WITHOUT "Price" lore, or WITH?
            // Usually valid armor to wear.
            // The shop display item has "Price" and "Click to buy".
            // The actual item given in GuiManager `giveArmor` also has that?
            // checking GuiManager `giveArmor`... it adds lore from config.

            // Wait, GuiManager `giveArmor` code:
            /*
             * if (piece.getLore() != null) {
             * List<String> lore = new ArrayList<>();
             * for (String l : piece.getLore()) {
             * lore.add(l.replace("&", "§"));
             * }
             * meta.setLore(lore);
             * }
             */
            // This does NOT add Price info, which is good.
            // So I should duplicate this logic or move it to a util.
            // Moving to ArmorSet as helper method `createItem()` is best practice.

            /*
             * BUT I cannot edit ArmorSet now without another call. I'll just duplicate for
             * speed or use a quick static helper method inside this class.
             */

            plugin.getGuiManager().giveItem(target, piece); // I'll add this public method to GuiManager or simply
                                                            // implement here.

            player.sendMessage("§aGave " + piece.getName() + " to " + target.getName());
        }
    }
}
