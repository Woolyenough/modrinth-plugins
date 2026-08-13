## [Combat Tag](https://modrinth.com/plugin/combat-tag)
Plugin to block commands, display messages & different scoreboards (with TAB) when players hit each other for a specified time.

### Commands
- `/combat-tag reload` (alias: `/ct`) — reload configuration 

Requires `combattag.admin`.

### Placeholders
- `%ct_in_combat%` 
- `%ct_time_left%`

### Configuration
<details><summary><b>Combat scoreboard</b></summary>

[TAB](https://github.com/NEZNAMY/TAB) offers a pretty nifty feature where you can display scoreboards determined by conditions. You can use this in conjunction with the `in_combat` placeholder offered by this plugin to create a scoreboard that is displayed when the player is in combat.

Example usage (in TAB's `config.yml`):
```yaml
scoreboard:
  scoreboards:
    # Combat scoreboard (TAB checks top to bottom)
    combat:
      display-condition: "%ct_in_combat%=true" # <- the condition
      title: Combat scoreboard
      lines:
        - ' You are in combat!'
        - ' %ct_time_left% secs left'
    # Default scoreboard
    scoreboard:
      title: Normal scoreboard
      lines:
        - ' The default scoreboard of my server!'
```
</br></details>

Messages use MiniMessage and live in [`messages.yml`](https://github.com/Woolyenough/modrinth-plugins/tree/master/CombatTag/src/main/resources/messages.yml); behaviour is configured in [`config.yml`](https://github.com/Woolyenough/modrinth-plugins/tree/master/CombatTag/src/main/resources/config.yml)
