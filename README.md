# Hollow Hands

Minecraft 26.3 Fabric client mod.

## Build
Requires Java 25 and Gradle 9.6:
`gradle clean build`

## Controls
Press **G** in a world to open Carrying Gear. Change the key in Options → Controls → Key Binds.

## Storage layout
- Belt: 2 slots
- Back: 6 slots
- Satchel: 9 slots
- Bandolier: 4 slots

Select a compartment, select a slot, use **Store held stack** to store the main-hand stack, or **Take selected slot** to retrieve it into an empty selected hotbar slot.

## Current implementation boundary
This first functional storage pass persists item identifiers and counts in the local client config file `config/hollowhands-storage.properties`. It is a local client stash, not server-authoritative inventory: it is intended for single-player prototyping and does not yet synchronize to multiplayer servers. Item metadata/components are not preserved in this first pass.
