package ar1hurgit.wheatarmor.command;

import ar1hurgit.wheatarmor.WheatArmor;
import ar1hurgit.wheatarmor.config.ArmorSet;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArmorCommand implements CommandExecutor, TabCompleter {

    private final WheatArmor plugin;

    public ArmorCommand(WheatArmor plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {return true;}

        Player player = (Player) sender;
        String commandName = command.getName().toLowerCase();
        String armorSetKey = commandName.replace("armor", "");

        ArmorSet set = plugin.getConfigManager().getArmorSet(armorSetKey);
        if (set == null) {
            player.sendMessage("§cArmor set not found.");
            return true;
        }

        if (args.length == 0) {
            plugin.getGuiManager().openShop(player, armorSetKey, false);
            return true;
        }

        if (args[0].equalsIgnoreCase("admin")) {
            if (!player.hasPermission("wheatarmor.admin")) {
                player.sendMessage("§cYou do not have permission.");
                return true;
            }

            if (args.length == 1) {
                plugin.getGuiManager().openShop(player, armorSetKey, true);
                return true;
            }

            if (args.length >= 4 && args[1].equalsIgnoreCase("give")) {
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found.");
                    return true;
                }

                String pieceName = args[3].toLowerCase();
                ArmorSet.ArmorPiece piece = null;

                switch (pieceName) {
                    case "head":  piece = set.getHead(); break;
                    case "chest": piece = set.getChest(); break;
                    case "legs":  piece = set.getLegs(); break;
                    case "boots": piece = set.getBoots(); break;
                    default:
                        player.sendMessage("§cInvalid armor piece. Use: head, chest, legs, boots");
                        return true;
                }

                plugin.getGuiManager().giveItem(target, piece);
                player.sendMessage("§aGave " + piece.getName() + " to " + target.getName());
                target.sendMessage("§aYou received " + piece.getName() + "!");
                return true;
            }
        }

        player.sendMessage("§cUsage: /" + commandName + " [admin] / " + commandName + " admin give <player> <piece>");
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (!sender.hasPermission("wheatarmor.admin")) return new ArrayList<>();
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if ("admin".startsWith(args[0].toLowerCase())) completions.add("admin");
        }

        else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            if ("give".startsWith(args[1].toLowerCase())) completions.add("give");
        }

        else if (args.length == 3 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("give")) {
            return null;
        }

        else if (args.length == 4 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("give")) {
            List<String> pieces = Arrays.asList("head", "chest", "legs", "boots");
            for (String p : pieces) {
                if (p.startsWith(args[3].toLowerCase())) completions.add(p);
            }
        }

        return completions;
    }
}
