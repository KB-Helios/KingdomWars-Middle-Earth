"""Generate four distinct, high-detail GeckoLib blasters and their UV-safe atlases."""

from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path

from PIL import Image

from generate_character_models import ModelBuilder, Palette, save_png


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/galacticwars"
MODEL_ROOT = ASSETS / "geckolib/models/item/blaster"
ANIMATION_ROOT = ASSETS / "geckolib/animations/item/blaster"
TEXTURE_ROOT = ASSETS / "textures/item/blaster"
ITEM_MODEL_ROOT = ASSETS / "models/item"
ITEM_DEFINITION_ROOT = ASSETS / "items"


@dataclass(frozen=True)
class BlasterDesign:
    id: str
    palette: Palette


DESIGNS = (
    BlasterDesign("dc15_blaster", Palette(
        (0, 0, 0), (47, 57, 68), (17, 23, 29), (64, 75, 86),
        (148, 162, 169), (49, 158, 236), (78, 69, 57))),
    BlasterDesign("e5_blaster", Palette(
        (0, 0, 0), (62, 53, 42), (28, 27, 24), (116, 97, 66),
        (183, 153, 99), (57, 215, 203), (67, 52, 35))),
    BlasterDesign("westar_blaster", Palette(
        (0, 0, 0), (51, 52, 60), (19, 20, 25), (118, 123, 132),
        (211, 217, 220), (176, 75, 244), (58, 47, 42))),
    BlasterDesign("scatter_blaster", Palette(
        (0, 0, 0), (56, 58, 47), (20, 23, 22), (76, 82, 65),
        (145, 150, 122), (240, 126, 37), (69, 52, 38))),
)


def bones(builder: ModelBuilder) -> None:
    builder.bone("root", [0, 0, 0])
    builder.bone("receiver", [0, 0, 0], "root")
    builder.bone("stock", [0, 0, 5], "root")
    builder.bone("grip", [0, -1, 1], "root")
    builder.bone("barrel", [0, 0, -6], "root")
    builder.bone("sight", [0, 3, -2], "receiver")
    builder.bone("foregrip", [0, -1, -8], "root")
    builder.bone("power_cell", [0, 0, 0], "receiver")
    builder.bone("muzzle", [0, 0, -16], "barrel")


def common_details(builder: ModelBuilder) -> None:
    builder.cube("receiver", "receiver_core", [-2.15, -1.45, -5.8], [4.3, 3.7, 9.8], "base")
    builder.cube("receiver", "receiver_upper_shell", [-1.8, 2.15, -5.2], [3.6, 0.7, 8.4], "light")
    builder.cube("receiver", "receiver_lower_spine", [-1.65, -2.15, -4.4], [3.3, 0.8, 6.4], "shadow")
    builder.cube("receiver", "receiver_left_plate", [-2.55, -0.8, -3.7], [0.5, 2.3, 5.5], "dark")
    builder.cube("receiver", "receiver_right_plate", [2.05, -0.8, -3.7], [0.5, 2.3, 5.5], "dark")
    builder.cube("grip", "pistol_grip", [-1.4, -6.2, 0.15], [2.8, 5.2, 3.0], "cloth",
                 rotation=[17, 0, 0], pivot=[0, -1, 1.4])
    builder.cube("grip", "trigger_guard", [-1.5, -3.2, -2.0], [3.0, 0.55, 2.7], "dark")
    builder.cube("power_cell", "power_cell", [-2.8, -0.15, -1.4], [0.65, 2.2, 4.1], "accent")
    builder.cube("sight", "rear_sight", [-1.05, 3.05, 0.6], [2.1, 0.8, 1.15], "dark")


