# TeamSmith Plugin

A robust team management system for Minecraft servers with advanced features for team collaboration and administration.

## Features
- Team creation/disbanding with ownership management
- Role system (Owner/Manager/Member) with permissions
- Customizable team chat prefixes with colors
- Team MOTD messages
- Friendly fire toggle
- Warp system with 3 named locations
- Team home locations
- Invite/kick/leave mechanics
- Detailed team info displays
- Tab-complete support for all commands

## Installation
1. Build plugin with `mvn clean package`
2. Place generated JAR in your server's `plugins/` folder
3. Restart/reload server

## Commands
```
/team create <name> - Create new team
/team invite <player> - Invite player
/team kick <player> - Remove player
/team prefix <text> - Set chat prefix
/team prefixcolor <&code> - Set prefix color
/team transfer <player> - Transfer ownership
/team sethome - Set team home
/team warp <name> - Teleport to warp
/team friendlyfire <on|off> - Toggle PvP
... (full command list in command handlers)
```

## Permissions
- `teamsmith.command.*` - Access to all commands
- Team-specific permissions handled through role system

## Configuration
- Team data stored in `plugins/TeamSmith/teams.yml`
- Modify `config.yml` for default settings

## Dependencies
- Requires Spigot/Paper 1.16.5+
- Maven: `org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT`

## Contributing
Pull requests welcome! Please follow existing code style and add tests for new features.