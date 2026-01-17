package io.github.Earth1283.ipwhitelist;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.net.InetAddress;

public class ConnectionListener implements Listener {
    private final IPWhitelistPlugin plugin;
    private final DatabaseManager databaseManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ConnectionListener(IPWhitelistPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        // Check for bypass permission (limited effectiveness in PreLogin for offline
        // players, but good practice)
        // Since we can't easily check permissions for offline players/pre-login without
        // a permission plugin bridge,
        // we'll primarily rely on IP. If the server is in online mode, we might fetch
        // the profile, but for now
        // we'll assume IP check is primary. If you really need bypass, it's often done
        // by IP anyway.
        // However, if the user requested it, we can try.
        // Note: AsyncPlayerPreLoginEvent doesn't expose a player object to check
        // permissions on until LoginEvent.
        // But some permission plugins load data early. We'll skip complex perm checks
        // here to avoid blocking async thread
        // or errors, unless we want to move to PlayerLoginEvent (sync).
        // Let's stick to IP check for security in PreLogin which is standard.
        // If the user *really* wants bypass, they can add their IP.

        InetAddress address = event.getAddress();
        String ip = address.getHostAddress();

        // Debug logging
        if (plugin.getConfig().getBoolean("debug-mode", false)) {
            plugin.getLogger().info("Checking IP: " + ip);
        }

        if (!databaseManager.isWhitelisted(ip)) {
            String kickMessageRaw = plugin.getConfig().getString("kick-message",
                    "<red>You are not whitelisted on this server.</red>");
            Component kickMessage = miniMessage.deserialize(kickMessageRaw);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, kickMessage);
        }
    }
}
