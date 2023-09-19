**English** | [简体中文](README-zh_CN.md)
# Stack to Nearby Chests
A client-side ONLY mod, adds the quick stacking and restocking features from Terraria into Minecraft.

The code to iterate over all nearby containers is from [clientcommands](https://github.com/Earthcomputer/clientcommands) mod.

## Installation
1. [Install Fabric Loader](https://fabricmc.net/wiki/player:tutorials:start#installing_fabric_loader)
2. Download the [Fabric API](https://minecraft.curseforge.com/projects/fabric)
3. Download this mod from the [releases page](https://github.com/xiaocihua/stack-to-nearby-chests/releases)
4. Place the two mods downloaded above in the `mods` folder

## Features
- **Press Left Ctrl + s + c to open the mod options screen, configure the mod's appearance, behavior and key mapping**


- **Quick stack** If the container contains at least one of an item, move the same item from player inventory into the container.
- **Restock** Refills the stackable items in player inventory to a full stack using items from the container. 
- The above two operations can be performed using buttons or shortcut keys in the container screen.
Items that will not be stacked and items that will not be restocked can be specified in the mod options (default: shulker box items).


- **Quick stack to nearby containers** Perform Quick Stack on all openable containers within the player's reach.
- **Restock from nearby containers** Restock with all openable containers within the player's reach.
- The above two operations can be performed using buttons or shortcut keys in the player inventory screen, 
or using shortcut keys when no screen is open.
Containers as stacking targets and restocking sources can be specified in the mod options (default: chests, trapped chests, barrels, shulker boxes, ender chests).
- **Quick stack all items of one type to nearby containers** via shortcut keys or by dragging and dropping items to the "Quick stack to nearby containers" button.


- **Favorite items** Hold Left Alt down right-clicking the item to favorite it. Favorite items cannot be Quick Stacked, otherwise behave like normal items.
Throw with Q key, pick up, quick-move and swap can be disabled separately for favorite items in the mod options. Items will be unfavorited after being removed from the player inventory.
This feature is not available in creative mode, switching to creative mode from another mode will clear the flags of all favorite items.

## Acknowledgements
- All the [**contributors**](https://github.com/xiaocihua/stack-to-nearby-chests/graphs/contributors). Please accept my heartfelt thanks.
- [clientcommands](https://github.com/Earthcomputer/clientcommands)
- [MonsterTrex](https://github.com/MonsterTrex), for providing me with valuable advice
- qing_ru, for improving translations

