# Galactic Wars Asset Provenance

## 2026-07-27 project-owned Dathomir character direction

The Nightsister Acolyte, Nightsister Archer, Nightsister Civilian, and Nightbrother Brute were
rebuilt as four distinct deterministic GeckoLib sets. The project-owned concept board is retained
at `tools/source_art/generated_dathomir_faction_orthographic_concept_2026-07-27.png` with SHA-256
`6323e502cdeca8748202c2f78009e4996ed7517e2401d7fbd74a0dc7a5990412`. It was created in one call
with the built-in ImageGen workflow and no input/reference images. It is design direction only:
`tools/generate_character_models.py` authors the shipped cuboids, exact UV allocations, pixel
materials, animations, spawn-capsule atlases/glowmasks, and fallback icons deterministically.

Exact prompt:

```text
Use case: stylized-concept
Asset type: project-owned orthographic character concept board for a Minecraft GeckoLib NPC model and texture production pipeline
Primary request: create one cohesive design board showing four original Dathomir-inspired dark-side faction roles: a ritual Acolyte, a bow-wielding Archer, a noncombatant Weaver/Civilian, and a powerful horned Nightbrother Brute. The board will guide distinct block-model silhouettes, UV-safe pixel-art materials, and role-specific animation.
Scene/backdrop: clean warm-gray studio concept-sheet background with four evenly spaced character columns; no environment or props beyond each role's equipment.
Subject: each role shown as a full-body front, side, and back orthographic turnaround. Acolyte has a tall split hood, layered asymmetrical wine-red and charcoal wraps, bone talismans, pale face with restrained angular markings, and subtle spectral-green ritual cords. Archer is leaner, with a swept headwrap and hair silhouette, fitted layered wraps, high back quiver, recurved energy-bow grip cues, and red/black materials with very sparse green accents. Civilian is clearly noncombatant: matron/weaver silhouette, broad draped shawl, braided or wrapped hair, woven apron, satchel and loom-tool details, muted burgundy/plum/charcoal palette, no weapon. Nightbrother is tall and broad with a distinct crown of short horns, red-orange skin, bold dark geometric tattoos, heavy asymmetric shoulder harness, layered waist cloth, forearm wraps, and grounded boots.
Style/medium: premium stylized voxel-game character concept art; block-model-aware but much more refined than default Minecraft humanoids; readable stepped silhouettes, layered cloth thickness, crisp material separation, hand-painted pixel-art surface cues; original project-owned interpretation, not copied from any existing game/mod asset.
Composition/framing: landscape board, all twelve turnaround views fully visible at matching scale, neutral A-pose, strict orthographic front/side/back, generous spacing.
Lighting/mood: soft neutral studio lighting, ominous ceremonial elegance, strong readable values.
Color palette: charcoal, oxblood, burgundy, plum, ash gray, bone, restrained spectral green; Nightbrother adds red-orange skin and near-black tattoos.
Materials/textures: layered woven cloth, leather wraps, bone/metal talismans, subtle embroidery, matte painted skin, no glossy plastic.
Constraints: four roles must be unmistakably different in silhouette; designs must translate into cuboid GeckoLib geometry and 256x256 texture atlases; preserve humanoid hands for held-item anchors; no capes obscuring the back view; no photorealism; no text, labels, logos, watermark, UI, scenery, extra characters, or cropped limbs.
Avoid: generic Steve proportions, identical robes, neon overload, modern clothing, firearms, iron swords, sexualized clothing, smooth high-poly anatomy, copyrighted character likenesses.
```

The Acolyte uses a split ritual hood, layered mantle, long asymmetrical panels, talismans, and
animated ritual cords. The Archer uses a lean fitted silhouette, swept headwrap, independently
animated hair, high quiver, split panels, and a bow-draw attack pose. The unarmed Civilian uses a
broad shawl, braided crown, woven apron, satchel, and restrained defensive motion. The Nightbrother
uses a wider body, two-part horn crown, facial/chest tattoos, asymmetric spiked shoulder harness,
waist cloth, and a heavy attack motion. Registry IDs, canonical humanoid bones, held-item anchors,
and the `misc.idle`, `move.walk`, `move.run`, and `attack.swing` animation names remain stable.

### Runtime orthographic revision

The implementation-focused follow-up is retained at
`tools/source_art/dathomir_runtime_orthographic_concept_2026-07-27.png` with SHA-256
`adad81527f8d7ada2f941eb925065ee57d7af0595eaadea7e5563ce26238be84`. It was generated with the
built-in ImageGen workflow using the user-approved Dathomir quartet render as a visual reference.
It is a direction sheet only; no concept pixels are copied into a runtime atlas.

The companion face-and-material study is retained at
`tools/source_art/dathomir_materials_face_cloth_leather_bone_markings_2026-07-27.png`
with SHA-256 `a44a533dec7f3414707c7304f42f803191f51cc86ab195859d284fc9ec4a798e`.
It was generated with the built-in ImageGen workflow from the same user-approved visual
reference and is also direction-only. Its exact prompt is stored beside it as
`dathomir_materials_face_cloth_leather_bone_markings_2026-07-27.prompt.md`.

Exact prompt:

```text
Use case: stylized-concept
Asset type: implementation-focused orthographic model sheet for a Minecraft/GeckoLib voxel NPC asset pipeline
Primary request: Translate the approved Dathomir character reference into a clean production model sheet for exactly four separate block-model characters: Nightsister Acolyte, Nightsister Archer, Nightsister Civilian, and Nightbrother Brute. Preserve the approved reference's quality, palette, costume logic, faces, and layered silhouette, but simplify every form into geometry that can actually be built from axis-aligned cuboids and thin voxel planes.
Input image: Image 1 is the approved visual-quality and character-design reference; use it for all four roles, materials, colors, and silhouette identity.
Scene/backdrop: neutral light-gray studio background with no scenery, floor clutter, labels, text, UI, watermark, or weapons floating separately.
Subject: show each of the four roles as a distinct full-body orthographic turnaround with front, exact side, and back views aligned at consistent scale. Acolyte has a tall split ritual hood, deep layered mantle and robe, pale painted face, bone talismans, belts and restrained green ichor cords. Archer is lean, has tied swept black hair with red wrap, asymmetric skirt, bracers, quiver with visible arrows, and carries a compact Dathomir bow. Civilian is older and practical with gray segmented hair, lowered hood, layered burgundy shawl, woven apron, hand wraps, utility belt and pouches. Nightbrother is substantially broader and taller, red-and-black geometric skin markings, crown horns, tusks, heavy wraps, asymmetric spiked mantle, crossed bandolier, bone trophies, waist cloth, and armored boots.
Style/medium: high-end Minecraft-compatible voxel concept render, crisp authored pixel textures at believable 256x256-atlas density, visible face painting, cloth weave, stitched seams, worn leather, carved bone, chipped horn, and controlled color variation; no smooth sculpting, rounded anatomy, photoreal skin, or non-voxel fabric.
Composition/framing: landscape sheet, four clearly separated role columns, each role's front/side/back views grouped together and fully visible, consistent camera and lighting; prioritize readable silhouette and buildable layer breakdown.
Lighting/mood: soft neutral studio lighting, restrained shadows, clear material readability.
Color palette: charcoal, near-black, oxblood, burgundy, rust red, muted brown leather, aged ivory bone, gray-white skin paint, tiny restrained green ichor accents only on the Acolyte.
Constraints: exactly four roles; keep designs culturally cohesive but instantly distinguishable; all accessories must be attached to characters and feasible as child bones; hands and forearms remain clear enough for runtime item anchors; no clone troopers, droids, blasters, lightsabers, extra people, logos, typography, captions, watermark, cinematic background, or painterly blur.
```

## 2026-07-22 subtitle-backed runtime sound identities

