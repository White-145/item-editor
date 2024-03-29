# Simple Item Editor

Simple item editor is a minecraft mod *(fabric 1.20.1)* for item editing using client commands.

## Features:

### `/edit` command:
- `... name ...`
- - `... get`
- - `... set [<name>]`
- - `... remove`
- `... lore ...`
- - `... get [<index>]`
- - `... set <index> [<line>]`
- - `... insert <index> [<line>]`
- - `... add [<line>]`
- - `... remove <index>`
- - `... clear`
- - `... clear ...`
- - - `... before <index>`
- - - `... after <index>`
- `... material ...`
- - `... get`
- - `... set <material>`
- `... count ...`
- - `... get`
- - `... set [<count>]`
- - `... add [<count>]`
- - `... remove [<count>]`
- - `... stack`
- `... model ...`
- - `... get`
- - `... set <model>`
- - `... remove`
- `... enchantment ...`
- - `... get [<enchantment>]`
- - `... set <enchantment> [<level>]`
- - `... remove <enchantment>`
- - `... clear`
- - `... glint`
- `... get [<item>] [<count>]`
- `... attribute ...`
- - `... get [<slot>] [<attribute>]`
- - `... add [<attribute> <amount>|infinity] [<operation>] [<slot>]`
- - `... remove <attribute> [<slot>]`
- - `... clear [<slot>]`
- `... equip [<slot>]`
- `... color ...`
- - `... get`
- - `... set <color>`
- - `... remove`
- `... unbreakable`
- - `... get`
- - `... toggle`
- `... flags ...`
- - `... get [<flag>]`
- - `... toggle <flag>`
- - `... toggle all`
- `... whitelist ...`
- - `... get [place|destroy]`
- - `... add place|destroy <block>`
- - `... remove place|destroy <block>`
- - `... clear [place|destroy]`
- `... durability ...`
- - `... get`
- - `... set [<durability>]`
- - `... percent <durability>`
- - `... remove`
- `... data ...`
- - `... append <path> <value>`
- - `... get <path>`
- - `... insert <path> <index> <value>`
- - `... merge <value> [<path>]`
- - `... prepend <path> <value>`
- - `... remove <path>`
- - `... clear`
- - `... set <path> <value>`
- `... book ...`
- - `... author ...`
- - - `... get`
- - - `... set <author>`
- - `... title ...`
- - - `... get`
- - - `... set <title>`
- - `... generation ...`
- - - `... get`
- - - `... set <generation>`
- - `... page ...`
- - - `... get [<index>]`
- - - `... set <index> [<page>]`
- - - `... insert <index> [<page>]`
- - - `... add [<page>]`
- - - `... remove <index>`
- - - `... clear`
- - - `... clear before <index>`
- - - `... clear after <index>`
- `... head ...`
- - `... get`
- - `... set`
- - `... set ...`
- - - `... owner <owner>`
- - - `... texture <texture>`
- - `... sound ...`
- - - `... get`
- - - `... set [<sound>]`
- - - `... remove`
- `... trim ...`
- - `... get`
- - `... set [<pattern> <material>]`
- - `... remove`
- `... firework ...`
- - `... flight ...`
- - - `... get`
- - - `... set [<flight>]`
- - `... explosion ...`
- - - `... get`
- - - `... add <type> <colors> [<flicker>] [<trail>] [<fadeColors>]`
- - - `... remove <index>`
- - - `... clear`
- `... banner ...`
- - `... get [<index>]`
- - `... set <index> <pattern> <color>`
- - `... remove <index>`
- - `... add <pattern> <color>`
- - `... insert <index> <pattern> <color>`
- - `... clear`
- - `... clear before <index>`
- - `... clear after <index>`
- `... potion ...`
- - `... get [<effect>]`
- - `... set <effect> [<level>] [<duration>|infinity] [<particle>]`
- - `... remove [<effect>]`
- - `... clear`
- `... custom ... `
- - `... get [<identifier>]`
- - `... set <identifier> <value>`
- - `... remove <identifier>`
- - `... clear`
- `... entity ...`
- - `... absorption ...`
- - - `... get`
- - - `... set [<absorption>]`
- - `... air ...`
- - - `... get`
- - - `... set [<air>]`
- - `... data ...`
- - - `... append <path> <value>`
- - - `... get <path>`
- - - `... insert <path> <index> <value>`
- - - `... merge <value> [<path>]`
- - - `... prepend <path> <value>`
- - - `... remove <path>`
- - - `... clear`
- - - `... set <path> <value>`
- - `... glow ...`
- - - `... get`
- - - `... toggle`
- - `... gravity ...`
- - - `... get`
- - - `... toggle`
- - `... health ...`
- - - `... get`
- - - `... set [<absorption>]`
- - `... intellect ...`
- - - `... get`
- - - `... toggle`
- - `... invisibility ...`
- - - `... get`
- - - `... toggle`
- - `... invulnerability ...`
- - - `... get`
- - - `... toggle`
- - `... motion ...`
- - - `... get`
- - - `... set [<motion>]`
- - `... persistance ...`
- - - `... get`
- - - `... toggle`
- - `... pickingup ...`
- - - `... get`
- - - `... toggle`
- - `... position`
- - - `... get`
- - - `... set [<position>]`
- - `... rotation`
- - - `... get`
- - - `... set [<rotation>]`
- - `... silence ...`
- - - `... get`
- - - `... toggle`
- - `... type ...`
- - - `... get`
- - - `... set [<type>]`

All commands are also available as `/itemeditor:<command>`

## Useful Information

- Written book displays "\*Invalid book tag\*" if any of the `title`, `author` and `pages` tags is not present 
- Item durability can be set below *0*
- Item durability [is not displayed if `Unbreakable` tag is enabled](https://minecraft.fandom.com/wiki/Durability#Interface), and thus you cannot change it with `/edit`
- Item head texture should be starting with `https://textures.minecraft.net/texture/` to be applied by minecraft
- Item count can be set to [more than *64*, but less than *128*](https://minecraft.fandom.com/wiki/Item#Behavior)
- In some text arguments (like `name` or `line`) you can use following placeholders:
- - `&X` for [vanilla formatting codes](https://minecraft.fandom.com/wiki/Formatting_codes)
- - `&#XXXXXX` for hex color
- - `&<X>` for [keybinds](https://minecraft.fandom.com/wiki/Controls#Configurable_controls)
- - `&[X]` for [translation codes](https://minecraft.fandom.com/wiki/Raw_JSON_text_format#Translated_Text)
- - `&_` for space (" ")
- Also in text arguments you can remove post formatting (like italic in custom name and lore) with `&r`

## Planned Features:

- `/color` command
- `/char` command

## Credits:

- [MineSkin](https://mineskin.org/): Custom head textures
- [DiamondFire](https://mcdiamondfire.com/home/): Idea of such item editor
