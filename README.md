# CIT for NBT

A mod that adds Optifine's CIT feature as a standalone feature for 1.12.2.

## Usage

This mod currently only looks in the `.minecraft/resources` folder for your files.
It treats this resource folder as the "assets" folder. In other words,
a path `assets/<namespace>/textures` would be placed in `resources/<namespace>/textures`
instead. This allows compat with mods like contenttweaker, which
follows this folder style instead.

To conditionally texture an item, a `.properties` file must exist. *This file can
exist anywhere in the resources folder*, and can apply to a list of items. This currently
*requires* two fields. `items`, the target item. And `texture` or `model`, which can be any texture
or valid item. All other fields are currently optional.

Example:
```
items=minecraft:stick
nbt.display.Name=contains:Glow
texture=cit:items/stick_glow
```

**Note:** `texture` does NOT need to be in the same namespace as the item.

## Behavior

- The NBT condition `exists` must be either `true` or `false`
- Relative paths do not work like Optifine's do
- Aside from the one exception above, the *file format* is stable. Unless 
a serious bug occurs, you can use the same `.properties` file in any version.

## Current Features

- NBT conditions
  - Matches: `nbt.tag.path=matches:ExactNBTMatch`
  - Contains: `nbt.tag.path=contains:TextToContain`
  - Exists: `nbt.tag.path=exists:true`
- Basic model support (not fully tested)
- mcmeta animation support
- Hot reload
- Multi item support `items=minecraft:paper minecraft:stick`

## Possible future features

- Regular Resourcepack Support
- More Optifine conditions
- Proper config (set primary assets folder)