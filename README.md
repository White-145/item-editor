# Simple Item Editor

Simple item editor is minecraft mod (fabric 1.20.1) for item editing, that uses client commands.

Not done yet, but feel free to build, use, and suggest new features to add (especially translations, if someone is good at it please message me/pull request/create an issue with translations)

## Features:

### `/edit` command:
- `... name ...`
- - `... get`
- - `... set [<name>]`
- - `... reset`
- `... lore ...`
- - `... get [<index>]`
- - `... set <index> [<line>]`
- - `... insert <index> [<line>]`
- - `... add [<line>]`
- - `... remove <index>`
- - `... clear`
- - `... clear before|after <index>`
- `... material ...`
- - `... get`
- - `... set <material>`
- `... count ...`
- - `... get`
- - `... set|add|remove [<count>]`
- - `... stack`
- `... model ...`
- - `... get`
- - `... set [<model>]`
- `... enchantment ...`
- - `... get [<enchantment>]`
- - `... set <enchantment> [<level>]`
- - `... remove <enchantment>`
- - `... clear`
- - `... glint`
- `... get <item> [<count>]`
- `... attribute ...`
- - `... get [<slot>] [<attribute>]`
- - `... set [<attribute> <amount>|infinity] [<operation>] [<slot>]`
- - `... remove <attribute> [<slot>]`
- - `... clear [<slot>]`
- `... equip`
- `... color ...`
- - `... get`
- - `... set [<color>]`
- `... unbreakable`
- - `... get`
- - `... toggle`
- `... flags ...`
- - `... get [<flag>]`
- - `... toggle <flag>`
- `... whitelist ...`
- - `... get [place|destroy]`
- - `... add place|destroy <block>`
- - `... remove place|destroy <block>`
- - `... clear [place|destroy]`
- `... durability ...`
- - `... get`
- - `... set [<durability>]`
- - `... percent <durability>`
- `... data ...`
- - `... get <path>`
- - `... set <path> <value>`
- - `... merge <value>`
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
- - `... set owner <owner>`
- - `... set texture <texture>`
- - `... sound ...`
- - - `... get`
- - - `... set <sound>`
- `... trim ...`
- - `... get`
- - `... set [<pattern> <material>]`
- `... firework ...`
- - `... flight ...`
- - - `... get`
- - - `... set [<flight>]`
- - `... star ...`
- - - `... get`
- - - `... add <type> <colors> [<flicker>] [<trail>] [<fadeColors>]`
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

All commands are also available as `/itemeditor:<command>`

#### /!\ NOT DONE SECTION /!\
- `... items ...`
- `... entity ...`
- `... script ...`

### `/color` command
### `/char` command