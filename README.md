# Anonymous Plugin

**Required plugin: ProtocolLib**

This plugin changes everyone in the game to have the same skin/name. The
plugin also modifies the player's display name so chat plugins will 
use the modified name too.

Configuration:

```yaml
username: Anonymous
skin: 640a5372-780b-4c2a-b7e7-8359d2f9a6a8
disable chat: true
```

`username` = name above head

`skin` = the uuid of the skin to use

`disable chat` = whether chat should be disabled or not

### Permissions

`anonymous.chat.bypass` - allows to chat even when `disable chat` is set
to true, defaults to OP

`anonymous.skin.bypass` - allows viewing of the real name/skin, defaults
to OP
