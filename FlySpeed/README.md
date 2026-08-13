## [Fly Speed](https://modrinth.com/plugin/fly-speed)

This plugin allows you to set the flying speed of yourself or other players on your server.

### Rationale
When working on large maps, I found the default flying speed to be a constant frustration. I created this plugin because I couldn't find a lightweight, simple, non-legacy plugin that offers this functionality.

### Commands and Permissions

- `/fly-speed <number> [player]` (aliases: `/fs`, `/flyspeed`) - accepts decimal values from `-10` to `10`
- `fs.use` - use the command
- `fs.others` - set another player's speed

`[player]` accepts a name or a target selector (`@p`, `@r`, `@s`).

Messages use MiniMessage and live in [`messages.yml`](https://github.com/Woolyenough/modrinth-plugins/tree/master/FlySpeed/src/main/resources/messages.yml); behaviour is configured in [`config.yml`](https://github.com/Woolyenough/modrinth-plugins/tree/master/FlySpeed/src/main/resources/config.yml)