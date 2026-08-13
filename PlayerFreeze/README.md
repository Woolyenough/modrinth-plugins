## [PlayerFreeze](https://modrinth.com/plugin/player-freeze)

A simple player freeze plugin.

### Commands
- `/freeze <player>` - freeze a player
- `/unfreeze <player>` - unfreeze a player
- `/player-freeze reload` (alias: `/pf`) - reload config

`<player>` accepts a name or a target selector (`@p`, `@r`, `@s`).

### Placeholders
- `%pf_frozen%`

### Permissions
- `pf.use` - run `/freeze` & `/unfreeze` commands
- `pf.immune` - immune to being frozen
- `pf.notify` - see the frozen broadcast
- `pf.admin` - permission to run `/pf reload` command

Messages use MiniMessage and live in [`messages.yml`](https://github.com/Woolyenough/modrinth-plugins/tree/master/PlayerFreeze/src/main/resources/messages.yml); behaviour is configured in [`config.yml`](https://github.com/Woolyenough/modrinth-plugins/tree/master/PlayerFreeze/src/main/resources/config.yml)