My minecraft mod (fabric 1.20.1) for item editing, that uses client commands.

Not done yet, but feel free to build, use, and suggest new features to add (especially translations, if someone is good at it please message me/pull request/create an issue with translations)

Features:

`/edit` command:
- `... name get`
- `... name set [<new name>]`
- `... name reset`
- `... lore get [<index>]`
- `... lore add [<line>]`
- `... lore set <index> [<line>]`
- `... lore insert <index> [<line>]`
- `... lore remove <index>`
- `... lore clear`
- `... lore clear before <index>`
- `... lore clear after <index>`
- `... material set <material>`
- `... count get`
- `... count set [<count>]`
- `... count add [<count>]`
- `... count remove [<count>]`
- `... model get`
- `... model set <custom model data>`
- `... model reset`
- `... enchantment get <enchantment>`
- `... enchantment set <enchantment> [<level>]`
- `... enchantment remove <enchantment>`
- `... enchantment clear`
- `... enchantment glint`
- `... get <item> [<count>]`
- `... attribute set [<attribute> <amount>|infinity] [<operation>] [<slot>]`
- `... attribute clear [<attribute>] [<slot>]`