The `galacticwars` blaster, lightsaber, Force, vehicle, travel, construction, mission, and planet
ambience sound events are original event identities and subtitles. For the 1.0 cohesion pass they
reference Minecraft's installed runtime sound events through `sounds.json` event aliases; no audio
files are copied or redistributed. This keeps the initial sound pass compatible with the host game
license while leaving the event identities stable for later project-owned recordings.

## 2026-07-20 permissioned upstream character-art replacement

The visually rejected generated clone, B1, and Mandalorian sets were replaced with authored source
art from two pinned repositories. `tools/generate_character_models.py` still creates the unmatched
NPC families and animations, then calls `tools/import_authorized_character_assets.py` last so the
permissioned conversions deterministically replace the matching outputs. The unified
`source_inventory.json` records raw upstream and sanitized-vendored hashes. Vendored Blockbench
copies remove only host-local texture paths; all other inputs and source art remain unchanged.

| Source and revision | Shipped Galactic Wars outputs | Deterministic adaptation |
| --- | --- | --- |
| `Parzivail-Modding-Team/GalaxiesParzisStarWarsMod` at `b91b4cc1a827eeb7c2ae16f0b703affd78c1c206`, `Clone Trooper.bbmodel`, `Clone Trooper.png`, clone item sprites | `phase_i_clone_trooper`, `phase_i_arc_trooper`, `clone_trooper`, `arc_trooper`; `phase_i_clone` and `republic_plastoid` wearable armor; both clone item-icon sets | Select Phase I/II helmet groups; enable rangefinder, pauldron, and kama for ARCs; mirror the Blockbench X axis; preserve 128x128 per-face UVs; add GeckoLib hand anchors and armor-parent bones. Phase I pixels are copied exactly. |
| `Pyrix25633/Forge-StarWarsCloneWars` at `c9555aa4966e9e63c22a59f488d4b05bc614569e`, three 501st armor layers | Phase II clone and ARC NPC, wearable armor, and item markings | Select the dominant authored 501st blue from the source helmet texture and transfer that palette only to UV-compatible helmet, torso, arm, and leg regions of the Galaxies atlas. |
| `Pyrix25633/Forge-StarWarsCloneWars` at `c9555aa4966e9e63c22a59f488d4b05bc614569e`, `Droid.bbmodel` and battle/security/pilot/commander atlases | `b1_battle_droid`, `b1_security_droid`, `separatist_technician`, and the B1 commander duty texture | Convert the authored skeletal model to GeckoLib, scale its 64x32 UV coordinates to the authored 128x64 atlases, add joint and held-item anchors, omit three integrated blaster cuboids so the runtime E-5 does not z-fight, and fill only transparent geometry-required face pixels. |
| `Parzivail-Modding-Team/GalaxiesParzisStarWarsMod` at `b91b4cc1a827eeb7c2ae16f0b703affd78c1c206`, `PSWG_Mandalorian.bbmodel`, `PSWG_Mandalorian.png` | `mandalorian_warrior`, `mandalorian_marksman`, `mandalorian_heavy`, `mandalorian_clansperson` | Select distinct helmet, rangefinder, macrobinocular, Z-6, and JT-12 groups; mirror the Blockbench X axis; preserve 128x128 per-face UVs; fill transparent wearable-only body-glove regions and apply deterministic clan-role palettes. |
| Same Galaxies revision, Senate Commando and Honor Guard models/atlases | `senate_commando`, `republic_honor_guard` | Preserve authored 128x128 UVs, establish canonical GeckoLib bones and hand anchors, and set head pivots to neck height. |
| Same Galaxies revision, shared humanoid plus Togruta, Duros, and Rodian models/layers | `togruta_civilian`, `smuggler`, `hutt_civilian` | Build 256x256 composites with body faces limited to `0..95`, species faces beginning at `106`, non-overlapping clothing/head regions, and opaque required faces. |
| Same Galaxies revision, Trandoshan model and selected clothing/face layers | `hutt_enforcer` | Repair canonical limbs, restore both leg meshes, reparent overlays and held-item anchors, and composite a declared 128x128 geometry/texture pair. |
| Galaxies clone geometry/base atlas combined with Pyrix 501st/commander palettes | `clone_trooper_commander`, `arc_trooper_commander` duty textures | Apply only permissioned palette markings to compatible clone face regions. This is a recorded mixed Galaxies/Pyrix derivative and does not add registry IDs. |

The Galaxies repository declares non-code assets CC-BY-SA 4.0. The Forge repository is GPL-3.0,
and the project owner additionally confirmed direct-copy and adaptation permission on 2026-07-20.
`NOTICE.md`, `third_party/licenses`, and `docs/authorized-source-intake.md` retain the attribution,
license links/copies, pinned revisions, and transformation ledger.

The Galaxies Jedi Commander overlay was evaluated but is not a shipped replacement. Its partial
clothing texture is tied to the upstream legacy humanoid UV layout; applying it to the current Jedi
would mis-map pixels and repeat the quality problem this pass is correcting. Unmatched Jedi, Republic
civilian, bounty hunter, B2, and BX assets remain deterministic project-authored sets until a
UV-compatible permissioned source is identified. The Nightsister family was subsequently rebuilt
from the project-owned 2026-07-27 concept direction recorded above.

This section supersedes the older generated-only provenance claims below for the seventeen imported
NPC sets, three duty textures, and two clone wearable armor sets. Generated orthographic sheets remain references only for the
unmatched roster; they are not the source of the permissioned outputs listed here.

## 2026-07-19 field-command and vehicle-deployment completion

Ten placeholder visual paths were replaced with original project-owned artwork generated in the
built-in image-generation mode. No input or reference images were used. The control beacon previously
rendered with the Duracrete texture; the three field-command tools and five deployment kits previously
rendered with the Energy Cell texture; and the Hyperspace Navigator previously inherited only the
vanilla compass frame. Each runtime model now resolves to a dedicated `galacticwars` texture. The
Navigator retains the `minecraft:item/compass_16` parent for model compatibility but overrides
`layer0` with its own artwork.

The exact control-beacon prompt was:

```text
Use case: stylized-concept
Asset type: tileable Minecraft block texture source
Primary request: an original tactical control-beacon casing panel for a science-fantasy battlefield base
Subject: edge-to-edge dark gunmetal and charcoal composite panel, recessed cross bracing, a centered cyan signal lens, two tiny amber status lights, subtle edge wear
Style/medium: crisp Minecraft-readable pixel art with hard square pixel clusters and a restrained 16x16-ready palette
Composition/framing: perfectly flat orthographic square material, edge-to-edge, no object silhouette, no perspective
Lighting/mood: neutral even material lighting
Color palette: charcoal, gunmetal, muted cyan, sparse amber
Constraints: seamless tileable opposite edges; fully opaque; no text; no logos; no faction insignia; no watermark; original design only; no recognizable franchise prop or official artwork
Avoid: gradients, blur, antialiasing, realistic photography, a scene, borders, transparent pixels
```

The nine item prompts used the following exact shared suffix:

```text
Style/medium: crisp Minecraft-readable pixel-art inventory icon with hard square pixel clusters and a restrained 16x16-ready palette
Composition/framing: one centered object in a three-quarter view, clear silhouette, generous empty padding
Scene/backdrop: perfectly flat solid #ff00ff chroma-key background for local removal; one uniform color with no shadows, gradients, texture, reflections, floor plane, or lighting variation
Constraints: no cast shadow; no contact shadow; no text; no logos; no faction insignia; no watermark; original design only; do not use #ff00ff anywhere in the object
Avoid: antialiasing, blur, realistic photography, a scene, extra objects, recognizable franchise prop replicas
```

Each item prompt began with `Use case: stylized-concept`, followed by the exact asset type and
asset-specific lines below, then the shared suffix above.

