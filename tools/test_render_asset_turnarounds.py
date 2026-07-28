"""Focused deterministic checks for the textured turnaround QA renderer."""

from __future__ import annotations

import json
import tempfile
from pathlib import Path

from PIL import Image

from render_asset_turnarounds import (
    ASSETS,
    RECRUIT_ASSETS,
    face_texture,
    render_turnaround,
)


RED = (224, 35, 45, 255)
GREEN = (35, 214, 104, 255)
BLUE = (42, 91, 224, 255)


def assert_all_recruits_are_cataloged() -> None:
    if len(RECRUIT_ASSETS) != 27 or len(set(RECRUIT_ASSETS)) != 27:
        raise AssertionError(f"Expected 27 unique turnaround recruits, found {len(RECRUIT_ASSETS)}")
    model_root = ASSETS / "geckolib/models/entity"
    texture_root = ASSETS / "textures/entity"
    for recruit in RECRUIT_ASSETS:
        if not (model_root / f"{recruit}.geo.json").is_file():
            raise AssertionError(f"Missing turnaround model for {recruit}")
        if not (texture_root / f"{recruit}.png").is_file():
            raise AssertionError(f"Missing turnaround texture for {recruit}")


def assert_negative_uv_size_mirrors_sampled_pixels() -> None:
    texture = Image.new("RGBA", (2, 1), RED)
    texture.putpixel((1, 0), BLUE)
    cube = {
        "size": [2, 1, 1],
        "uv": {"north": {"uv": [2, 0], "uv_size": [-2, 1]}},
    }
    patch = face_texture(texture, cube, "north")
    if patch is None:
        raise AssertionError("Mirrored UV patch was not sampled")
    sampled_pixels = list(patch.get_flattened_data())
    if sampled_pixels != [BLUE, RED]:
        raise AssertionError(f"Negative UV width was not mirrored: {sampled_pixels}")


def assert_rotated_cube_renders_actual_texture_pixels() -> None:
    with tempfile.TemporaryDirectory(prefix="galacticwars-turnaround-") as temp:
        temp_root = Path(temp)
        model_path = temp_root / "sampling_probe.geo.json"
        texture_path = temp_root / "sampling_probe.png"
        texture = Image.new("RGBA", (8, 8), RED)
        for y in range(texture.height):
            for x in range(texture.width):
                texture.putpixel((x, y), RED if (x + y) % 2 == 0 else GREEN)
        texture.save(texture_path)

        per_face_uv = {
            face: {"uv": [0, 0], "uv_size": [8, 8]}
            for face in ("north", "south", "east", "west", "up", "down")
        }
        model = {
            "format_version": "1.12.0",
            "minecraft:geometry": [{
                "description": {
                    "identifier": "geometry.galacticwars.turnaround_sampling_probe",
                    "texture_width": 8,
                    "texture_height": 8,
                    "visible_bounds_width": 1,
                    "visible_bounds_height": 1,
                    "visible_bounds_offset": [0, 0, 0],
                },
                "bones": [{
                    "name": "probe",
                    "pivot": [0, 4, 0],
                    "cubes": [{
                        "origin": [-2, 0, -2],
                        "size": [4, 8, 4],
                        "pivot": [0, 4, 0],
                        "rotation": [0, 25, 0],
                        "uv": per_face_uv,
                    }],
                }],
            }],
        }
        model_path.write_text(json.dumps(model), encoding="utf-8")
        preview = render_turnaround(model_path, texture_path, "sampling_probe")
        colors = set(preview.get_flattened_data())
        if RED not in colors or GREEN not in colors:
            raise AssertionError(
                "Rotated-cuboid preview did not preserve both high-contrast source texels"
            )


def main() -> None:
    assert_all_recruits_are_cataloged()
    assert_negative_uv_size_mirrors_sampled_pixels()
    assert_rotated_cube_renders_actual_texture_pixels()
    print("test_render_asset_turnarounds passed")


if __name__ == "__main__":
    main()