def dc15(builder: ModelBuilder) -> None:
    common_details(builder)
    builder.cube("stock", "clone_shoulder_stock", [-2.5, -1.4, 3.7], [5.0, 3.9, 6.2], "dark")
    builder.cube("stock", "stock_cheek_rest", [-1.8, 2.35, 4.5], [3.6, 0.75, 4.8], "base")
    builder.cube("stock", "stock_neck", [-1.45, -0.7, 9.3], [2.9, 2.3, 3.1], "shadow")
    builder.cube("stock", "stock_butt", [-2.5, -1.5, 11.8], [5.0, 4.2, 1.15], "cloth")
    builder.cube("barrel", "dc15_barrel", [-0.8, -0.15, -23.5], [1.6, 1.5, 18.0], "shadow")
    builder.cube("barrel", "dc15_barrel_shroud", [-1.65, -0.75, -13.2], [3.3, 2.7, 7.5], "base")
    builder.cube("barrel", "dc15_shroud_left", [-2.1, -0.3, -12.5], [0.5, 1.8, 5.8], "dark")
    builder.cube("barrel", "dc15_shroud_right", [1.6, -0.3, -12.5], [0.5, 1.8, 5.8], "dark")
    builder.cube("barrel", "dc15_gas_tube", [-0.4, 1.55, -19.8], [0.8, 0.7, 12.2], "light")
    for index, z in enumerate((-10.5, -13.3, -16.1)):
        builder.cube("barrel", f"dc15_cooling_ring_{index}", [-1.25, -0.55, z],
                     [2.5, 2.3, 0.55], "light")
    builder.cube("sight", "dc15_scope_body", [-1.05, 3.35, -5.2], [2.1, 1.7, 7.0], "dark")
    builder.cube("sight", "dc15_scope_lens", [-0.82, 3.58, -5.6], [1.64, 1.24, 0.55], "accent")
    builder.cube("sight", "dc15_scope_mount_front", [-0.45, 2.65, -4.2], [0.9, 0.9, 0.8], "base")
    builder.cube("sight", "dc15_scope_mount_back", [-0.45, 2.65, 0.3], [0.9, 0.9, 0.8], "base")
    builder.cube("foregrip", "dc15_foregrip", [-1.05, -5.0, -10.4], [2.1, 3.9, 2.5], "cloth",
                 rotation=[-8, 0, 0], pivot=[0, -1, -8.8])
    builder.cube("muzzle", "dc15_muzzle_brake", [-1.45, -0.6, -24.2], [2.9, 2.4, 1.2], "dark")
    builder.cube("muzzle", "dc15_emitter", [-0.88, -0.05, -24.55], [1.76, 1.35, 0.45], "accent")


def e5(builder: ModelBuilder) -> None:
    builder.cube("receiver", "e5_round_receiver", [-1.9, -1.8, -4.8], [3.8, 4.5, 8.0], "base")
    builder.cube("receiver", "e5_receiver_spine", [-1.1, 2.65, -4.1], [2.2, 0.65, 6.7], "light")
    builder.cube("receiver", "e5_receiver_belly", [-1.25, -2.75, -2.8], [2.5, 1.0, 4.8], "shadow")
    builder.cube("receiver", "e5_left_mechanism", [-2.65, -0.7, -2.9], [0.8, 2.3, 4.6], "dark")
    builder.cube("receiver", "e5_right_mechanism", [1.85, -0.7, -2.9], [0.8, 2.3, 4.6], "dark")
    builder.cube("grip", "e5_grip", [-1.3, -6.3, 0.3], [2.6, 5.0, 2.8], "cloth",
                 rotation=[20, 0, 0], pivot=[0, -1, 1.4])
    builder.cube("grip", "e5_trigger_guard", [-1.4, -3.0, -1.8], [2.8, 0.55, 2.6], "dark")
    builder.cube("power_cell", "e5_side_cell", [1.9, -0.1, -1.5], [0.7, 1.9, 3.8], "accent")
    builder.cube("stock", "e5_stock_top", [-1.25, 1.45, 2.8], [2.5, 0.7, 8.8], "base")
    builder.cube("stock", "e5_stock_bottom", [-1.25, -1.35, 2.8], [2.5, 0.7, 8.8], "base")
    builder.cube("stock", "e5_stock_end", [-1.85, -1.6, 11.1], [3.7, 3.8, 0.9], "dark")
    builder.cube("stock", "e5_stock_pad", [-2.25, -1.8, 11.8], [4.5, 4.2, 0.7], "cloth")
    builder.cube("barrel", "e5_thin_barrel", [-0.62, 0.05, -23.0], [1.24, 1.2, 18.5], "dark")
    builder.cube("barrel", "e5_lower_support", [-0.38, -1.75, -18.2], [0.76, 0.7, 12.0], "base")
    for index, z in enumerate((-7.2, -10.2, -13.2, -16.2, -19.2)):
        builder.cube("barrel", f"e5_cooling_ring_{index}", [-1.2, -0.55, z],
                     [2.4, 2.45, 0.6], "light")
    builder.cube("sight", "e5_rear_post", [-0.65, 3.0, 0.7], [1.3, 1.35, 0.75], "dark")
    builder.cube("sight", "e5_front_post", [-0.5, 1.7, -18.5], [1.0, 1.2, 0.65], "light")
    builder.cube("foregrip", "e5_forward_handle", [-0.95, -4.8, -9.0], [1.9, 3.4, 2.0], "cloth",
                 rotation=[-7, 0, 0], pivot=[0, -1.2, -8.3])
    builder.cube("muzzle", "e5_muzzle_ring", [-1.15, -0.5, -23.65], [2.3, 2.3, 0.8], "dark")
    builder.cube("muzzle", "e5_emitter", [-0.7, -0.05, -23.95], [1.4, 1.4, 0.4], "accent")


