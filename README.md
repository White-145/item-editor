# Simple Item Editor

Simple item editor is a minecraft mod for item editing using client commands.

## Features:

### `/edit` command (as of latest commits):
- `... attribute ...`
- - `... get [<id>]`
- - `... set <id> <attribute> <amount> [<operation>] [<slot>]`
- - `... remove <id>`
- - `... clear`
- `... banner ...`
- - `... get [<index>]`
- - `... set <index> <pattern> <color>`
- - `... remove <index>`
- - `... add <pattern> <color>`
- - `... insert <index> <pattern> <color>`
- - `... base ...`
- - - `... get`
- - - `... set <color>`
- - - `... remove`
- - `... clear`
- - `... clear before <index>`
- - `... clear after <index>`
- `... color ...`
- - `... get`
- - `... set <color>`
- - `... remove`
- `... component ...`
- - `... get [<component>]`
- - `... set <component> <value>`
- - `... remove <component>`
- `... count ...`
- - `... get`
- - `... set [<count>]`
- - `... add [<count>]`
- - `... take [<count>]`
- - `... max ...`
- - - `... get`
- - - `... set <count>`
- - - `... reset`
- - `... stack`
- `... data <source> ...`
- - `... get [<path>]`
- - `... append <path> <value>`
- - `... insert <path> <index> <value>`
- - `... prepend <path> <value>`
- - `... set <path> <value>`
- - `... merge <value> [<path>]`
- - `... remove <path>`
- - `... clear`
- `... durability ...`
- - `... get`
- - `... set <durability>`
- - `... progress <progress>`
- - `... reset`
- - `... max ...`
- - - `... get`
- - - `... set <durability>`
- - - `... remove`
- - - `... reset`
- - `... unbreakable ...`
- - - `... get`
- - - `... set <unbreakable>`
- `... enchantment ...`
- - `... get [<enchantment>]`
- - `... set <enchantment> [<level>]`
- - `... remove <enchantment>`
- - `... glint ...`
- - - `... get`
- - - `... set <glint>`
- - - `... reset`
- - `... clear`
- - `... stored ...`
- - - `... get [<enchantment>]`
- - - `... set <enchantment> [<level>]`
- - - `... remove <enchantment>`
- - - `... clear`
- `... equip [<slot>]`
- `... get [<item>] [<count>]`
- `... head ...`
- - `... get`
- - `... set ...`
- - - `... owner <owner>`
- - - `... texture <texture>`
- - `... remove`
- - `... sound ...`
- - - `... get`
- - - `... set <sound>`
- - - `... remove`
- `... lore ...`
- - `... get [<index>]`
- - `... set <index> [<line>]`
- - `... remove <index>`
- - `... add [<line>]`
- - `... insert <index> [<line>]`
- - `... clear`
- - `... clear before <index>`
- - `... clear after <index>`
- `... material ...`
- - `... get`
- - `... set <material>`
- `... name ...`
- - `... item ...`
- - - `... get`
- - - `... set [<name>]`
- - - `... reset`
- - `... custom ...`
- - - `... get`
- - - `... set [<name>]`
- - - `... remove`
- `... potion ...`
- - `... get [<effect>]`
- - `... set <effect> <duration> [<amplifier>] [<particles>] [<icon>] [<ambient>]`
- - `... remove <effect>`
- - `... clear`
- - `... type ...`
- - - `... get`
- - - `... set <type>`
- - - `... reset`
- - `... color ...`
- - - `... get`
- - - `... set <color>`
- - - `... remove`
- `... rarity ...`
- - `... get`
- - `... set <rarity>`
- `... tooltip ...`
- - `... get <part>`
- - `... set <part> <tooltip>`
- `... trim ...`
- - `... get`
- - `... set <pattern> <material>`
- - `... remove`

## Credits:

- [MineSkin](https://mineskin.org/): Custom head textures
- [DiamondFire](https://mcdiamondfire.com/home/): Idea of such item editor
