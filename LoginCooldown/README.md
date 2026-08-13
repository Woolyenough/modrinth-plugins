## [Login Cooldown](https://modrinth.com/plugin/login-cooldown)

A plugin that prevents players from joining and leaving excessively, with a configurable limit on the number of times allowed within a set time frame.

### Rationale
Originally created to address players spamming chat with repeated leave and join messages, especially as a spam tactic after being muted or a way to circumvent chat spam.

### Usage
- `/logincooldown reload` - Reloads the configuration file
- `/logincooldown set <joins-per|time-frame> <value>` - Directly modify one of the two config values

Both commands require `logincooldown.admin` permission.

- `logincooldown.notify` - Players with this permission will receive the configurable notification message when someone is blocked from joining
- `logincooldown.bypass` - Players with this permission are never blocked from joining

### Configuration
Everything, messages included, is configured in [`config.yml`](https://github.com/Woolyenough/modrinth-plugins/tree/master/LoginCooldown/src/main/resources/config.yml). Messages use MiniMessage.
