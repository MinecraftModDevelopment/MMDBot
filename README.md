# MMDBot
An in house bot for MMD that handles the logging of Discord events, commands, cleanup and other misc things.

For a list of the commands you can access please use ``/help`` in our [Discord server.](https://discord.mcmoddev.com)

## Getting Started
In order to get started using the bot, make sure you have at least Java 17 installed, and that you downloaded the latest bot version from the Releases tab.
Once you have the bot jar downloaded, run it for the first time using `java -jar MMDBot-[version]-all.jar`. A new file called `config.json` will be created.
That file decides the path of each bot, and if it is enabled. By default, all bots are disabled.
In order to enable a bot, change the `enabled` property to `true`:
```json
"thecommander": {
    "enabled": true,
    "runPath": "thecommander"
}
```
After you've enabled at least one bot, run the jar again, and your bot(s) should start for the first time.
## Setting up the tokens
Each bot needs a token, which is defined in a `.env` file in the directory of that bot. A bot token can be gotten from the [Discord Developer Portal](https://discord.com/developers/applications/), and is associated with the `BOT_TOKEN` key in the `.env`:
```env
BOT_TOKEN=<myBotToken>
```
