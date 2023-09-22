---
layout: page
title: Universe
subtitle: Things that are generated for you upfront
menubar: data_menu_chassis
toc: false
show_sidebar: false
hero_image: ../../assets/Chassis.png
---
# Universe: Things that are generated for you upfront

We also need some "fix" classes that we use in generated code, like

- Annotations
- `object Defaults` with constant (like) definitions, e.g.
  - `DEFAULT_STRING`, `NULL_STRING`, `DEFAULT_UUID`, `NULL_UUID`, `DEFAULT_INSTANT`, `NULL_INSTANT`
- common base interface `WasGenerated`
- a `class Dummy`
- common base implementations, e.g.
  - `IUuidDto`, `UuidTable`
- Helper Functions to be "DRY'
  - PoetHelpers

Chassis will use its internal kotlinPoet to generate these for you

in a fixed package name and structure.

See `object com.hoffi.chassis.shared.fix.Universe`

<a onclick="window.history.back()">Back</a>

<hr/>

[back to root](..)
