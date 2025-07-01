# CIT for NBT

A mod that adds Optifine's CIT feature as a standalone feature.
Currently uses pre-baking to ensure good performance.

## Usage

This mod currently only looks in the resources folder for your files.
It treats this resource folder as the "assets" folder. In other words,
a path `assets/<namespace>/textures` would be placed in `resources/<namespace>/textures`
instead. This allows compat with mods like contenttweaker, which
follows this folder style instead.

To conditionally texture an item, a `.properties` file must exist. *This file can
exist anywhere in the resources folder*. This currently *requires* two fields. 
`match`, the target item. And `texture`, which can be any texture. 
All other fields are currently optional.

```
match=minecraft:stick
nbt.display.Name=contains:Glow
texture=cit:items/stick_glow
```

**Note:** `texture` does NOT need to be in the same namespace as the item.


## Current Features

- NBT conditions
  - Matches: `nbt.tag.path=matches:ExactNBTMatch`
  - Contains: `nbt.tag.path=contains:TextToContain`
  - Exists: `nbt.tag.path=exists`
- Basic model support (not fully testec)
- mcmeta animation support

## Possible future features

- Hot reload - Based off DynamicTexture, not BakedModels
- Regular Resourcepack Support, same as hot reload
- Exists true or false to allow more conditions
- More Optifine conditions