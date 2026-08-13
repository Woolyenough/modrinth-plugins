## [Custom MOTD](https://modrinth.com/plugin/custom-motd)

Paper plugin for easily modifying the MOTD, player limit, and listed player info (message when hovering cursor over the player count).

### Commands and permissions
- `/motd-reload` - reload the configuration and server-list icon
- `/max-players <number>` - set and save the displayed player limit

Both commands require `custommotd.admin`.

### Configuration

Messages use MiniMessage and live in [`messages.yml`](https://github.com/Woolyenough/modrinth-plugins/tree/master/CustomMOTD/src/main/resources/messages.yml); behaviour is configured in [`config.yml`](https://github.com/Woolyenough/modrinth-plugins/tree/master/CustomMOTD/src/main/resources/config.yml)