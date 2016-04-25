# Anonymous Plugin

**Required plugin: ProtocolLib**

This plugin changes everyone in the game to have the same skin/name. The
plugin also modifies the player's display name so chat plugins will 
use the modified name too.

Configuration:

```yaml
username: Anonymous
skin: 640a5372-780b-4c2a-b7e7-8359d2f9a6a8
disable chat: false
rewrite names in tab complete: true
rewrite joins and leaves: true
refresh skin minutes: 5
```

`username`

This is the name that is shown above player heads and in tab. Players
with bypass permissions will see the real name

`skin` 

This is the uuid (with dashses) of the skin to use for tab/players.
Players with bypass permissions will see the real skin

`disable chat`

Whether chat should be disabled or not. Note: the user's display name is
changed so any chat will show as the `username` name.

`rewrite names in tab complete`

Whether to modify names tab completed in chat messages to `username`

`rewrite joins and leaves` 

Rewrites the join and leave messages to use `username`. This uses the
regular Minecraft translatable messages. If you are using a plugin that
already modifies joins/leaves you should disable this and as long as 
that plugin uses the players display name instead of username they will
work correctly.

`refresh skin minutes`

How many mintues between attempting to refresh the stored skin. Cached
skins are stored in the file `skin-cache.yml` in the config directory

### Permissions

`anonymous.chat.bypass` - allows to chat even when `disable chat` is set
to true, defaults to OP

`anonymous.tabcomplete.bypass` - allows tab completing real names in 
chat messages

`anonymous.skin.bypass` - allows viewing of the real name/skin, defaults
to OP

`anonymous.joinleave.bypass` - allows viewing of the real join/leave
messages

# Known problems for leaking names

There are still a couple of ways player names could be leaked:

- `/me`/`/bukkit:me` this command still uses the regular name, blocking
the command from being used will solve that issue

- Commands will still tab complete names (e.g. `/tell <TAB>`), one way
to fix this is to configure Spigot to disallow all tab completing

- Other commands may leak names (like `/teams`). They will need to 
either be blocked or have player permissions modified to avoid leaking
names