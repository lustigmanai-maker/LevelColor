# LevelColor

Client-side Minecraft Java **26.3** Fabric mod to customize only the XP level number color.

## Features

- Open the in-game menu with **K** while in a world.
- Enter a HEX color (for example `#B10100`) or RGB values (0–255 per channel).
- Saves the color in `.minecraft/config/levelcolor.json`.
- Modifies vanilla's existing XP number foreground color; the four black outline draws are left alone.

## Build status

Every push to `main` triggers a GitHub Actions build using Java 25 and the actual Minecraft/Fabric dependencies. The build JAR is published as a workflow artifact only when compilation succeeds.

Open the **Actions** tab, choose the latest successful **Build LevelColor** run, then download the `LevelColor-26.3-...` artifact. Extract the JAR from the downloaded ZIP and put only that JAR into the NoRisk profile's `mods` folder.

## Build versions

- Minecraft 26.3
- Java 25
- Fabric Loader 0.20.1
- Fabric API 0.165.0+26.3
- Fabric Loom 1.19-SNAPSHOT
