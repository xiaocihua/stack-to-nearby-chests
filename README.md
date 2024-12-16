**English** | [简体中文](README-zh_CN.md)
# Quick Stack to Nearby Chests
A client-side ONLY mod, adds the quick stacking and restocking features from Terraria into Minecraft.

The code to iterate over all nearby containers is from [clientcommands](https://github.com/Earthcomputer/clientcommands) mod.

## Installation
1. [Install Fabric Loader](https://fabricmc.net/wiki/player:tutorials:start#installing_fabric_loader)
2. Download the [Fabric API](https://minecraft.curseforge.com/projects/fabric)
3. Download this mod from the [releases page](https://github.com/xiaocihua/stack-to-nearby-chests/releases)
4. Place the two mods downloaded above in the `mods` folder

## Features
- **Press Left Ctrl + S + C to open the mod options screen and configure the mod's appearance, behavior, and key mappings.**


- **Quick stack** If the container contains at least one of an item, move the same item from the player's inventory into the container.
- **Restock** Refill stackable items in the player's inventory to a full stack using items from the container. 
- Perform the above two actions using buttons or shortcut keys in the container screen. Items that will not be stacked or restocked can be specified in the mod options (default: shulker box items, bundle items).


- **Quick stack to nearby containers** Perform Quick Stack on all openable containers within the player's reach.
- **Restock from nearby containers** Restock stackable items from all openable containers within the player's reach.
- Perform Quick Stack or Restock for nearby containers using buttons or shortcut keys in the player inventory screen, or shortcut keys when no screen is open. Containers as stacking targets and restocking sources can be specified in the mod options (default: chests, trapped chests, barrels, shulker boxes, ender chests, boats with chests, minecarts with chests, donkeys, mules, llamas).
- **Selective Quick Stack**  Use shortcut keys or drag and drop items to the "Quick Stack to Nearby Containers" button to quick stack only a specific item type from the player's inventory into nearby containers.


- **Favorite items** Hold Left Alt and right-click an item to favorite it. Favorite items cannot be Quick Stacked but otherwise behave like normal items. Actions such as throwing with the Q key, picking up, quick-moving, and swapping can be disabled separately for favorite items in the mod options. Items will be unfavorited when removed from player inventory. This feature is unavailable in Creative Mode, and switching to Creative Mode will clear the favorite status of all items.

## Acknowledgements
- All the [**contributors**](https://github.com/xiaocihua/stack-to-nearby-chests/graphs/contributors). Please accept my heartfelt thanks.
- [clientcommands](https://github.com/Earthcomputer/clientcommands)
- [MonsterTrex](https://github.com/MonsterTrex), for providing me with valuable advice
- qing_ru, for improving translations