| Final asset | Built-in result | Asset type | Primary request | Subject | Color palette |
| --- | --- | --- | --- | --- | --- |
| `textures/item/blueprint_projector.png` | `exec-d9002340-887e-424f-888b-987ef4daef98.png` | Minecraft game item icon | an original handheld blueprint projector for planning battlefield base construction | compact gunmetal projector with a cyan holographic grid lens, folding amber alignment vanes, dark grip, and a tiny green ready light | gunmetal, cyan, amber, sparse green |
| `textures/item/claim_transponder.png` | `exec-3240e84b-9bb5-4c56-b205-7f80e8744774.png` | Minecraft game item icon | an original claim transponder used to register settlement territory in a science-fantasy battlefield | rugged palm-sized charcoal transponder with a short antenna, cyan signal ring, amber authorization chip, and reinforced silver corners | charcoal, silver, cyan, amber |
| `textures/item/command_marker.png` | `exec-3136a0f3-b40b-4225-89d4-12041ca6e004.png` | Minecraft game item icon | an original tactical command marker used to designate an in-world squad target | compact deployable beacon baton with a dark ribbed handle, pointed silver base, bright amber command lens, and two cyan indicator bands | dark gunmetal, silver, amber, cyan |
| `textures/item/hyperspace_navigator.png` | `exec-58627dd1-8043-497a-92b1-c2d0afc0bbe2.png` | Minecraft game item icon | an original hyperspace navigation instrument for selecting distant planets | compact circular astrogation device with a dark octagonal casing, luminous cyan star-map ring, small amber destination pointer, and silver grip tabs | charcoal, silver, cyan, amber |
| `textures/item/barc_speeder_deployment_kit.png` | `exec-58efe502-d89e-4662-8899-295f4462e368.png` | Minecraft vehicle deployment-kit item icon | an original compact field crate that deploys a fast one-rider hover speeder | slim white-and-blue armored equipment case with a cyan horizontal speed glyph made only from abstract light bars, a clipped carry handle, and an amber release latch | off-white armor, navy blue, cyan, gunmetal, amber |
| `textures/item/at_rt_deployment_kit.png` | `exec-16d45705-68b0-4690-80f3-1b1a37a3a14c.png` | Minecraft vehicle deployment-kit item icon | an original compact field crate that deploys a light two-legged scout walker | upright white-and-blue reinforced case with an abstract cyan two-leg schematic made from simple bars, twin side clamps, a command-blue top stripe, and an amber release latch | off-white armor, command blue, cyan, gunmetal, amber |
| `textures/item/stap_deployment_kit.png` | `exec-41b94916-dce9-49c3-b13e-d7801a74d5ae.png` | Minecraft vehicle deployment-kit item icon | an original compact field pod that deploys a narrow one-rider droid hover platform | slim bronze-and-charcoal vertical deployment canister with a cyan lift indicator made from simple bars, folded side stabilizers, and a red-orange release latch | weathered bronze, charcoal, cyan, red-orange |
| `textures/item/aat_deployment_kit.png` | `exec-445086cc-1820-48a0-a91c-68703c0a0266.png` | Minecraft vehicle deployment-kit item icon | an original heavy field crate that deploys a low armored hover tank | broad bronze-and-charcoal reinforced case with an abstract cyan domed-tank schematic made from simple blocks, heavy corner locks, and a red-orange release latch | weathered bronze, charcoal, cyan, red-orange |
| `textures/item/laat_gunship_deployment_kit.png` | `exec-755c9d00-cb01-461d-ae31-4ad082c9a8ef.png` | Minecraft vehicle deployment-kit item icon | an original heavy field crate that deploys a winged troop transport gunship | wide off-white-and-crimson reinforced case with an abstract cyan winged-transport schematic made from simple bars, twin carry grips, heavy silver clamps, and an amber release latch | off-white armor, muted crimson, cyan, silver, amber |

The control-beacon result is `exec-bc6c8a12-06f9-49f2-aa46-4586ba83d58b.png`. It was reduced to
16x16 with nearest-neighbor sampling and opposite edge texels were equalized for seamless tiling. The
nine item sources were chroma-cleaned, cropped to visible bounds, and nearest-neighbor fitted into a
transparent 16x16 canvas with one pixel of minimum padding. Pillow was unavailable in the active Python
runtime, so the installed chroma helper could not run; an in-memory `System.Drawing` pass applied the
same project convention with a deterministic magenta-hue threshold. A stricter second pass removed the
remaining fringe pixels. Final validation confirmed exact dimensions, opaque/transparent contracts,
non-empty subjects, zero magenta-family visible pixels, distinct hashes, and resolving model references.

## 2026-07-13 equipped-armor and recruitment-capsule rebuild

The equipped armor and recruit spawn items were rebuilt with
`tools/generate_character_models.py`. The five GeckoLib armor models now contain 60, 51, 54, 55,
and 54 cuboids for Republic plastoid, Separatist alloy, Mandalorian alloy, Nightsister weave, and
Beskar respectively. The Republic helmet alone uses eighteen layered parts: shell, crown and fin,
brow, horizontal visor and vertical nose slit, angled cheek plates, mouth grille and vents, chin,
ear modules, rear filter, and command stripe. All families retain the standard GeckoLib armor bones,
player-limb parenting, and explicit six-face 1024x1024 UVs. The twenty existing 16x16 armor item
icons are intentionally unchanged so inventory and item-frame presentation remains intact.

The silhouette direction came from the built-in image-generation result
`exec-2326d290-a1d1-49be-97a9-7150c9d5c5f2.png`, retained as
`tools/source_art/generated_armor_model_reference_v4.png`. It was generated in `stylized-concept`
mode from a prompt for five original full-body voxel science-fiction armor sets shown front and rear:
a white-and-blue legion infantry set with an enclosed segmented helmet, horizontal visor, vertical
nose slit, cheek plates, mouth grille, brow, fin, ear modules, and rear filter; plus bronze mechanical,
teal bounty-warrior, black-and-crimson mystic, and silver heavy sets. The prompt excluded logos,
insignia, and film-prop copies. It is a shape and palette reference only; no generated reference pixels
ship in the UV atlases.

All twenty spawn eggs now render as one shared eighteen-cuboid GeckoLib recruitment capsule with
individual 512x512 material and emissive glowmask atlases. Vanilla `SpawnEggItem` behavior is retained;
the Java item adds a unit-specific GeckoLib renderer and idle core pulse. The compact 16x16 sprites remain
as fallback art, while in-hand, GUI, ground, and fixed contexts use the 3D model. The capsule direction
came from built-in result `exec-4ac1cef6-01fb-486b-a90f-7ab634958f84.png`, retained as
`tools/source_art/generated_spawn_capsule_reference_v1.png`. It was generated in `stylized-concept`
mode from a prompt for exactly twenty original voxel recruitment capsules with stepped shells, a dark
equator band, emissive status windows, and distinct unit palette families, excluding text, logos,
creatures, and recognizable replicas. Geometry, UVs, atlas pixels, glowmasks, and fallback icons are
deterministic project-authored output.

## 2026-07-13 GeckoLib lightsaber and model-quality pass

The six lightsabers now share a project-authored GeckoLib item geometry generated by
`tools/generate_lightsaber_model.py`. The model replaces the former vanilla `elements` geometry with
separate `hilt` and `blade` bones, twenty-four UV-mapped cuboids, a 36-unit energy blade, and a
blade-to-hilt ratio of roughly 3.4:1. Each color owns a four-frame 256x1024 material atlas and matching
full-bright glowmask; item definitions use Minecraft's special-model entrypoint with
`geckolib:geckolib`.

The new material and proportion direction came from the built-in image-generation result
`exec-21d571bd-1049-4ba6-89cd-19fdbf518965.png`, retained as
`tools/source_art/generated_lightsaber_model_reference_v4.png`. The prompt requested six original
voxel science-fiction energy swords with straight blades approximately four times their hilt length,
segmented gunmetal hilts, white-hot cores, no characters, no text, no logos, and no recognizable
franchise-specific prop replicas. The generated sheet is concept-only; no source pixels ship in the
runtime atlas. Geometry, UVs, texture pixels, glowmasks, and icons are deterministic script output.

