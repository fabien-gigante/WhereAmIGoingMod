<img height="96" align="right" src="./src/main/resources/assets/waig/icon.png" alt="WAIG Icon">

# WAIG: Where Am I Going

![Environment: client](https://img.shields.io/badge/environment-client-87CEFA?style=flat)
[![modrinth downloads](https://img.shields.io/modrinth/dt/waig?color=00AF5C&label=modrinth&style=flat&logo=modrinth)](https://modrinth.com/mod/waig)
[![CurseForge downloads](https://cf.way2muchnoise.eu/waig-where-am-i-going.svg)](https://www.curseforge.com/minecraft/mc-mods/waig-where-am-i-going)
[![CurseForge versions](https://cf.way2muchnoise.eu/versions/waig-where-am-i-going.svg)](https://www.curseforge.com/minecraft/mc-mods/waig-where-am-i-going/files)

Fabric mod for Minecraft, adds a minimal RPG-inspired compass HUD to the game.
This mod is client side only.

![Demo Image](demo.gif)

**Features:**

- Always know in which direction your Minecraft character is going
- Toggleable via key binding (default: F6)
- Can be configured to show the HUD only when the player carries a compass in the inventory or is holding a compass in
  any hand. See Configuration section below for details.
- Does not obstruct boss bars by moving down
- Client side only
- Scales with the GUI

**FORK Additional Features:**

![Fork Image](screenshot-fork.png)
- Smoother display with rounded effect, sync'ed with player's FoV
- The compass targets (spawn, death, anchor) or the map's points of interest are visible when the corresponding item is in inventory or held (see Configuration)
- Distance to targets can optionnaly be shown (see Configuration)

**Configuration:**

- Config file location: `<minecraft-base-folder>/config/waig.config`
- A default configuration is generated when the mod is started for the first time or if the config file is missing
- `hud-show-mode`: config key to enable the compass HUD only in certain circumstances (default: `always`)
  - if set to `always` the HUD will always be displayed
  - if set to `inventory` the HUD will be displayed only if the player carries a compass in the inventory
  - if set to `hand` the HUD will be displayed only if the player holds a compass in the main hand or off-hand
- `compass-items`: config key to list all items that count as compass equivalent. When `hud-show-mode` is set to
  `inventory` or `hand`, the mod checks if any of the listed items are in the inventory or held, and if so will show
  the HUD.

**FORK Additional Configuration:**
- `hud-fov-mode`: config key to control how wide is the hud
  - if set to `player` it will match the player's FoV
  - if set to `fixed` it will have a constant width
- `hud-poi-mode`: config key to control how targets and points of interest are shown (default: `icon`)
  - if set to `hidden` they will not be shown on the compass HUD
  - if set to `icon` they will appear has icons on the compass HUD
  - if set to `distance` they will appear has icons on the compass HUD with a distance to target just below the icon
- `compass-items`: now defaults to `minecraft:compass, minecraft:recovery_compass, minecraft:filled_map`
