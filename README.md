# Hello there

This is a monorepo containing plugins published under my Modrinth account. Each directory is a releasable plugin with its own versions. `WoolyCommon` is a shared library shaded into every plugin JAR, and is therefore never loaded by Paper.

<details>
<summary>List of plugins</summary>

1. **Combat Tag**: [Modrinth](https://modrinth.com/plugin/combat-tag) • [Source](https://github.com/Woolyenough/modrinth-plugins/tree/master/CombatTag)
1. **Crystal Damage Modifier**: [Modrinth](https://modrinth.com/plugin/crystal-damage-modifier) • [Source](https://github.com/Woolyenough/modrinth-plugins/tree/master/CrystalDamageModifier)
1. **Custom MOTD**: [Modrinth](https://modrinth.com/plugin/custom-motd) • [Source](https://github.com/Woolyenough/modrinth-plugins/tree/master/CustomMOTD)
1. **Fly Speed**: [Modrinth](https://modrinth.com/plugin/fly-speed) • [Source](https://github.com/Woolyenough/modrinth-plugins/tree/master/FlySpeed)
1. **Login Cooldown**: [Modrinth](https://modrinth.com/plugin/login-cooldown) • [Source](https://github.com/Woolyenough/modrinth-plugins/tree/master/LoginCooldown)
1. **Player Freeze**: [Modrinth](https://modrinth.com/plugin/player-freeze) • [Source](https://github.com/Woolyenough/modrinth-plugins/tree/master/PlayerFreeze)

</details>

## Building
Plugin JARs are produced in each plugin module's `target/` directory.

Build: `mvn clean verify`

Build (plugin): `mvn -pl FlySpeed -am package`.