def westar(builder: ModelBuilder) -> None:
    builder.cube("receiver", "westar_receiver", [-2.0, -1.35, -4.5], [4.0, 3.5, 7.6], "light")
    builder.cube("receiver", "westar_angular_upper", [-1.65, 2.05, -3.9], [3.3, 0.85, 5.8], "base")
    builder.cube("receiver", "westar_lower_frame", [-1.35, -2.0, -3.2], [2.7, 0.7, 4.6], "shadow")
    builder.cube("receiver", "westar_left_plate", [-2.45, -0.55, -2.8], [0.5, 2.1, 4.4], "dark")
    builder.cube("receiver", "westar_right_plate", [1.95, -0.55, -2.8], [0.5, 2.1, 4.4], "dark")
    builder.cube("grip", "westar_grip", [-1.45, -6.6, 0.15], [2.9, 5.5, 3.0], "cloth",
                 rotation=[18, 0, 0], pivot=[0, -1, 1.35])
    for index, y in enumerate((-5.5, -4.25, -3.0)):
        builder.cube("grip", f"westar_grip_rib_{index}", [-1.65, y, 0.35], [3.3, 0.35, 2.8], "dark")
    builder.cube("grip", "westar_trigger_guard", [-1.45, -3.0, -1.7], [2.9, 0.5, 2.4], "dark")
    builder.cube("power_cell", "westar_side_cell", [1.95, -0.15, -1.9], [0.65, 1.7, 3.5], "accent")
    builder.cube("barrel", "westar_short_barrel", [-0.9, -0.05, -10.3], [1.8, 1.7, 6.2], "shadow")
    builder.cube("barrel", "westar_barrel_collar", [-1.65, -0.55, -6.2], [3.3, 2.7, 1.7], "base")
    builder.cube("sight", "westar_rear_sight", [-0.7, 2.75, -0.5], [1.4, 0.8, 1.0], "dark")
    builder.cube("sight", "westar_front_sight", [-0.45, 2.0, -8.8], [0.9, 1.1, 0.65], "light")
    builder.cube("muzzle", "westar_fork_right", [-1.75, -0.45, -11.15], [1.1, 2.4, 1.45], "dark")
    builder.cube("muzzle", "westar_fork_left", [0.65, -0.45, -11.15], [1.1, 2.4, 1.45], "dark")
    builder.cube("muzzle", "westar_emitter", [-0.6, 0.0, -11.5], [1.2, 1.5, 0.45], "accent")


