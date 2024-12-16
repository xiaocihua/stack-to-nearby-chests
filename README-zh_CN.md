[English](README.md) | **简体中文**

# 快速堆叠到附近的箱子
一个客户端模组，将《泰拉瑞亚》中的快速堆叠和补货功能添加到《我的世界》。

遍历附近所有容器的代码来自 [clientcommands](https://github.com/Earthcomputer/clientcommands) 模组。 

## 安装
1. [安装 Fabric Loader](https://fabricmc.net/wiki/player:tutorials:start#installing_fabric_loader)
2. 下载 [Fabric API](https://minecraft.curseforge.com/projects/fabric) 
3. 从 [发布页面](https://github.com/xiaocihua/stack-to-nearby-chests/releases) 下载本模组
4. 将上面下载到的两个模组放置在`mods`文件夹中

## 功能
- **按 左Ctrl + s + c 打开模组选项界面，配置模组的外观、行为和按键映射。**  


- **快速堆叠** 如果容器包含某种物品至少一个，就将玩家物品栏中的同种物品移动到容器里。
- **补货** 使用容器中的物品补充玩家物品栏中的可堆叠物品到一整组。
- 上述两个操作可以在打开的容器界面中使用按钮或快捷键执行。可以在模组选项中指定不会受堆叠操作影响的物品和不会受补货操作影响的物品（默认：潜影盒物品、收纳袋物品）。


- **快速堆叠到附近的容器** 对玩家触及范围内的所有可以打开的容器执行快速堆叠。
- **从附近的容器中补货** 使用玩家触及范围内的所有可以打开的容器进行补货。
- 上述两个操作可以在玩家物品栏界面使用按钮或快捷键，或者在未打开任何界面的情况下使用快捷键执行。可以在模组选项中指定作为堆叠目标和补货来源的容器种类（默认：箱子、陷阱箱、木桶、潜影盒、末影箱、运输船、运输矿车、驴、骡、羊驼）。
- 通过快捷键或拖放物品到“快速堆叠到附近的容器”按钮上，可仅让这一类物品快速堆叠到附近的容器。


- **收藏物品** 使用 左Alt + 鼠标右键 点击一个物品可以将其标记为收藏。收藏的物品不会被堆叠到容器里，除此之外的行为和普通物品一样。可以在模组选项中分别禁用对已收藏物品的抓取、快速移动、数字键交换和 Q 键丢弃操作。移出玩家物品栏之后物品会被取消收藏。此功能无法在创造模式中使用，从其他模式切换到创造模式会清除对所有已收藏物品的标记。

## 鸣谢
- 所有[**贡献者**](https://github.com/xiaocihua/stack-to-nearby-chests/graphs/contributors)。请接受我衷心的感谢。
- [clientcommands](https://github.com/Earthcomputer/clientcommands)
- [MonsterTrex](https://github.com/MonsterTrex)，为我提供了宝贵的建议
- qing_ru，改进翻译