## 2026-07-12 planetary refinement and lightsaber expansion

Four placeholder-grade planetary materials were replaced with separately generated, original source
textures retained under `tools/source_art`. Each source was prompted as an orthographic seamless pixel-art
material without logos, symbols, characters, text, or recognizable franchise motifs. The reproducible
`tools/generate_polished_textures.py` pass fits each source to 16x16, reduces it to a controlled 24-color
palette, and equalizes opposite edge texels after reduction so the final block tiles are seamless.

| Final asset | Retained source | Built-in generation result | Material direction |
| --- | --- | --- | --- |
| `textures/block/coruscant_panel.png` | `generated_coruscant_panel_source.png` | `exec-7f40e704-5b4d-4895-9b7f-05b33363505e.png` | Dark modular infrastructure plating with cyan conduits and sparse amber indicators. |
| `textures/block/kamino_panel.png` | `generated_kamino_panel_source.png` | `exec-63a419ba-8326-4190-be53-d2a8e5a75ba1.png` | Pearl ceramic-alloy research panel with cool seams and icy-blue lighting. |
| `textures/block/geonosis_rock.png` | `generated_geonosis_rock_source.png` | `exec-3a2b65a9-fad3-4df5-9ae3-a92784d8d52a.png` | Iron-rich rust sediment with ochre strata and restrained basalt fragments. |
| `textures/block/tatooine_sand.png` | `generated_tatooine_sand_source.png` | `exec-213d0bac-de93-47cd-be73-efe2afd201cb.png` | Pale wind-combed desert sand with warm mineral flecks. |

Purple, yellow, and white lightsabers were generated as separate straight-beam energy-saber concepts on
a flat green key. The accepted sources are retained beside chroma-cleaned cutouts; the first purple draft
was rejected because its broad fantasy-sword silhouette did not meet the lightsaber brief. The installed
16x16 inventory icons are fitted from the accepted cutouts. Six matching 3D-model material sets use
project-authored hilt patterns and four-frame animated blade strips generated deterministically from the
accepted palettes.

| Final icon | Retained accepted source | Built-in generation result | Hilt direction |
| --- | --- | --- | --- |
| `textures/item/purple_lightsaber.png` | `generated_purple_lightsaber_source.png` | `exec-fe3667ee-8cae-465e-96f2-9bfb75bfe26e.png` | Blackened silver with a violet emitter ring. |
| `textures/item/yellow_lightsaber.png` | `generated_yellow_lightsaber_source.png` | `exec-16c22aaf-699d-4819-b86c-f92258a025dd.png` | Weathered gunmetal, dark grip, and amber activation stud. |
| `textures/item/white_lightsaber.png` | `generated_white_lightsaber_source.png` | `exec-a4a0ae86-20ba-4ed3-aaa9-665456ddce18.png` | Brushed silver, black grip bands, and pearl-blue activation light. |

The installed image-generation helper removed each flat green background using border key sampling, a
soft matte, thresholds 12/220, and despill. Final cutouts have transparent corners, non-empty visible
coverage, and no green key background. No official art, film stills, logos, or third-party mod assets were
used.

## Polished clone, droid, and lightsaber pass

The detailed Clone Trooper, B1 Battle Droid, and lightsaber direction was generated with the built-in image generation workflow as project-owned concept material. Because general image generation does not preserve Minecraft cube UV coordinates, `tools/generate_polished_textures.py` translates that direction into deterministic UV-safe 64x64 GeckoLib skins and native 16x16 item sprites. The existing Gondorian-derived clone/droid geometry, bone names, pivots, animations, and UV origins remain unchanged; only the texture pixels are replaced.

All assets listed here are original, project-bound artwork. No official Star Wars artwork, film stills, logos, third-party mod textures, or other third-party assets were used.

## Equipped armor and complete NPC model pass

The twenty recruit/civilian models and five equipped armor families are generated together by
`tools/generate_character_models.py`. Recruit cuboids retain non-overlapping 128x128 box UVs.
Equipped armor instead uses explicit six-face mappings on 1024x1024 atlases at six texture texels per
model unit, allowing recessed seams, bevel lighting, controlled wear, vents, fasteners, woven fibers,
and family-specific plate markings without changing the player-space geometry scale. The script also
produces the five civilian spawn-egg asset sets that were previously missing. The resulting geometry adds
separate helmet shells, visors, chest and back plates, pauldrons, gauntlets, belts, pouches, knee and
shin guards, boots, droid joints, robes, hoods, packs, sensors, and profession details while retaining
the animation and held-item bone contract.

The armor silhouette pass used the project-owned inventory sheet
`tools/source_art/generated_armor_icons.png` as its palette input and the built-in image-generation
result `exec-ec4c1511-f614-4799-8051-f0f34a0dcd67.png` as a generic voxel-material reference. A copy
is retained at `tools/source_art/generated_armor_model_reference_v2.png`. The prompt explicitly
required new generic science-fantasy designs, neutral mannequins, and no characters, logos, emblems,
weapons, or existing-franchise imagery. Final distributable models and UV atlases are deterministic
script output rather than crops or copied pixels from the reference sheet.

After the initial equipped textures failed visual review for flat, low-density surfaces, a second
project-owned material study was generated from the five existing inventory icons. It is retained as
`tools/source_art/generated_armor_material_reference_v3.png`. That study established the final material
language: off-white layered plastoid, blackened bronze alloy, weathered teal plate, black woven cloth
with crimson lacquer, and brushed silver beskar. No pixels from the study are shipped directly; the
generator translates those material decisions into exact per-face GeckoLib UV regions.

## Method

- Generated source images with the built-in OpenAI image-generation tool on 2026-07-11.
- Used one focused prompt per deliverable. Opaque blocks were generated edge-to-edge; inventory and UI art used a flat `#ff00ff` chroma background.
- Fitted sources to the manifest dimensions with nearest-neighbor resampling. Chroma pixels were removed locally using a conservative magenta color threshold because the bundled Pillow helper was unavailable in the active runtime.
- Validated exact dimensions, opaque/transparent pixel contracts, non-empty visible coverage, distinct SHA-256 hashes, and visual readability at native resolution.
- Beskar ore, raw Beskar, and the Beskar ingot were transformations of the project's own Mithril-derived artwork, then substantially recolored and redrawn by the generator.

## Shared prompt constraints

Every prompt required crisp Minecraft-readable pixel art with hard pixel clusters, no antialiasing, text, logos, faction insignia, watermarks, or official Star Wars imagery. Transparent assets additionally required a uniform `#ff00ff` background, no shadows, and no magenta in the subject.

## Core batch

| Final asset | Generated source | Asset-specific prompt |
| --- | --- | --- |
| `textures/block/command_center_side.png` | `exec-cf5ea313-ba97-411e-bd6e-d3ba22beef63.png` | Armored charcoal duracrete side panel with inset gunmetal plating, amber status light, and restrained cyan circuit accents. |
| `textures/block/command_center_top.png` | `exec-e13fc952-e395-4adb-9619-c9e9985c36e3.png` | Top-down segmented command-center panel with a circular landing-console motif and small amber/cyan indicators. |
| `textures/block/command_center_bottom.png` | `exec-cb352f0c-c57b-4e30-8605-fe7d93baa280.png` | Heavy duracrete underside with cross-braced gunmetal seams, recessed bolts, and industrial wear. |
| `textures/block/duracrete.png` | `exec-07a97d60-42aa-408d-a585-79637ca8247e.png` | Cool gray poured composite with aggregate speckles and restrained panel seams for large base surfaces. |
| `textures/block/beskar_ore.png` | `exec-0d27b0e1-7903-464c-9617-3cffdbfa99bd.png` | Rework project-owned Mithril ore into dark duracrete with sparse cold silver-blue Beskar veins. |
| `textures/item/raw_beskar.png` | `exec-be930583-9f7f-4037-af74-dbf60bcfbab1.png` | Angular unprocessed Beskar chunk with dark gunmetal facets and cold silver-blue highlights. |
| `textures/item/beskar_ingot.png` | `exec-a250625c-7a88-41a2-84a4-a5ec7060fe0d.png` | Compact beveled Beskar ingot with brushed gunmetal, cold edge highlights, and forge grooves. |
| `textures/item/credit_chip.png` | `exec-21a2c6bf-e264-40a5-9f22-94c27e9e2843.png` | Generic hexagonal sci-fi credit chip with an amber inset and cyan edge lights. |
| `textures/item/energy_cell.png` | `exec-e0cd83b5-c09a-47d3-aac8-069fa4e03963.png` | Compact gunmetal blaster cartridge with a cyan core slit and amber charge cap. |
| `textures/gui/galactic_wars_tab.png` | `exec-a6d8fe53-fc73-4c10-9b48-de5c50d5afd7.png` | Abstract four-point hyperspace compass around an amber star, designed for tiny UI scale. |

