## [Crystal Damage Modifier](https://modrinth.com/plugin/crystal-damage-modifier)

Scale End Crystal and Respawn Anchor explosion damage with their own configurable multiplier.

### Rationale
Originally created for an SMP server to reduce End Crystal damage, which was considered overpowered in PvP scenarios. This plugin provides a simple way to balance gameplay by adjusting explosive damage to suit your server's needs.

### Commands
All commands require `crystaldamage.admin`.

- `/crystaldamage` - show the current multipliers
- `/crystaldamage reload` - reload `config.yml`
- `/crystaldamage set <type> <factor>` - change and save one type's multiplier, where `<type>` is `end-crystal` or `respawn-anchor`

### Configuration

Messages use MiniMessage and live in [`messages.yml`](https://github.com/Woolyenough/modrinth-plugins/tree/master/CrystalDamageModifier/src/main/resources/messages.yml); behaviour is configured in [`config.yml`](https://github.com/Woolyenough/modrinth-plugins/tree/master/CrystalDamageModifier/src/main/resources/config.yml)