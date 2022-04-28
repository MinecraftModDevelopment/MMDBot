---
title: Getting Started
sidebar_position: 1
---

In order to get started using the bot, make sure you have at least Java 17 installed (if not, Temurin Java builds can be downloaded from [here](https://adoptium.net/download)), and that you downloaded the latest bot version from the [Releases](https://github.com/MinecraftModDevelopment/MMDBot/releases) tab.
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

## One-bot jars
We provide the ability to use one-bot jars, which contain only one bot, unlike the `-all` jar.
Such executables are not provided in releases, but you can built them yourself following the following steps:
- Download the source code from the release you want;
- Unarchive the source code `.zip` file;
- In the folder you extracted the source code, run the command `./gradlew [botName]jar`, where `[botName]` is the name of the bot you want to build;
- After the task finished, in the `/build/libs/` folder, there should be a file called `MMDBot-[version]-[botName].jar` containing the standalone bot you built.
The only difference between the bot from the big jar and the one you just built is that the latter will not create a `config.json` file, and the bot will run in the folder that the jar was started in.

## Setting up the tokens
Each bot needs a token, which is defined in a `.env` file in the directory of that bot. A bot token can be gotten from the [Discord Developer Portal](https://discord.com/developers/applications/), and is associated with the `BOT_TOKEN` key in the `.env`:
```env
BOT_TOKEN=<myBotToken>
```