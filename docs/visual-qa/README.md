# Runtime and Visual QA

This checklist is the repeatable creative-world acceptance pass for player-facing
Galactic Wars content. Run it with the built JAR in the GDLauncher NeoForge 26.2
instance and YACL plus GeckoLib enabled.

## 2026-07-21 complete 3D visual overhaul

- The full generator was run twice and 764 tracked asset outputs retained identical SHA-256 hashes.
  The turnaround renderer produced 54 front/side/back inspections across the seven sheets in
  `build/previews/turnarounds`.
- Fabric and NeoForge development clients both reached resource reload with the Galactic Wars client
  foundation active. GeckoLib reported 49 models and 49 animations on each loader, with no missing or
  invalid `galacticwars` model, texture, animation, or GeckoLib parser warning.
- Minecraft 26.2 emits one loader-level `Missing block model: minecraft:builtin/entity` warning on
  each loader. `builtin/entity` is the GeckoLib-required base for special item rendering; there is no
  missing project resource behind this warning.
- Automated desktop capture could enumerate the GLFW client windows but could not activate them.
  The 54 deterministic turnaround views are reviewed evidence, but interactive NPC animation,
  equipped/ridden vehicles, held-item clipping, and every GUI/first/third/dropped/framed context remain
  a manual release pass and are not claimed by this run.

## 2026-07-20 NPC and clone-armor overhaul

- `tools/generate_character_models.py` was run twice after the source import and every character
  output hash remained identical. `tools/render_character_previews.py` produced
  `build/previews/recruit_models.png` and `build/previews/armor_models.png` for native-resolution review.
- `runHarnesses`: passed all 136 tasks, including mixed-resolution 22-NPC geometry, signed per-face
  UV bounds, source-pixel equality, named silhouettes, bone parents, animation references, registry,
  item, recipe, provenance, and manifest gates.
- `buildAll`: passed all 144 tasks for Fabric and NeoForge.
- `runGameTestServer`: all 55 required tests passed. Both Phase I entities and
  spawn eggs loaded their unit data and complete armor loadouts; Phase I hiring
  satisfied the generic clone campaign objective, persisted, and joined the
  Republic outpost profile.
- Development client: resource reload completed after the permissioned import with GeckoLib reporting
  35 models and 35 animations. `neoforge/run/logs/latest.log` contains no Galactic Wars missing-model,
  missing-texture, missing-animation, GeckoLib parser, or renderer-loading error.
- Visual review: Phase I/II helmets, regular/ARC equipment, Forge-derived B1 silhouette, and four
  Galaxies-derived Mandalorian loadouts are distinguishable in the generated preview. The preview
  renderer does not evaluate every hierarchical animation or held-item transform.
- Interactive arena captures for all 22 front/side/back and first/third-person
  armor views remain a manual release check; Windows exposed duplicate,
  indistinguishable GLFW windows to the automation controller, so no visual
  pass is claimed here.

## Automated gates

- `clean build`: passed on 2026-07-14.
- `runGameTestServer`: 55/55 required GameTests passed, including direct
  Command Center player interaction, all five campaign paths, NPC brains,
  vehicles, Force abilities, multiplayer authority, travel, and persistence.
- Spawn eggs: all 22 registry entries passed player-game-mode interaction,
  entity-type component, persistence, loadout/archetype, creative/survival,
  replaceable-block, custom-name, stack-consumption, and spawner checks.
- Assets: every authored item model has a modern item definition and every
  inherited model texture resolves to a PNG.
- Client log: no `galacticwars` missing-model or missing-texture warning.

## Command Center client acceptance

The 2026-07-14 fresh creative-world pass used a 571x350 client window to stress
the responsive layout. The Command Center rendered with its directional model,
opened the five-faction pledge screen on placement, committed a Galactic
Republic pledge, and reopened into the synchronized operations dashboard.

- Overview, Campaign, Construction, Squads, Workforce, Kingdom, Diplomacy, and
  Storage tabs rendered and remained usable at the small viewport.
- Multi-page action controls advanced and returned without overlapping the
  dashboard frame.
- Campaign Chapter 1 showed the pledged and Command Center requirements as
  complete and named the physical Clone Trooper hiring objective.
- World save, integrated-server shutdown, and client exit completed cleanly.
- Labels found too long during the pass were shortened before the final build.

## Lightsaber acceptance

The six colors inherit one seventeen-part hilt/blade geometry and override only the
hilt and animated blade textures. The first-person extension preserves the
vanilla hand transform and adds only the ready/slash offset.

The rebuilt visual language was developed from the original project-owned
reference in `lightsaber-design-reference.png`, generated for this repository
on 2026-07-13. The shipped 32-pixel material atlases are deterministic original
pixel art produced by `tools/generate-lightsaber-textures.ps1`; no third-party
mod textures or geometry are included.

| Context | Result | Evidence |
| --- | --- | --- |
| Creative inventory/GUI | Pass | `lightsaber-gui.png` |
| First person | Pass | `lightsaber-first-person.png` |
| Third person | Recheck after pose/transform changes | Capture front and back |
| Dropped item | Recheck on a clear stone floor | Capture after one animation cycle |
| Item frame | Recheck on a plain wall | Capture fixed transform |

## Content audit order

Use daylight, a plain stone floor, hitboxes off, and the same camera distance.
Capture idle, walk, attack/held-item, and seated views where applicable.

1. NPCs: all 27 egg variants; verify body scale, seams, surfaces, held items,
   armor, idle/walk/attack clips, and neutral ownership.
2. Vehicles: BARC speeder, AT-RT, STAP, AAT, and LAAT gunship; verify kit icon,
   deployed model, collision box, rider seat, and animation.
3. Weapons and armor: all lightsabers, blasters, tools, and faction armor.
4. Blocks and kits: command/logistics blocks, beacons, transponders, deployment
   kits, and environmental blocks in GUI, placed, dropped, and framed views.

Any missing `galacticwars` model/texture line in `latest.log` is a release
failure, even if Minecraft substitutes a fallback model.

## Automated turnaround command

Run `python tools/render_asset_turnarounds.py` with a Pillow-enabled Python. Seven QA sheets are
written under `build/previews/turnarounds` for 27 NPCs, commanders, armor, vehicles, lightsabers,
blasters, and capsules. Each cell contains front/side/back orthographic views after local and bone
hierarchy rotations. These sheets are diagnostic evidence; NeoForge and Fabric client passes remain
the authority for hand anchors, riding, GUI lighting, and animation.

The active checklist is 27 NPCs, all three commander variants, six armor families, five vehicles,
six saber colors, and four blasters in GUI, first person, third person, dropped, and item-frame views.
