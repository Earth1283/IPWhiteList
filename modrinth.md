# IPWhitelist

**IPWhitelist** is a lightweight yet powerful security plugin designed to restrict server access to specific IPv4 addresses. It is ideal for private servers, staff-only networks, or maintaining high security on public servers by restricting admin account access to known locations.

## 🛡️ Key Features

*   **Strict Access Control**: Only connections from whitelisted IPs are allowed to join.
*   **Player & IP Association**: Associate IPs with player names to easily track who owns which address.
*   **Safety First**: Built-in confirmation system prevents accidental bulk-deletion of user IPs.
*   **MiniMessage Support**: Beautiful, modern message formatting with RGB colors and gradients.
*   **Performance**: Uses high-performance SQLite for storage—no external database setup required.
*   **Bypass Options**: Grant `ipwhitelist.bypass` permission to completely skip checks for trusted users.

## 🚀 Commands & Permissions

**Main Command**: `/ipwhitelist` (or `/ipw`)

*   **Add IP**: `/ipw add <ip> [player]`
    *   *Adds an IP, optionally tagging it with a player name.*
*   **Remove IP**: `/ipw remove <ip>`
    *   *Removes a specific IP instantly.*
*   **Remove Player**: `/ipw remove <player>`
    *   *Removes all IPs linked to a player name (requires confirmation).*
*   **List**: `/ipw list`
    *   *Shows all whitelisted entries.*
*   **Reload**: `/ipw reload`
    *   *Reloads the configuration.*

**Permission**: `ipwhitelist.admin` (Required for all commands)

## ⚙️ Configuration

Fully customizable `config.yml` allows you to change every message output by the plugin.

```yaml
kick-message: "<red><bold>ACCESS DENIED</bold><br><gray>Your IP address is not authorized.</gray></red>"
```

## 📦 Installation

1.  Drop `IPWhitelist.jar` into your `plugins` folder.
2.  Restart your server.
3.  Add your IP using the console: `ipw add <your-ip>`.
4.  Enjoy enhanced security!
