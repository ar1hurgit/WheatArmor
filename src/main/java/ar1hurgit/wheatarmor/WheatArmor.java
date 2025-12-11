package ar1hurgit.wheatarmor;

import ar1hurgit.wheatarmor.command.ArmorCommand;
import ar1hurgit.wheatarmor.listener.DropListener;
import ar1hurgit.wheatarmor.manager.ConfigManager;
import ar1hurgit.wheatarmor.manager.GuiManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class WheatArmor extends JavaPlugin {
    private ConfigManager configManager;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        this.guiManager = new GuiManager(this);

        // Register commands
        ArmorCommand armorCommand = new ArmorCommand(this);
        getCommand("wheatarmor").setExecutor(armorCommand);
        getCommand("woodarmor").setExecutor(armorCommand);
        getCommand("cavearmor").setExecutor(armorCommand);
        getCommand("richearmor").setExecutor(armorCommand);
        getCommand("sagearmor").setExecutor(armorCommand);

        // Register listeners
        getServer().getPluginManager().registerEvents(new DropListener(this), this);
        getServer().getPluginManager().registerEvents(guiManager, this);
        getServer().getPluginManager().registerEvents(new ar1hurgit.wheatarmor.listener.ArmorChangeListener(this),
                this);

        getLogger().info("WheatArmor enabled successfully");
    }

    @Override
    public void onDisable() {
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