def scatter(builder: ModelBuilder) -> None:
    builder.cube("receiver", "scatter_receiver", [-3.25, -2.0, -5.0], [6.5, 5.1, 9.8], "base")
    builder.cube("receiver", "scatter_top_spine", [-2.5, 3.0, -4.2], [5.0, 0.8, 7.4], "light")
    builder.cube("receiver", "scatter_lower_frame", [-2.5, -2.75, -3.8], [5.0, 0.8, 6.6], "shadow")
    builder.cube("receiver", "scatter_left_heatshield", [-3.85, -0.8, -4.1], [0.7, 2.9, 6.8], "dark")
    builder.cube("receiver", "scatter_right_heatshield", [3.15, -0.8, -4.1], [0.7, 2.9, 6.8], "dark")
    for index, z in enumerate((-3.3, -1.3, 0.7)):
        builder.cube("receiver", f"scatter_left_vent_{index}", [-3.98, 0.15, z],
                     [0.35, 1.0, 0.8], "accent")
        builder.cube("receiver", f"scatter_right_vent_{index}", [3.63, 0.15, z],
                     [0.35, 1.0, 0.8], "accent")
    builder.cube("stock", "scatter_stock", [-2.9, -1.7, 4.4], [5.8, 4.4, 6.2], "dark")
    builder.cube("stock", "scatter_stock_neck", [-1.6, -0.7, 10.1], [3.2, 2.4, 2.8], "shadow")
    builder.cube("stock", "scatter_stock_pad", [-3.15, -1.9, 12.4], [6.3, 4.8, 1.0], "cloth")
    builder.cube("grip", "scatter_grip", [-1.55, -6.8, 0.25], [3.1, 5.4, 3.4], "cloth",
                 rotation=[17, 0, 0], pivot=[0, -1, 1.5])
    builder.cube("grip", "scatter_trigger_guard", [-1.65, -3.25, -2.1], [3.3, 0.55, 2.9], "dark")
    builder.cube("foregrip", "scatter_forward_grip", [-1.35, -5.3, -8.3], [2.7, 4.2, 2.8], "cloth",
                 rotation=[-10, 0, 0], pivot=[0, -1, -7.4])
    builder.cube("power_cell", "scatter_heat_bank", [-3.85, -1.0, -1.6], [0.75, 3.2, 4.8], "accent")
    builder.cube("barrel", "scatter_upper_barrel", [-1.25, 0.7, -17.2], [2.5, 2.1, 12.4], "shadow")
    builder.cube("barrel", "scatter_lower_barrel", [-1.25, -2.0, -17.2], [2.5, 2.1, 12.4], "shadow")
    for index, z in enumerate((-7.0, -10.0, -13.0)):
        builder.cube("barrel", f"scatter_barrel_bridge_{index}", [-2.25, -2.15, z],
                     [4.5, 5.1, 0.65], "light")
    builder.cube("barrel", "scatter_upper_rib", [-0.55, 2.8, -15.2], [1.1, 0.7, 9.4], "base")
    builder.cube("barrel", "scatter_lower_rib", [-0.55, -2.65, -15.2], [1.1, 0.7, 9.4], "base")
    builder.cube("sight", "scatter_reflex_sight", [-0.85, 3.75, -2.4], [1.7, 1.15, 2.2], "dark")
    builder.cube("muzzle", "scatter_upper_muzzle", [-1.75, 0.35, -17.8], [3.5, 2.8, 0.9], "dark")
    builder.cube("muzzle", "scatter_lower_muzzle", [-1.75, -2.35, -17.8], [3.5, 2.8, 0.9], "dark")
    builder.cube("muzzle", "scatter_upper_emitter", [-1.05, 1.0, -18.1], [2.1, 1.5, 0.45], "accent")
    builder.cube("muzzle", "scatter_lower_emitter", [-1.05, -1.7, -18.1], [2.1, 1.5, 0.45], "accent")


BUILDERS = {
    "dc15_blaster": dc15,
    "e5_blaster": e5,
    "westar_blaster": westar,
    "scatter_blaster": scatter,
}


