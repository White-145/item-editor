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
- - `... set <custom model data>`
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

(literally only 1 command)