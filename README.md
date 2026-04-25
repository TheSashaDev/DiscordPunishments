# DiscordPunishments

Minecraft Paper/Spigot plugin that lets staff issue punishments from Discord using buttons and modal forms.

## Features

- Discord control panel with Ban / Mute / Kick actions
- Modal forms for nickname, duration, and reason
- Configurable Minecraft punishment commands
- Admin permission node for setup
- Maven-based Java 17 build

## Requirements

- Java 17+
- Paper/Spigot 1.20+
- Discord bot token
- Maven for building from source

## Build

```bash
mvn clean package
```

The compiled plugin jar will be generated in `target/`.

## Installation

1. Build or download the plugin jar.
2. Put it into your server `plugins/` directory.
3. Restart the server.
4. Configure the plugin and Discord bot settings.
5. Use `/discordpunish setup` to create the control panel.

## Permissions

```yaml
discordpunishments.admin
```

## Notes

Do not commit real Discord bot tokens. Store secrets in config files excluded from Git or in your hosting provider's secret manager.