def write_glowmask(texture_path: Path, palette: Palette) -> None:
    with Image.open(texture_path) as source:
        texture = source.convert("RGBA")
    glow = Image.new("RGBA", texture.size, (0, 0, 0, 0))
    for y in range(texture.height):
        for x in range(texture.width):
            pixel = texture.getpixel((x, y))
            if pixel[3] and sum(abs(pixel[index] - palette.accent[index]) for index in range(3)) <= 100:
                glow.putpixel((x, y), pixel)
    save_png(glow, texture_path.with_name(texture_path.stem + "_glowmask.png"))


def display_model(weapon_id: str) -> dict:
    pistol = weapon_id == "westar_blaster"
    heavy = weapon_id == "scatter_blaster"
    scale = 0.53 if heavy else 0.68 if pistol else 0.56
    first_person_scale = 0.46 if heavy else 0.62 if pistol else 0.49
    third_rotation = [-2, -90, -3] if pistol else [2, -90, 2]
    third_translation = [0.2, 2.2, -0.4] if pistol else [0.15, 2.0, -0.25]
    return {
        "parent": "builtin/entity",
        "ambientocclusion": False,
        "gui_light": "front",
        "display": {
            "thirdperson_righthand": {"rotation": third_rotation, "translation": third_translation, "scale": [scale] * 3},
            "thirdperson_lefthand": {"rotation": [third_rotation[0], 90, -third_rotation[2]], "translation": [-third_translation[0], third_translation[1], third_translation[2]], "scale": [scale] * 3},
            "firstperson_righthand": {"rotation": [0, -91, -2], "translation": [1.1, 2.5, 0.8], "scale": [first_person_scale] * 3},
            "firstperson_lefthand": {"rotation": [0, 91, 2], "translation": [-1.1, 2.5, 0.8], "scale": [first_person_scale] * 3},
            "gui": {"rotation": [18, 222, 0], "translation": [0, -0.2, 0], "scale": [0.50 if heavy else 0.56] * 3},
            "ground": {"rotation": [0, 90, 0], "translation": [0, 2, 0], "scale": [0.42] * 3},
            "fixed": {"rotation": [0, 90, 8], "translation": [0, 0, 0], "scale": [0.54] * 3},
        },
    }


def generate_all() -> None:
    for directory in (MODEL_ROOT, ANIMATION_ROOT, TEXTURE_ROOT, ITEM_MODEL_ROOT, ITEM_DEFINITION_ROOT):
        directory.mkdir(parents=True, exist_ok=True)
    animation = {
        "format_version": "1.8.0",
        "animations": {"animation.blaster.idle": {"loop": True, "animation_length": 1, "bones": {}}},
    }
    for design in DESIGNS:
        builder = ModelBuilder(
            f"item.blaster.{design.id}", design.palette, atlas_size=256, texel_density=2)
        bones(builder)
        BUILDERS[design.id](builder)
        model_path = MODEL_ROOT / f"{design.id}.geo.json"
        texture_path = TEXTURE_ROOT / f"{design.id}.png"
        builder.write(model_path, texture_path)
        model = json.loads(model_path.read_text(encoding="utf-8"))
        description = model["minecraft:geometry"][0]["description"]
        description.update({"visible_bounds_width": 5, "visible_bounds_height": 3, "visible_bounds_offset": [0, 0, -4]})
        model_path.write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
        write_glowmask(texture_path, design.palette)
        (ANIMATION_ROOT / f"{design.id}.animation.json").write_text(
            json.dumps(animation, indent=2) + "\n", encoding="utf-8")
        (ITEM_MODEL_ROOT / f"{design.id}.json").write_text(
            json.dumps(display_model(design.id), indent=2) + "\n", encoding="utf-8")
        (ITEM_DEFINITION_ROOT / f"{design.id}.json").write_text(json.dumps({
            "model": {
                "type": "minecraft:special",
                "base": f"galacticwars:item/{design.id}",
                "model": {"type": "geckolib:geckolib"},
            }
        }, indent=2) + "\n", encoding="utf-8")
    print(f"Generated {len(DESIGNS)} volumetric GeckoLib blasters")


if __name__ == "__main__":
    generate_all()
