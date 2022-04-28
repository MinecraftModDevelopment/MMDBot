---
title: Punishments
sidebar_position: 1
---
Punishments are a way of punishing a member for doing something.  
A punishment is configured in the general config of The Watcher, in the `punishments` block, the property with the punishment name.
The punishment format is: `[ACTION] [DURATION]`
The valid actions are: 
- Ban
- Kick
- Mute
  
Duration examples: 1d, 2y, 3m, 4s
In order to disable a punishment, set its value to `NONE`.

# Punishable Actions:

## Phishing Link
Name: `phishing_link`  
Default Value: `MUTE 1d`  
Description: A member has this punishment applied when they send a phishing link, or they edit a message whose new content contains one. Phishing links are tested against the domains on this site: https://phish.sinking.yachts/v2/all.

## Spam pinging
Name: `spam_pinging`  
Default Value: `BAN 2d`  
Description: A member has this punishment applied when they ping 20 or more members in one message.

## New account
Name: `new_account`  
Default Value: `KICK`  
Description: A member has this punishment applied when they join the guild with an account that is newer than one hour.