Generated sources remain in the Codex image-generation store for auditability. The distributable sources of truth are the final PNGs under `src/main/resources/assets/galacticwars/`.

## Remaining manifest batches

- `factions`: identity chips, five armor layer sets, and armor inventory icons.
- `combat_and_tools`: complete; all seventeen icons and their item/model references are installed and validated.

## Unit batch

The fifteen 64x64 entity textures use generated character concepts only as palette, silhouette, and material references. Final distributable atlases are deterministically authored into the exact cube-face regions declared by the locked GeckoLib models under `assets/galacticwars/geckolib/models/entity/`; model-specific and shared humanoid base regions are opaque, while remaining atlas space is transparent.

| Final atlas | Generated concept source |
| --- | --- |
| `textures/entity/clone_trooper.png` | `exec-4e523ad4-febb-4172-ae0b-61c6c215abfe.png` |
| `textures/entity/arc_trooper.png` | `exec-20fe8cda-3d58-486d-9fe9-92d2ca35e1f7.png` |
| `textures/entity/jedi_knight.png` | `exec-9d6656e8-fb76-41c1-9c34-1d5f958bc62a.png` |
| `textures/entity/b1_battle_droid.png` | `exec-3b346fd3-4bd8-4210-b985-fb35d758007c.png` |
| `textures/entity/b2_super_battle_droid.png` | `exec-6ef7139a-42fe-4b11-8090-b16a08a702f7.png` |
| `textures/entity/commando_droid.png` | `exec-3efdb814-4e46-4a4a-a828-909989e517a0.png` |
| `textures/entity/mandalorian_warrior.png` | `exec-0a04ce0a-657f-48bb-ba65-d4daf23dd241.png` |
| `textures/entity/mandalorian_marksman.png` | `exec-ef79dde2-8ac3-4cb1-86cb-b24c3b5c5acb.png` |
| `textures/entity/mandalorian_heavy.png` | `exec-a90d7134-bcb3-4947-b8e1-0976020e843f.png` |
| `textures/entity/hutt_enforcer.png` | `exec-deb6a950-d46f-4029-bfdc-a1755df3d7aa.png` |
| `textures/entity/bounty_hunter.png` | `exec-76c207a7-7298-4f3c-bbe1-59ae3fc00387.png` |
| `textures/entity/smuggler.png` | `exec-2a9a37dc-6c9d-40b3-806f-c8deb5c5ef65.png` |
| `textures/entity/nightsister_acolyte.png` | `exec-51af9e24-e176-41c3-9d13-3703eb0cfa96.png` |
| `textures/entity/nightsister_archer.png` | `exec-3f844138-1ccf-470c-bc7c-c384447deef3.png` |
| `textures/entity/nightbrother_brute.png` | `exec-acb59286-5603-4519-909d-e4e809863517.png` |

### UV validation correction

The initial geometry-only transparency mask cleared `hutt_enforcer.png` pixel `16,31`, which belongs to the canonical body-west face. The atlas was regenerated with the shared head, body, arm, and leg UV boxes always preserved in addition to geometry-specific boxes. The corrected pixel now uses the surrounding authored body material and is fully opaque. `RecruitTextureAtlasTest` subsequently validated required face opacity, bounds, dimensions, animation references, alpha-safe atlases, and distinct unit texture hashes for all fifteen units.

## Combat and tools partial batch

Each completed source was fitted to 16x16 with nearest-neighbor resampling, cleaned to a transparent inventory background, and linked through matching item-definition and handheld-model JSON files.

| Final icon | Generated source |
| --- | --- |
| `textures/item/vibroblade.png` | `exec-26e2e6d2-9af2-4b76-87d7-fb7502bc9621.png` |
| `textures/item/beskar_vibroblade.png` | `exec-e73ef179-48a3-4e47-a970-ec451b8afbd8.png` |
| `textures/item/plasma_cutter.png` | `exec-723720af-b9eb-4752-8b0d-be7998b1a283.png` |
| `textures/item/power_drill.png` | `exec-3f6324bf-78a0-4404-a8f9-6719e42de260.png` |
| `textures/item/sonic_excavator.png` | `exec-7999e613-9f6c-4ac4-b616-81d66ab099ee.png` |
| `textures/item/hydrospanner.png` | `exec-84f3ede6-0f56-463a-974b-b553fa61c8e7.png` |
| `textures/item/dc15_blaster.png` | `exec-2d527b16-359c-4535-8e46-224711344039.png` |
| `textures/item/e5_blaster.png` | `exec-24cfabe0-11a5-457e-9475-cc4b4bafcec0.png` |

The project-owned Mandalorian ingot texture and both resource references were renamed from `mandalorian_alloy_iron_ingot` to the registered `mandalorian_alloy_ingot` identifier without altering the artwork.

### Consolidated source-sheet mapping

The remaining nine icons came from the generated 3x3 source sheet `exec-aeab48fc-acaa-441f-ac1f-4329e9681096.png`. Each equal cell was cropped independently, nearest-neighbor fitted to 16x16, and chroma-cleaned.

| Cell | Final icon |
| --- | --- |
| Row 1, column 1 | `textures/item/westar_blaster.png` |
| Row 1, column 2 | `textures/item/scatter_blaster.png` |
| Row 1, column 3 | `textures/item/nightsister_bow.png` |
| Row 2, column 1 | `textures/item/blue_lightsaber.png` |
| Row 2, column 2 | `textures/item/green_lightsaber.png` |
| Row 2, column 3 | `textures/item/red_lightsaber.png` |
| Row 3, column 1 | `textures/item/blaster_bolt.png` |
| Row 3, column 2 | `textures/item/force_light.png` |
| Row 3, column 3 | `textures/item/force_dark.png` |

All seventeen combat/tool textures passed the final contract audit: exact 16x16 dimensions, transparent corners and backgrounds, non-empty visible subjects, zero visible chroma pixels, seventeen distinct SHA-256 hashes, and resolving `assets/galacticwars/items` plus `models/item` references.

## Faction batch

Faction inventory art came from `exec-f7a0bd99-9610-42b1-ae4e-8c0a7d07113b.png`. The 5x5 sheet maps columns left-to-right to `republic`, `separatist`, `mandalorian`, `hutt_cartel`, and `nightsister`; rows top-to-bottom map to `identity_chip`, `helmet`, `chestplate`, `leggings`, and `boots`. Every cell was independently center-cropped, nearest-neighbor fitted to 16x16, and chroma-cleaned to its corresponding `textures/item/{faction}_{row}.png` path.

The adult, baby, and leggings layers were recolored from project-owned UV-safe armor templates using the same generated faction palettes, preserving transparency and exact vanilla armor UV positions. Their paths are `textures/entity/equipment/{humanoid|humanoid_baby|humanoid_leggings}/{faction}.png` and are connected by `equipment/{faction}.json`.

