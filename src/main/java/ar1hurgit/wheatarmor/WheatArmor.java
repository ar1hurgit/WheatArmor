package ar1hurgit.wheatarmor;

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

        // Register listeners
        getServer().getPluginManager().registerEvents(new DropListener(this), this);
        getServer().getPluginManager().registerEvents(guiManager, this);

        getLogger().info("WheatArmor enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }
}
