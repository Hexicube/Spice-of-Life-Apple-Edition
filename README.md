<p align="center">
	<img width=256px src="GitHub/logo.png" />
</p>

A mod designed to encourage dietary variety! It does so by **rewarding** the player for eating a variety of foods instead of **punishing** them for failing to diversify. Instead of using the stick we use the apple!

This mod is a fork of Spice of Life: Carrot edition, featuring the same max health rewards but by completing food groups instead of milestones.

### Features

The mod keeps track of how many unique foods a player has eaten. As food groups are completed by eating every food therein, they permanently gain new hearts, increasing their max health! In order to max out their HP bars, players will have to quest for and craft exotic foods.

This mod comes with a custom config to set the player's starting hearts (which can be less than 10 if desired) as well as the number of and contents of food groups they need to complete by eating every food. The number of hearts per group is also configurable.

The default configuration will have 10 base hearts and the following groups, most offering 1 heart and golden offering 5 hearts:

- **Harvestables**: Carrot, Beetroot, Apple, Potato
- **Fish**: Cooked Cod, Cooked Salmon, Tropical Fish
- **Produce**: Bread, Baked Potato, Pumpkin Pie, Beetroot Soup, Rabbit Stew, Mushroom Stew
- **Meat**: Cooked Porkchop, Cooked Mutton, Cooked Chicken, Steak, Cooked Rabbit
- **Treats**: Sweet Berries, Honey Bottle, Glow Berries, Melon Slice, Cookie
- **Golden**: Golden Apple, Golden Carrot, Enchanted Golden Apple

In order to track your progress, SoL: Apple offers a handy book called the Food Book, crafted simply by combining a book and an apple in any shape. This book offers a visualization of your overall progress, as well as all the food groups and what's been eaten from them.

### Commands

This mod also features 3 commands:

- /solapple size  
    Tells you the number of unique foods you've eaten, as well as how many groups are unfinished.
- /solapple clear  
    Clears the stored list of unique foods a player has eaten and resets their heart count. This is useful for testing when editing the config or when you want to start over.
- /solapple sync  
    Forces a sync of the food list to the client, for when something went wrong and it's mismatched.

### Notes
- SoL: Apple has two places to configure it. The client-side configs (like visual options) are in the regular `config` folder and aren't synced between server and client. The server-side configs (food groups and such) are stored in a serverconfig folder within each save and synced to the client. You can provide default values for these by placing a copy in the `defaultconfigs` folder, i.e. at `defaultconfigs/solapple-server.toml`.
- Food groups are currently configured as a JSON string as I have no idea how to dynamically read server config files for food groups.