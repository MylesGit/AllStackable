# All Stackable Continued

**An updated version of [AllStackable](https://github.com/KrisCris/AllStackable)**

***Ever wanted to stack potions, totems, or Shulker Boxes in one slot? Or perhaps you hope to sort totems produced by the raid farm using your Redstone machines? This is the mod for you!***

## Download

All Stackable (**Original**)
**[Modrinth](https://modrinth.com/mod/all-stackable)**

**All Stackable Continued** PLACEHOLDER
**[Modrinth](https://modrinth.com/mod/all-stackable)**

## Usage

- `/allstackable show all`: List all modified items.
- `/allstackable show [item]`: Show information about a specified item.
- `/allstackable show hand [player]`: Show information about an item in the player's hand.
- `/allstackable reset all`: Reset all changes.
- `/allstackable reset [item]`: Reset a specified item.
- `/allstackable reset hand [player]`: Reset an item in a player's hand.
- `/allstackable set [itemID] [count]`: Set the max stackable count of a specified item.
- `/allstackable set hand [player]`: Set the max stackable count of an item in a player's hand.
- `/allstackable set vanilla [default value] [new value]`: Apply **new value** to the stackable size of items whose default size is **default value**. (Only works for items that haven't been modified.)
- `/allstackable set modified [current value] [new value]`: Apply **new value** to the stackable size of items whose current size is **current value**. (Only works for items that have been modified.)
- `/allstackable set all [current value] [new value]`: Apply **new value** to the stackable size of items whose current size is **current value**. (Applies to all items.)
- `/allstackable config reload`: Reload config from file.
- `/allstackable config loadFromGlobal`: Load the global config (stored in Minecraft's config folder) to your game.
- `/allstackable config saveToGlobal`: Let your current config be the global one.
- `/allstackable config globalConfigAutoApply [true/false]`: Whether the global config will be automatically applied to every new game.
- `/allstackable config restore`: Restore to last change.


- **If you want to use this mod without operator, you can manually set the "permissionLevel" from 4 to 0 in the config file (in your save folder).**
- **If you want to only stack empty shulkerbox, change the setting manually in the config files (0 -> 1).**

## FAQ

[1]. Where is the config file stored?

**They are stored in the folder of your world. (There is also a global config, which would apply its settings to all new saves. Only works after you turned it on.)**

[2]. Can I migrate the config from one world to another?

**Just copy and paste the config file to your new world, then use `/allstackable reload` if you're already in the world.**

[3]. What will happen to my world if I delete this mod?

**Don't worry, this mod updates items stack size dynamically and changes NOTHING about the data (Though you have to manually split those items that are already stacked). If you reinstall this mod, all features will come back as long as you didn't remove the config file.**

[4]. Server?

**This mod is required on both servers and clients. The server will automatically let clients know if any item needs to be modified and synced.**

[5]. Sorting non-stackable items that are made stackable by this mod.

**Identical to sorting normal items.**

[6]. Surpass 64-item limit?

**Not supported.**

[7]. I found a bug! / This mod doesn't work! / Any other Issues

**[Report!](https://github.com/MylesGit/AllStackable/issues)**

[8]. Why fork the original repository? (https://github.com/KrisCris/AllStackable)

**The original author seems to not be maintaining the original repository. The sole purpose of this fork is to implement some pull requests that have been sitting stale for a long time and fix various issues that I currently would like. I do not plan on this being a hard fork; Meaning, I will probably cease development when or if the orignal author comes back.**

## Notice:

(Some issues related to how Minecraft's code works)

**[1].** Have a test before using/consuming certain types of stacked items (especially those having special functionalities), since you may lose them all or encounter unexpected behaviors. (Even though most issues are fixed in previous versions, please let me know if you found new bugs.)

**[2]. I am not the orignal author. You can find them here: https://github.com/KrisCris**

## Contributors

- luaanon
- MeeniMc
- chimericdream
- triphora
- zhaixianyu
