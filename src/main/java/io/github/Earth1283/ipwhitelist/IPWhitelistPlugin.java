package io.github.Earth1283.ipwhitelist;

import org.bukkit.plugin.java.JavaPlugin;
import java.sql.SQLException;

public class IPWhitelistPlugin extends JavaPlugin {
    private DatabaseManager databaseManager;
    private ConfirmationManager confirmationManager;

    @Override
    public void onEnable() {
        // Load Config
        saveDefaultConfig();

        // Initialize Database
        databaseManager = new DatabaseManager(getDataFolder().getAbsolutePath(), getLogger());
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            databaseManager.init();
        } catch (SQLException e) {
            getLogger().severe("Failed to initialize database! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        confirmationManager = new ConfirmationManager();

        // Register Listeners
        getServer().getPluginManager().registerEvents(new ConnectionListener(this, databaseManager), this);

        // Register Commands
        getCommand("ipwhitelist").setExecutor(new WhitelistCommand(this, databaseManager, confirmationManager));

        getLogger().info("IPWhitelist enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("IPWhitelist disabled.");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
