# IPWhitelist

A robust Minecraft plugin to whitelist incoming connections based on specified IPv4 addresses. Ensure only trusted locations can access your server, adding an extra layer of security for staff or private SMPs.

## Features
- **Strict IP Validation**: Ensures only valid IPv4 addresses are added.
- **Player Association**: Link IPs to specific player names for better management.
- **Safe Removal**: Requires confirmation when removing IPs by player name to prevent accidents.
- **Customizable Messages**: rich text support using MiniMessage (gradients, colors, click events).
- **SQLite Storage**: Efficient and persistent local storage.
- **Bypass Permission**: Allow specific players/ranks to bypass the IP check.

## Installation
1. Download the latest `IPWhitelist-2.0.jar` from the releases tab.
2. Place the jar in your server's `plugins` folder.
3. Restart the server.
4. Configure `plugins/IPWhiteList/config.yml` if desired.

## Commands
| Command | Description | Permission |
|---|---|---|
| `/ipw add <ip> [player]` | Add an IP to the whitelist. Optionally link to a player. | `ipwhitelist.admin` |
| `/ipw remove <ip>` | Remove a specific IP. | `ipwhitelist.admin` |
| `/ipw remove <player>` | Remove all IPs associated with a player (requires confirmation). | `ipwhitelist.admin` |
| `/ipw list` | List all whitelisted IPs. | `ipwhitelist.admin` |
| `/ipw reload` | Reload the configuration file. | `ipwhitelist.admin` |
| `/ipw confirm` | Confirm a pending removal action. | `ipwhitelist.admin` |

*Alias: `/ipwhitelist`, `/ipw`*

## Configuration
The `config.yml` allows you to customize the kick message and all plugin feedback.

```yaml
# Message shown to players who are not whitelisted.
kick-message: "<red><bold>You are not whitelisted on this server.</bold><br><gray>Please contact an administrator if you think that this is a mistake.</gray></red>"

# Enable debug logging
debug-mode: false

# Custom messages
messages:
  prefix: "<gray>[<gradient:#00ff00:#00aa00>IPWhitelist</gradient>]</gray> "
  # ... and more
```

## Permissions
- `ipwhitelist.admin`: Full access to manage the whitelist.
- `ipwhitelist.bypass`: Bypasses the IP check (useful for unexpected travel/dynamic IPs).