Validation passed for all forty faction PNGs: twenty-five unique 16x16 inventory hashes, five distinct textures within each armor-layer class, exact 64x32 adult/leggings and 64x64 baby dimensions, non-empty transparency and visible coverage, zero residual chroma, resolving item/model references, and five complete equipment descriptors.

## Planets batch

The four planet-selection icons and navigation console were generated on a uniform magenta background, nearest-neighbor fitted to their final GUI dimensions, and chroma-cleaned to transparent PNGs. The four environment materials were generated as opaque edge-to-edge square sources and nearest-neighbor fitted to 16x16.

| Final asset | Generated source |
| --- | --- |
| `textures/gui/planet/tatooine.png` | `exec-7a284c37-bea1-400a-a566-21897a7869ad.png` |
| `textures/gui/planet/geonosis.png` | `exec-c15aa39c-90cf-4a2b-a873-5fa96849fc68.png` |
| `textures/gui/planet/kamino.png` | `exec-1c60ac36-588f-4873-8e4c-280dcbb8fe94.png` |
| `textures/gui/planet/coruscant.png` | `exec-090d0da3-28ef-4775-a447-f7baa974d97b.png` |
| `textures/block/tatooine_sand.png` | `exec-816bfdcc-02d5-4cd9-b055-c6f9f75b31ab.png` |
| `textures/block/geonosis_rock.png` | `exec-22c00903-b5eb-4149-aaf2-fdbf62e194fb.png` |
| `textures/block/kamino_panel.png` | `exec-b198b307-3f5c-4cbb-93d0-951a9add76bd.png` |
| `textures/block/coruscant_panel.png` | `exec-c18810f8-3243-4f27-8c78-eb4bf14882fb.png` |
| `textures/gui/navigation_console.png` | `exec-a25927d5-7c63-42b7-b4ea-5ee435fe099b.png` |

The completed planet gate passed exact-dimension and alpha checks: every planet icon is 128x128 with transparent corners, the navigation console is 256x256 with a transparent exterior and center, every block is fully opaque at 16x16, all transparent assets have non-empty visible coverage and zero residual magenta, and all nine PNGs have distinct SHA-256 hashes. Each environment block has resolving blockstate, block-model, item-definition, and item-model JSON references.

## Vehicle batch

Each vehicle began as a separately generated, edge-to-edge material kit containing original armor, engine, cockpit, joint, and weapon panels. The sources were nearest-neighbor fitted to 256x256 and copied only into the explicit cuboid face footprints declared by the matching GeckoLib geometry. All unused atlas space remains transparent.

On 2026-07-13, `tools/refine_vehicle_models.py` expanded the placeholder-grade silhouettes while
preserving those material atlases and every animation bone. BARC, AT-RT, STAP, AAT, and LAAT now use
20, 28, 20, 23, and 40 cuboids respectively instead of 6, 10, 6, 6, and 11. New hull steps, armor
plates, engine caps, joints, controls, weapon barrels, wings, cockpit layers, and tail details reuse
smaller regions inside their owning bone's existing authored UV footprint; no third-party texture or
model source was introduced.

| Final atlas | Generated material source | Geometry and animation contract |
| --- | --- | --- |
| `textures/entity/vehicle/barc_speeder.png` | `exec-367b9f7b-bcd4-4d68-9bd8-4d6fe21f7a9b.png` | `geckolib/{models,animations}/entity/vehicle/barc_speeder.*.json` |
| `textures/entity/vehicle/at_rt.png` | `exec-79a66d46-f2c3-4b78-8bf1-0a2648066303.png` | `geckolib/{models,animations}/entity/vehicle/at_rt.*.json` |
| `textures/entity/vehicle/stap.png` | `exec-456e75ae-c89e-4219-adbc-0aeb9ab753f7.png` | `geckolib/{models,animations}/entity/vehicle/stap.*.json` |
| `textures/entity/vehicle/aat.png` | `exec-b2870082-c235-4546-90d9-86eaf1b8aa2c.png` | `geckolib/{models,animations}/entity/vehicle/aat.*.json` |
| `textures/entity/vehicle/laat_gunship.png` | `exec-66e0e323-af49-443e-905e-fabcb722cb43.png` | `geckolib/{models,animations}/entity/vehicle/laat_gunship.*.json` |

Validation parsed every geometry and animation file, resolved parent and animated bone references, verified the `geometry.galacticwars.vehicle.*` identifiers and 256x256 declarations, and recalculated every box-UV footprint from its cube dimensions. All footprints are in bounds, non-overlapping, and fully opaque; every atlas also retains transparent unused space and a transparent corner. The five final PNGs have distinct SHA-256 hashes and passed native-resolution visual inspection.

The first LAAT audit identified a six-pixel-high overlap between the right-cannon and side-door footprints. The right-cannon origin was moved from `[112,128]` to `[112,168]`, the atlas was rebuilt from the same generated source, and the complete five-vehicle validation was rerun successfully after the correction.

## Effects and GUI batch

All six sources were generated on a uniform magenta background, visually reviewed together, cropped to their visible subject bounds, nearest-neighbor fitted with transparent padding to the exact manifest dimensions, and chroma-cleaned.

| Final asset | Generated source |
| --- | --- |
| `textures/particle/blaster_bolt.png` | `exec-b2c405e5-d86c-4d93-b888-80bbe178b103.png` |
| `textures/particle/force_wave.png` | `exec-5f00564a-979f-4c9c-9328-db2f39440d2a.png` |
| `textures/gui/quest_panel.png` | `exec-676222e1-84eb-4415-8d78-9ea53164ffc9.png` |
| `textures/gui/conquest_panel.png` | `exec-50d1b8b8-1f44-4178-a59f-45dd11189f74.png` |
| `textures/gui/vehicle_hud.png` | `exec-cf7dc42c-3058-4cd7-88d2-056246f19579.png` |
| `textures/gui/force_meter.png` | `exec-b0ddd5b5-edf8-46b4-a8da-87d793538ace.png` |

The particle sprites resolve through `particles/blaster_bolt.json` and `particles/force_wave.json`; GUI textures are direct named resources for their render layers. Validation passed exact sizes, transparent corners and interiors, non-empty visible coverage, zero residual chroma, six distinct SHA-256 hashes, JSON/reference resolution, and native-resolution readability.

## Armor icon refinement

The twenty armor inventory icons were regenerated as one cohesive project-owned pixel-art sheet (`exec-35cea704-fd2a-43e4-a596-d48046a7341d.png`). The retained source is `tools/source_art/generated_armor_icons.png`; `tools/generate_polished_textures.py` removes its flat green key, isolates the fixed five-by-four cells, fits each silhouette into a transparent 16x16 canvas, and writes the helmet, chestplate, leggings, and boots icons for all five armor families. This keeps the small in-game assets reproducible while preserving distinct Republic plastoid, Separatist alloy, Mandalorian alloy, Nightsister weave, and Beskar material language.

## 2026-07-20 NPC and clone-armor v4 overhaul

`tools/generate_character_models.py` is the sole deterministic source for all twenty-two recruit
models, textures, animation sets, recruitment-capsule visuals, and six GeckoLib equipped-armor
families. NPC atlases are 256x256 with explicit north/south/east/west/up/down UV declarations at two
texels per model unit. Equipped armor remains 1024x1024 with six texels per model unit. The two added
recruits are `phase_i_clone_trooper` and `phase_i_arc_trooper`; the retained `clone_trooper` and
`arc_trooper` resources now represent late-war blue-marked armor. The new `phase_i_clone` wearable
family has its own geometry, texture, animation, and four original 16x16 inventory icons.

Five image-generation outputs are retained only as project-bound silhouette, material, and proportion
references. No generated reference pixels are cropped, traced, sampled, or copied into shipped atlases.
All distributable cuboids and pixels are authored by the deterministic Python generator. The sheets are
original, anonymous designs without official pixels, logos, named-character likenesses, or third-party
assets:

