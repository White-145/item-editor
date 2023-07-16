My minecraft mod (fabric 1.20.1) for item editing, that uses client commands.

Not done yet, but feel free to build, use, and suggest new features to add (especially translations, if someone is good at it please message me/pull request/create an issue with translations)

Features:

`/edit` command:
- `... name ...`
- - `... get`
- - `... set [<name>]`
- - `... reset`
- `... lore ...`
- - `... get [<index>]`
- - `... add [<line>]`
- - `... set <index> [<line>]`
- - `... insert <index> [<line>]`
- - `... remove <index>`
- - `... clear`
- - `... clear before <index>`
- - `... clear after <index>`
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
- - `... reset`
- `... enchantment ...`
- - `... get <enchantment>`
- - `... set <enchantment> [<level>]`
- - `... remove <enchantment>`
- - `... clear`
- - `... glint`
- `... get <item> [<count>]`
- `... attribute ...`
- - `... set [<attribute> <amount>|infinity] [<operation>] [<slot>]`
- - `... clear [<slot>]`
- - `... get [<attribute>] [<slot>]` TODO
- - `... remove [<attribute>] [<slot>]` TODO
- `... equip`
- `... color ...`
- - `... get`
- - `... set <color>`
- - `... reset`
- `... unbreakable`
- `... flags ...`
- - `... get [enchantments|attributes|unbreakable|candestroy|canplaceon|others|dyed|trim]`
- - `... set enchantments|attributes|unbreakable|candestroy|canplaceon|others|dyed|trim`
- `... whitelist ...`
- - `... get [place|destroy]`
- - `... add <block>`
- - `... add place|destroy <block>`
- - `... remove <block>`
- - `... remove place|destroy <block>`
- - `... clear [place|destroy]`
- `... durability ...`
- - `... get`
- - `... set [<durability>]`
- - `... percent <durability>`
- `... data ...`
- - `... get <path>`
- - `... set <path> <value>`
- - `... merge <path> <value>`

Also available as `/itemeditor:edit`

(literally only 1 command)