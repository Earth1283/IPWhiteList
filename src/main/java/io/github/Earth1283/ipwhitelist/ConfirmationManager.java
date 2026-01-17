package io.github.Earth1283.ipwhitelist;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConfirmationManager {
    private final Map<UUID, Confirmation> pendingConfirmations = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private record Confirmation(Runnable action, long timestamp) {
    }

    public void requestConfirmation(CommandSender sender, Runnable action, Component message) {
        if (!(sender instanceof org.bukkit.entity.Player)) {
            // Console doesn't need confirmation usually, but for consistency if requested:
            // Actually, let's allow console to confirm too using a fake UUID or similar,
            // but console usually executes immediately. For safety, let's treat console
            // same way if we want,
            // or just bypass. Let's support console confirmation.
        }

        UUID id = getSenderId(sender);
        pendingConfirmations.put(id, new Confirmation(action, System.currentTimeMillis()));
        sender.sendMessage(message);

        // Expire after 30 seconds
        scheduler.schedule(() -> {
            if (pendingConfirmations.containsKey(id) &&
                    System.currentTimeMillis() - pendingConfirmations.get(id).timestamp >= 30000) {
                pendingConfirmations.remove(id);
            }
        }, 30, TimeUnit.SECONDS);
    }

    public boolean confirm(CommandSender sender) {
        UUID id = getSenderId(sender);
        Confirmation confirmation = pendingConfirmations.remove(id);
        if (confirmation != null) {
            confirmation.action.run();
            return true;
        }
        return false;
    }

    private UUID getSenderId(CommandSender sender) {
        if (sender instanceof org.bukkit.entity.Player player) {
            return player.getUniqueId();
        } else {
            // Constant UUID for console
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
    }
}