| Family | Retained source | Built-in result |
| --- | --- | --- |
| Clone armor | `tools/source_art/generated_clone_armor_orthographic_v4.png` | `exec-aa4eacd1-972e-4c31-9212-1d626584b4b6.png` |
| Mechanical infantry | `tools/source_art/generated_droid_orthographic_v4.png` | `exec-0362beec-8040-4b2b-b3cf-77c75ae5f279.png` |
| Armored clans and guardian | `tools/source_art/generated_mandalorian_jedi_orthographic_v4.png` | `exec-7d705b9c-da21-4b15-9029-c8e3f16187e0.png` |
| Dathomir-inspired settlement | `tools/source_art/generated_dathomir_orthographic_v4.png` | `exec-1567e44c-d3c6-402e-8a26-a0dcdd7e6fea.png` |
| Hutt-aligned, outlaw, and civilian | `tools/source_art/generated_hutt_outlaws_civilians_orthographic_v4.png` | `exec-ebbe15ec-d4b7-4415-9168-3aa5fec052ff.png` |

The exact successful built-in prompts were:

```text
Use case: stylized-concept
Asset type: project-bound orthographic game character concept sheet for a deterministic voxel-model pipeline
Primary request: create one cohesive original front, side, and back orthographic design sheet for four anonymous retro-futurist space infantry armor roles: clean early-generation line armor, evolved late-generation blue-marked line armor, early-generation field specialist armor, and late-generation blue-marked field specialist armor.
Scene/backdrop: neutral light-gray technical presentation background, separated turnarounds, no scenery
Style/medium: polished original hard-surface concept art translated toward chunky Minecraft-compatible cuboid construction, crisp panel boundaries, readable at small scale
Composition/framing: full body, all figures same scale, clear front, side, and back views, helmets on, neutral A-pose, generous separation
Materials/textures: off-white ceramic composite plates, black body glove, restrained cobalt-blue markings on late-generation armor, subtle chipped edges and field wear; specialists add rangefinder, asymmetrical shoulder guard, split utility skirt, ammo pouches, bracers, compact backpack, and holsters
Constraints: strongly distinguish early and late helmet silhouettes and line-versus-specialist silhouettes; entirely original designs; no existing franchise characters, logos, symbols, text, labels, watermark, weapons, or recognizable faces
```

```text
Use case: stylized-concept
Asset type: project-bound orthographic game character concept sheet for a deterministic voxel-model pipeline
Primary request: create one cohesive original front, side, and back orthographic design sheet for four anonymous retro-futurist mechanical infantry types: a tall skeletal tan line automaton, a broad gunmetal heavy automaton, an angular charcoal infiltration automaton, and a compact ochre field-repair technician automaton.
Scene/backdrop: neutral light-gray technical presentation background, separated turnarounds, no scenery
Style/medium: polished original hard-surface concept art translated toward chunky Minecraft-compatible cuboid construction, crisp panel boundaries and mechanical pivots
Composition/framing: full body, same scale, clear front, side, and back views, neutral pose, generous separation
Materials/textures: worn painted metal, exposed neck pistons, elbow and knee hinges, narrow waist couplers, layered chest plates, small optics; orange service markings only on technician
Constraints: make each body architecture unmistakably different; show usable child-joint shapes for neck, shoulders, elbows, wrists, hips, knees, ankles, torso and backpack; entirely original designs; no existing franchise characters, logos, symbols, text, labels, watermark, weapons, or humanoid faces
```

```text
Use case: stylized-concept
Asset type: project-bound orthographic game character concept sheet for a deterministic voxel-model pipeline
Primary request: create one cohesive original front, side, and back orthographic design sheet for five anonymous space-fantasy roles: a teal-gray helmeted clan warrior, a sand-orange helmeted clan marksman, a blue-gray heavily armored clan defender, a practical unhelmeted clan civilian, and a robed peacekeeper guardian.
Scene/backdrop: neutral light-gray technical presentation background, separated turnarounds, no scenery
Style/medium: polished original hard-surface and costume concept art translated toward chunky Minecraft-compatible cuboid construction
Composition/framing: full body, same scale, clear front, side, and back views, neutral A-pose, generous separation
Materials/textures: angular layered clan armor with narrow T-shaped abstract visor language, capes or compact packs, rangefinder only for marksman, thick pauldrons and power pack for heavy; guardian uses layered earth-tone robes, tabards, belt, boots and simple cloak
Constraints: early-war utilitarian clan aesthetic, not a glossy lone-gunslinger look; readable family-specific child parts; entirely original designs; no existing franchise characters, logos, symbols, text, labels, watermark, weapons, jet flames, or recognizable actor faces
```

```text
Use case: stylized-concept
Asset type: project-bound orthographic game character concept sheet for a deterministic voxel-model pipeline
Primary request: create one cohesive original front, side, and back orthographic design sheet for four anonymous dark-fantasy alien settlement roles: a hooded pale acolyte, a lean pale archer, a practical pale village civilian, and a tall muscular red-and-black horned brute.
Scene/backdrop: neutral light-gray technical presentation background, separated turnarounds, no scenery
Style/medium: polished original costume and creature concept art translated toward chunky Minecraft-compatible cuboid construction
Composition/framing: full body, same scale, clear front, side, and back views, neutral pose, generous separation
Materials/textures: layered black, burgundy, charcoal, and bone-gray cloth; woven wraps, belts, pouches, shoulder mantles; restrained geometric face paint; brute has broad shoulders, short crown horns, heavy wraps and rugged boots
Constraints: readable hood, skirt-panel, mantle, hair or horn child geometry; each silhouette distinct; entirely original designs; no existing franchise characters, logos, symbols, text, labels, watermark, weapons, gore, or recognizable actor faces
```

```text
Use case: stylized-concept
Asset type: project-bound orthographic game character concept sheet for a deterministic voxel-model pipeline
Primary request: create one cohesive original front, side, and back orthographic design sheet for five anonymous retro-futurist underworld and civilian roles: a huge green alien syndicate enforcer, a practical warm-toned syndicate civilian, a compact armored tracker, a rugged jacketed cargo pilot, and a clean blue-gray metropolitan civilian.
Scene/backdrop: neutral light-gray technical presentation background, separated turnarounds, no scenery
Style/medium: polished original creature, costume, and hard-surface concept art translated toward chunky Minecraft-compatible cuboid construction
Composition/framing: full body, same baseline but believable different heights and widths, clear front, side, and back views, neutral pose, generous separation
Materials/textures: enforcer has thick neck, broad torso, utility harness and heavy boots; syndicate civilian has layered desert fabrics and sash; tracker has mismatched compact armor, visor and gear pack; pilot has vest, jacket, belt and holster shapes; metropolitan civilian has tailored jacket, utility tablet pouch and clean boots
Constraints: strongly distinct body proportions and silhouettes; readable packs, belts, hair, headwear and layered clothing as child geometry; entirely original designs; no existing franchise characters, logos, symbols, text, labels, watermark, weapons, cigars, or recognizable actor faces
```

An earlier clone-family prompt that explicitly named franchise-era roles was rejected by the image
safety system during output moderation (request ID
`9cc53275-4589-4841-a2a9-79c5a6279e75`) and produced no retained file. The successful neutral prompt
above replaced it. Visual validation confirmed distinct early/late and line/specialist clone silhouettes,
skeletal/heavy/agile droid proportions, family-specific child rigs, twenty or more non-zero cuboids per
NPC, and unique texture and animation hashes across all twenty-two sets.

## 2026-07-21 complete 3D visual-overhaul concepts

These project-bound boards are silhouette/material references only; no generated pixels ship as
runtime textures. Final geometry, UVs, atlases, glowmasks, definitions, and transforms are deterministic
outputs of the repository Python tools. Existing upstream-derived work retains the pinned source,
GPL-compatible licensing, authorization, and transformation records above.

