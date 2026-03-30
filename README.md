# Ward & Watch

Ward & Watch is a Fabric security mod for Minecraft `1.21.11`.

The project is inspired by classic security-style mods, but it is being built as its own original Fabric mod with its own code, assets, and progression.

## Current Features

- `Password Protector` item with crafting recipe
- Password-protected chests
- Password-protected furnaces, blast furnaces, and smokers
- Password-protected door block (`Protected Door`)
- Numeric password setup screen
- Unlock screen with keypad and masked password input
- Owner-only breaking for protected blocks and doors
- Protected chests with custom textures
- Protected door with custom texture
- Spanish and English localization

## Protected Door

The current `Protected Door` is an independent block, not a modified vanilla `iron_door`.

Behavior:

- Crafted separately
- Requires a `Password Protector` in its recipe
- Prompts for setup on first interaction
- Opens only after entering the correct code
- Closes automatically after about 3 seconds (`60` ticks)
- Cannot be opened by redstone
- Can only be broken by its creator once protected

## Requirements

- Minecraft `1.21.11`
- Fabric Loader `0.18.4+`
- Fabric API `0.141.1+1.21.11`
- Java `21`

## Installation

1. Install Fabric Loader for Minecraft `1.21.11`
2. Install Fabric API
3. Put the mod jar into your `mods` folder

Current build:

- `build/libs/ward-watch-0.1.0.jar`

## Development Status

This project is currently in `alpha`.

Implemented now:

- Core password system
- Chest protection flow
- Furnace protection flow
- First protected door implementation

Planned next:

- Protected furnace visual variants
- More security blocks
- Additional door variants
- More polish for assets and UI

## Building

```powershell
./gradlew build
```

## License

`CC0-1.0`
