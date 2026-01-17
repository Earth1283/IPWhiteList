package io.github.Earth1283.ipwhitelist;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WhitelistCommand implements CommandExecutor, TabCompleter {
    private final IPWhitelistPlugin plugin;
    private final DatabaseManager databaseManager;
    private final ConfirmationManager confirmationManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Strict IPv4 Regex
    private static final String IPV4_REGEX = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    public WhitelistCommand(IPWhitelistPlugin plugin, DatabaseManager databaseManager,
            ConfirmationManager confirmationManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.confirmationManager = confirmationManager;
    }

    private String getMessage(String key) {
        return plugin.getConfig().getString("messages." + key, "");
    }

    private void sendMessage(CommandSender sender, String key, String... placeholders) {
        String msg = getMessage("prefix") + getMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            msg = msg.replace("<" + placeholders[i] + ">", placeholders[i + 1]);
        }
        sender.sendMessage(miniMessage.deserialize(msg));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission("ipwhitelist.admin")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sendMessage(sender, "usage");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "add" -> {
                if (args.length < 2) {
                    sendMessage(sender, "usage");
                    return true;
                }
                String ip = args[1];
                if (!ip.matches(IPV4_REGEX)) {
                    sendMessage(sender, "invalid-ip");
                    return true;
                }
                String playerName = (args.length > 2) ? args[2] : null;

                if (databaseManager.addIP(ip, sender.getName(), playerName)) {
                    sendMessage(sender, "add-success", "ip", ip, "player", (playerName != null ? playerName : "None"));
                } else {
                    sendMessage(sender, "add-fail", "ip", ip);
                }
            }
            case "remove" -> {
                if (args.length < 2) {
                    sendMessage(sender, "usage");
                    return true;
                }
                String target = args[1];

                // Check if target is an IP
                if (target.matches(IPV4_REGEX)) {
                    if (databaseManager.removeIP(target)) {
                        sendMessage(sender, "remove-success", "ip", target);
                    } else {
                        sendMessage(sender, "remove-fail", "ip", target);
                    }
                } else {
                    // Treat as player name
                    List<String> userIPs = databaseManager.getIPsByPlayer(target);
                    if (userIPs.isEmpty()) {
                        sender.sendMessage(miniMessage.deserialize(
                                getMessage("prefix") + "<red>No IPs found for player " + target + ".</red>"));
                    } else {
                        // Request confirmation
                        String confirmMsgRaw = getMessage("remove-confirm")
                                .replace("<count>", String.valueOf(userIPs.size()))
                                .replace("<player>", target);

                        confirmationManager.requestConfirmation(sender, () -> {
                            int count = databaseManager.removeIPsByPlayer(target);
                            sender.sendMessage(miniMessage.deserialize(getMessage("prefix") + "<green>Removed " + count
                                    + " IPs for " + target + ".</green>"));
                        }, miniMessage.deserialize(getMessage("prefix") + confirmMsgRaw));
                    }
                }
            }
            case "confirm" -> {
                if (confirmationManager.confirm(sender)) {
                    sendMessage(sender, "confirm-success");
                } else {
                    sendMessage(sender, "confirm-fail");
                }
            }
            case "reload" -> {
                plugin.reloadConfig();
                sendMessage(sender, "reload");
            }
            case "list" -> {
                List<String> ips = databaseManager.getAllIPs();
                if (ips.isEmpty()) {
                    sendMessage(sender, "list-empty");
                } else {
                    sendMessage(sender, "list-header");
                    for (String listIp : ips) {
                        sender.sendMessage(miniMessage.deserialize("<gray>- " + listIp + "</gray>"));
                    }
                }
            }
            default -> sendMessage(sender, "usage");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("ipwhitelist.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subCommands = List.of("add", "remove", "list", "reload", "confirm");
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                List<String> suggestions = new ArrayList<>(databaseManager.getAllIPs()); // Contains "IP (Player)"
                                                                                         // strings
                // Actually tab complete should just suggest raw IPs or logic to extract names?
                // `getAllIPs` returns formatted strings now. We might need raw access or parse.
                // Let's implement a specific method in DBManager or parse here.
                // Or just fetch all unique player names from DB + all IPs.
                // Creating a new method in DBManager effectively would be cleaner but for now
                // let's reuse/parse.
                // Actually, simply iterating through `getAllIPs` output:
                return suggestions.stream()
                        .map(s -> s.split(" ")[0]) // Just IP
                        // Also add known player names?
                        // Ideally we query DB for names.
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("add")) {
                // Return nothing for IP part? Or maybe history? Just empty for now.
                return Collections.emptyList();
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            // Suggest online players for association
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