| Board | Built-in output | SHA-256 | Final use |
| --- | --- | --- | --- |
| `tools/source_art/generated_concepts/2026-07-21/clone_wars_character_turnarounds_v1.png` | `exec-981484d3-ddc9-40e6-8ee2-f788d80c331e.png` | `c75c8566512c7c73aa069da5dfedd6a9904b67e40d087ad371180f94160125aa` | Robes, droid landmarks, bounty gear, Dathomirian cloth/horns, and Togruta head-tails. |
| `tools/source_art/generated_concepts/2026-07-21/clone_wars_vehicle_turnarounds_v1.png` | `exec-f89fb0fd-b489-45e5-b4b0-17dfa159c2a3.png` | `80f84fc5b74b24ab5c58842f0f74c38d5eaff5c0693b803974c8ecfc255c0c5c` | Forked speeder, open walker, narrow platform, crescent tank, and angular gunship massing. |
| `tools/source_art/generated_concepts/2026-07-21/clone_wars_equipment_turnarounds_v1.png` | `exec-3abfa4e7-b4bf-4184-91c4-4aa3a8b53d6e.png` | `a808c9d6f0185acc31628da7cfb9649b150c1748cb9f4ac002c323c84f646590` | Volumetric blasters, distinct saber hilts, Mandalorian, cloth, and mechanical armor. |

Successful prompts:

```text
Create a stylized orthographic turnaround concept sheet for an original block-built galactic voxel game. Show nine distinct unnamed archetypes in front, side, and back views: a layered-robed energy-sword field guardian; a bulky blue-gray combat automaton with an integrated right forearm cannon; a slender agile tan command automaton with a narrow angular head; an armored frontier tracker; a practical city civilian; a crimson-and-charcoal mystic acolyte in layered cloth wraps and hood; a mystic archer with quiver and asymmetric skirt; a muscular horned warrior with geometric facial and body markings; and an orange-skinned civilian with immediately readable striped head crests and three long head tails. Chunky Minecraft-compatible voxel construction, animated-series silhouette language, pixel-material readability at gameplay distance, neutral A-pose, consistent scale, light gray studio grid. Emphasize landmark geometry and clean material zones. Original designs only. No text, labels, logos, watermark, photorealism, blood, or combat scene.
```

```text
Create an original stylized orthographic vehicle concept sheet for a block-built galactic voxel game. Five distinct vehicles, each shown in front, side, top, and rear views on a light gray studio grid: a very fast forked-nose scout speeder bike with exposed saddle and rear vanes; an open-cockpit two-legged reconnaissance walker with articulated knees and chin blaster; a narrow single-rider vertical hover platform with small steering forks; a low crescent-hulled repulsor battle tank with central turret and side cannons; and a broad angular troop gunship with open side doors, short wings, missile pods, bubble turrets, and rear ramp. Chunky Minecraft-compatible geometry, clear animated-series silhouettes, readable scale, practical panel separation, original designs. No text, labels, logos, watermark, photorealism, characters, or battle scene.
```

```text
Create an original stylized orthographic equipment concept sheet for a block-built galactic voxel game. On a light gray studio grid show four distinct volumetric energy rifles in front/side/top views: a long heavy service rifle with squared stock and blue power cell; a compact tan mechanical carbine with skeletal barrel; a twin-grip chrome frontier pistol with angular muzzle; and a short broad scatter carbine with vented receiver. Also show six long energy-sword hilts, each with a different era-authentic silhouette: ridged silver-blue, black-and-brass green, ornate silver-purple, dark clawed red, temple-guard gold, and clean pearl-white; blades are not shown. Include three wearable armor silhouettes: layered plate-and-cloth jetpack armor with strong T-shaped visor, layered crimson mystic wraps with hood and split skirt, and a coherent angular graphite mechanical exosuit. Chunky Minecraft-compatible voxel geometry, readable pixel-material zones, original designs, no text, labels, logos, watermark, photorealism, hands, or characters.
```

The first franchise-worded character attempt was rejected during output moderation (request ID
`11465710-c5b7-482a-8a83-87a61e96b358`); no output was retained. References were the repository's
authorized intake, current QA captures, and the project-authorized era direction; no unlicensed
third-party mod asset or external reference image was introduced.

Runtime resolutions are 256x256 for rebuilt/import-refined NPCs, 1024x1024 for original wearable
armor, 256x256 per blaster atlas/glowmask, 256x1024 per animated saber atlas/glowmask, and 256x256 for
vehicles. Clone/guard/B1 assets retain stronger authored 128x128 or 128x64 resolution. Commander
markings start from an exact soldier RGBA copy and generation rejects any alpha-mask change.

## Sith Acolyte Force-Career Asset (2026-07-21)

`tools/source_art/generated_sith_acolyte_concept.png` is project-bound concept art generated by the
built-in image workflow (`exec-fadff1bd-d7e3-4f9c-86a4-492f3dec85e1.png`, SHA-256
`6f36dab7bb8750ec78c71bbeb02313827d4d856702d9a7a7ec06ef8e5d88178b`). The prompt requested an
original block-built charcoal and burgundy acolyte with restrained crimson energy accents, no logo,
official character likeness, or external reference image. `tools/generate_character_models.py`
translates only that palette and silhouette direction into the repo's deterministic 256x256 explicit
six-face UV contract. The resulting `sith_acolyte` geometry, animation, entity atlas, recruitment
capsule, glowmask, and icon are project-owned outputs; the entity atlas SHA-256 is
`7498409e7ff5c331ac47849c7ec3d164ac0d4903995806b40fecf0c434144972`.

## Blaster Family Rebuild (2026-07-27)

`tools/source_art/generated_blaster_families_orthographic_concept_2026-07-27.png` is
project-bound concept art generated with the built-in image workflow
(`call_RjJMFX5mPsF5eEaYMuD48zVY.png`, SHA-256
`d46ac23e51e34e44842522898ba5207092bef5fe07bf6c0f0737864e91c455ba`).
It is visual direction only. The invented names and marks in the concept are deliberately not copied.
`tools/generate_blaster_models.py` independently expresses the registered DC-15, E-5, WESTAR, and
scatter-blaster identities as original deterministic GeckoLib geometry, explicit 256x256 UV atlases,
glowmasks, and display transforms. No third-party mod asset was used.

Successful built-in prompt:

```text
Use case: stylized-concept
Asset type: original orthographic art-direction sheet for fictional Minecraft-compatible collectible miniatures
Primary request: Create a polished concept sheet for four clearly fictional, nonfunctional retro-space-opera game props: a long clone-trooper-style energy prop, a skeletal battle-droid-style energy prop, a compact armored sidearm prop, and a heavy twin-front scatter prop. These are decorative voxel game miniatures, not real-world weapons.
Scene/backdrop: neutral warm light-gray studio background, no scenery
Subject: four props only, each shown separately in side, top, and front orthographic views; emphasize distinctive model-ready outer silhouettes, decorative plating, and color blocking rather than function
Style/medium: premium high-detail voxel concept render, block-built forms, crisp pixel materials, collectible game-prop presentation
Composition/framing: landscape reference sheet with all props fully visible and generous spacing
Lighting/mood: soft neutral studio lighting for shape and surface readability
Color palette: charcoal gunmetal and blue-gray for the long clone-style prop; graphite and dark bronze for the skeletal droid-style prop; silver-charcoal and restrained amber for the compact prop; weathered steel, black, and muted rust-red for the heavy prop
Materials/textures: layered fictional alloy panels, decorative ribbing, wrapped grips, optics-like ornaments, subtle edge wear, and tiny colored status lights
Constraints: original project-owned art direction; decorative and visibly fictional; no real-world weapon instructions, no firing scene, no internal cutaway, no functional mechanical details, no characters, hands, copied franchise logos, third-party mod assets, watermark, or scenery
Avoid: photorealism, modern Earth firearm silhouettes, instructional diagrams, measurements, ammunition, realistic mechanisms, flat sprites, excessive neon, or clutter
```
