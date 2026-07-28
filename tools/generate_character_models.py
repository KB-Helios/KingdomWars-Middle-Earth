"""Generate original UV-safe GeckoLib recruit and equipped-armor assets.

The geometry and every painted box face come from the same deterministic source so a model
change cannot silently leave a texture pointing at an unrelated UV layout.
"""

from __future__ import annotations

import hashlib
import io
import json
import math
import os
import tempfile
from dataclasses import dataclass
from pathlib import Path

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/galacticwars"
ENTITY_MODELS = ASSETS / "geckolib/models/entity"
ENTITY_ANIMATIONS = ASSETS / "geckolib/animations/entity"
ENTITY_TEXTURES = ASSETS / "textures/entity"
ARMOR_MODELS = ASSETS / "geckolib/models/armor"
ARMOR_ANIMATIONS = ASSETS / "geckolib/animations/armor"
ARMOR_TEXTURES = ASSETS / "textures/armor"
ITEM_GEO_MODELS = ASSETS / "geckolib/models/item"
ITEM_GEO_ANIMATIONS = ASSETS / "geckolib/animations/item"
SPAWN_CAPSULE_TEXTURES = ASSETS / "textures/item/spawn_capsule"
ITEM_DEFINITIONS = ASSETS / "items"
ITEM_MODELS = ASSETS / "models/item"
ITEM_TEXTURES = ASSETS / "textures/item"
CURATED_AUTHORIZED_OVERRIDES = (
    ENTITY_MODELS / "mandalorian_marksman.geo.json",
    ITEM_TEXTURES / "republic_plastoid_boots.png",
    ITEM_TEXTURES / "republic_plastoid_chestplate.png",
    ITEM_TEXTURES / "republic_plastoid_helmet.png",
    ITEM_TEXTURES / "republic_plastoid_leggings.png",
)
ATLAS_SIZE = 256
ENTITY_TEXEL_DENSITY = 2
ARMOR_ATLAS_SIZE = 1024
ARMOR_TEXEL_DENSITY = 6
DATHOMIR_RECRUIT_IDS = frozenset({
    "nightsister_acolyte",
    "nightsister_archer",
    "nightsister_civilian",
    "nightbrother_brute",
})
DATHOMIR_CLOTH_LABELS = (
    "hood", "spire", "robe", "sleeve", "wrap", "mantle", "skirt",
    "panel", "shawl", "apron", "sash", "tunic", "cloth", "waist",
)


Color = tuple[int, int, int]


def save_png(image: Image.Image, destination: Path, *, optimize: bool = False) -> None:
    buffer = io.BytesIO()
    image.save(buffer, format="PNG", optimize=optimize)
    destination.parent.mkdir(parents=True, exist_ok=True)
    descriptor, temporary_name = tempfile.mkstemp(dir=destination.parent, suffix=".png")
    try:
        with os.fdopen(descriptor, "wb") as temporary:
            temporary.write(buffer.getvalue())
        os.replace(temporary_name, destination)
    finally:
        Path(temporary_name).unlink(missing_ok=True)


@dataclass(frozen=True)
class Palette:
    skin: Color
    shadow: Color
    dark: Color
    base: Color
    light: Color
    accent: Color
    cloth: Color


@dataclass(frozen=True)
class RecruitDesign:
    id: str
    style: str
    variant: str
    palette: Palette


RECRUITS = (
    RecruitDesign("phase_i_clone_trooper", "plate", "phase_i_clone", Palette(
        (187, 126, 87), (34, 38, 45), (16, 20, 27), (195, 202, 204),
        (244, 245, 239), (152, 48, 38), (43, 48, 55))),
    RecruitDesign("phase_i_arc_trooper", "plate", "phase_i_arc", Palette(
        (177, 113, 77), (30, 35, 43), (14, 19, 27), (195, 203, 205),
        (244, 245, 239), (173, 54, 40), (38, 43, 51))),
    RecruitDesign("clone_trooper", "plate", "clone", Palette(
        (187, 126, 87), (30, 35, 43), (18, 22, 29), (190, 198, 201),
        (239, 241, 237), (42, 91, 170), (44, 50, 58))),
    RecruitDesign("arc_trooper", "plate", "arc", Palette(
        (177, 113, 77), (27, 32, 40), (15, 21, 29), (194, 201, 204),
        (241, 242, 236), (29, 102, 202), (38, 44, 53))),
    RecruitDesign("jedi_knight", "robe", "jedi", Palette(
        (185, 124, 79), (78, 47, 29), (48, 31, 23), (160, 118, 68),
        (222, 198, 142), (87, 220, 110), (91, 61, 39))),
    RecruitDesign("sith_acolyte", "robe", "sith", Palette(
        (116, 102, 96), (52, 19, 25), (12, 13, 17), (42, 43, 49),
        (91, 91, 98), (239, 44, 47), (70, 25, 31))),
    RecruitDesign("senate_commando", "plate", "senate_commando", Palette(
        (185, 124, 79), (31, 39, 59), (14, 20, 36), (42, 69, 116),
        (120, 157, 202), (235, 211, 112), (30, 39, 57))),
    RecruitDesign("republic_honor_guard", "plate", "honor_guard", Palette(
        (185, 124, 79), (80, 33, 32), (37, 22, 24), (146, 51, 48),
        (219, 177, 116), (236, 211, 146), (70, 34, 36))),
    RecruitDesign("b1_battle_droid", "droid_slim", "b1", Palette(
        (196, 163, 105), (87, 69, 45), (39, 37, 32), (151, 123, 78),
        (210, 180, 120), (232, 157, 48), (72, 65, 53))),
    RecruitDesign("b1_security_droid", "droid_slim", "b1", Palette(
        (196, 163, 105), (74, 65, 49), (34, 35, 32), (137, 122, 91),
        (207, 183, 133), (206, 49, 43), (65, 61, 51))),
    RecruitDesign("b2_super_battle_droid", "droid_heavy", "b2", Palette(
        (91, 98, 108), (42, 47, 54), (20, 23, 28), (75, 83, 94),
        (133, 144, 153), (244, 146, 32), (45, 49, 56))),
    RecruitDesign("commando_droid", "droid_slim", "commando", Palette(
        (59, 65, 72), (29, 33, 39), (13, 16, 20), (45, 51, 58),
        (103, 113, 121), (212, 47, 52), (31, 35, 40))),
    RecruitDesign("mandalorian_warrior", "plate", "warrior", Palette(
        (180, 119, 76), (43, 53, 56), (24, 31, 34), (84, 112, 113),
        (170, 192, 190), (25, 158, 156), (48, 54, 56))),
    RecruitDesign("mandalorian_marksman", "plate", "marksman", Palette(
        (177, 116, 73), (66, 61, 51), (31, 30, 28), (143, 131, 101),
        (199, 187, 150), (216, 98, 30), (56, 52, 47))),
    RecruitDesign("mandalorian_heavy", "plate", "heavy", Palette(
        (177, 116, 73), (38, 53, 67), (20, 27, 34), (73, 99, 129),
        (135, 163, 188), (83, 207, 229), (38, 45, 54))),
    RecruitDesign("hutt_enforcer", "brute", "enforcer", Palette(
        (112, 123, 62), (62, 69, 38), (36, 34, 29), (91, 101, 52),
        (145, 155, 84), (180, 116, 49), (55, 48, 39))),
    RecruitDesign("bounty_hunter", "plate", "hunter", Palette(
        (169, 107, 67), (55, 50, 43), (28, 27, 25), (93, 91, 81),
        (157, 154, 137), (225, 145, 31), (65, 50, 36))),
    RecruitDesign("smuggler", "civilian", "smuggler", Palette(
        (194, 132, 83), (92, 59, 36), (48, 35, 27), (151, 117, 77),
        (210, 191, 145), (67, 142, 198), (134, 60, 31))),
    RecruitDesign("nightsister_acolyte", "robe", "acolyte", Palette(
        (205, 199, 191), (65, 42, 35), (17, 16, 21), (101, 30, 43),
        (151, 53, 70), (91, 194, 118), (43, 22, 32))),
    RecruitDesign("nightsister_archer", "robe", "archer", Palette(
        (201, 194, 186), (67, 45, 34), (15, 17, 20), (82, 31, 40),
        (132, 49, 62), (174, 53, 65), (38, 24, 31))),
    RecruitDesign("nightbrother_brute", "brute", "nightbrother", Palette(
        (174, 55, 42), (65, 43, 34), (20, 18, 18), (82, 42, 35),
        (181, 98, 69), (216, 177, 126), (44, 29, 27))),
    RecruitDesign("republic_civilian", "civilian", "republic", Palette(
        (184, 123, 81), (65, 72, 82), (30, 34, 42), (99, 116, 139),
        (173, 188, 201), (52, 105, 186), (72, 80, 94))),
    RecruitDesign("togruta_civilian", "civilian", "togruta", Palette(
        (211, 102, 57), (55, 67, 91), (24, 34, 58), (79, 104, 151),
        (205, 216, 221), (45, 82, 151), (51, 66, 96))),
    RecruitDesign("separatist_technician", "droid_slim", "technician", Palette(
        (170, 139, 88), (76, 69, 55), (34, 34, 31), (115, 106, 88),
        (179, 165, 132), (225, 151, 44), (62, 59, 51))),
    RecruitDesign("mandalorian_clansperson", "civilian", "clansperson", Palette(
        (180, 119, 76), (48, 58, 58), (28, 33, 34), (84, 108, 108),
        (156, 178, 176), (31, 151, 150), (68, 65, 58))),
    RecruitDesign("hutt_civilian", "civilian", "hutt", Palette(
        (167, 119, 77), (74, 54, 39), (39, 31, 25), (132, 93, 61),
        (190, 149, 99), (205, 126, 45), (92, 68, 51))),
    RecruitDesign("nightsister_civilian", "civilian", "nightsister", Palette(
        (196, 188, 180), (73, 50, 38), (21, 19, 24), (87, 43, 60),
        (139, 77, 96), (191, 151, 112), (47, 27, 39))),
)


ARMOR_PALETTES = {
    "phase_i_clone": Palette((0, 0, 0), (50, 56, 64), (17, 22, 29),
                               (194, 202, 204), (244, 245, 239), (161, 48, 37), (43, 49, 57)),
    "republic_plastoid": Palette((0, 0, 0), (47, 54, 64), (18, 23, 31),
                                  (185, 195, 200), (240, 243, 239), (39, 88, 171), (45, 52, 62)),
    "separatist_alloy": Palette((0, 0, 0), (70, 61, 49), (31, 29, 26),
                                 (63, 57, 48), (117, 103, 82), (211, 126, 31), (48, 45, 40)),
    "mandalorian_alloy": Palette((0, 0, 0), (40, 57, 59), (21, 29, 32),
                                  (78, 111, 112), (166, 191, 188), (24, 158, 156), (45, 52, 54)),
    "nightsister_weave": Palette((0, 0, 0), (41, 18, 27), (12, 12, 16),
                                  (102, 27, 39), (151, 43, 52), (218, 34, 58), (24, 18, 24)),
    "beskar": Palette((0, 0, 0), (51, 62, 72), (24, 31, 38),
                       (124, 144, 155), (213, 225, 228), (63, 103, 132), (47, 55, 64)),
}


def clamp_color(color: Color, amount: int) -> Color:
    return tuple(max(0, min(255, channel + amount)) for channel in color)


class Atlas:
    def __init__(
            self,
            palette: Palette,
            salt: str,
            *,
            size: int = ATLAS_SIZE,
            texel_density: int = 1,
    ):
        self.palette = palette
        self.salt = salt
        self.size = size
        self.texel_density = texel_density
        self.image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        self.draw = ImageDraw.Draw(self.image)
        self.x = 1
        self.y = 1
        self.row_height = 0

    def allocate(
            self,
            size: tuple[float, float, float],
            label: str,
            material: str,
    ) -> list[int] | dict[str, dict[str, list[int]]]:
        width, height, depth = (max(1, math.ceil(value)) for value in size)
        if self.texel_density > 1:
            return self.allocate_faces((width, height, depth), label, material)
        footprint_width = depth * 2 + width * 2
        footprint_height = depth + height
        if self.x + footprint_width + 1 > self.size:
            self.x = 1
            self.y += self.row_height + 2
            self.row_height = 0
        if self.y + footprint_height + 1 > self.size:
            raise ValueError(f"{self.salt} atlas overflow while packing {label}")
        uv = [self.x, self.y]
        self.paint_box(uv, (width, height, depth), label, material)
        self.x += footprint_width + 2
        self.row_height = max(self.row_height, footprint_height)
        return uv

    def allocate_faces(
            self,
            size: tuple[int, int, int],
            label: str,
            material: str,
    ) -> dict[str, dict[str, list[int]]]:
        width, height, depth = size
        density = self.texel_density
        dimensions = {
            "north": (width * density, height * density),
            "south": (width * density, height * density),
            "east": (depth * density, height * density),
            "west": (depth * density, height * density),
            "up": (width * density, depth * density),
            "down": (width * density, depth * density),
        }
        mapping: dict[str, dict[str, list[int]]] = {}
        for face_name, (face_width, face_height) in dimensions.items():
            left, top = self.allocate_region(face_width, face_height, label)
            self.paint_high_density_face(
                (left, top, face_width, face_height), label, material, face_name)
            mapping[face_name] = {
                "uv": [left, top],
                "uv_size": [face_width, face_height],
            }
        return mapping

    def allocate_region(self, width: int, height: int, label: str) -> tuple[int, int]:
        padding = max(2, self.texel_density)
        if self.x + width + padding > self.size:
            self.x = padding
            self.y += self.row_height + padding
            self.row_height = 0
        if self.y + height + padding > self.size:
            raise ValueError(f"{self.salt} atlas overflow while packing {label}")
        position = self.x, self.y
        self.x += width + padding
        self.row_height = max(self.row_height, height)
        return position

    def paint_box(
            self,
            uv: list[int],
            size: tuple[int, int, int],
            label: str,
            material: str,
    ) -> None:
        u, v = uv
        width, height, depth = size
        faces = (
            ("top", (u + depth, v, width, depth)),
            ("bottom", (u + depth + width, v, width, depth)),
            ("left", (u, v + depth, depth, height)),
            ("front", (u + depth, v + depth, width, height)),
            ("right", (u + depth + width, v + depth, depth, height)),
            ("back", (u + depth * 2 + width, v + depth, width, height)),
        )
        base = getattr(self.palette, material)
        for face_name, (left, top, face_width, face_height) in faces:
            seed = int(hashlib.sha256(
                f"{self.salt}:{label}:{face_name}".encode("utf-8")).hexdigest()[:8], 16)
            for dy in range(face_height):
                for dx in range(face_width):
                    edge = dx == 0 or dy == 0 or dx == face_width - 1 or dy == face_height - 1
                    amount = -25 if edge else ((dx * 13 + dy * 7 + seed) % 13) - 6
                    if face_name == "top":
                        amount += 17
                    elif face_name in ("left", "bottom"):
                        amount -= 12
                    color = clamp_color(base, amount)
                    self.image.putpixel((left + dx, top + dy), (*color, 255))
            if face_width >= 4 and face_height >= 3:
                accent_period = 7 + seed % 5
                accent_y = top + 1 + seed % max(1, face_height - 2)
                for dx in range(1, face_width - 1):
                    if (dx + seed) % accent_period == 0 or material == "accent":
                        self.image.putpixel((left + dx, accent_y), (*clamp_color(base, 28), 255))

    def paint_high_density_face(
            self,
            rect: tuple[int, int, int, int],
            label: str,
            material: str,
            face_name: str,
    ) -> None:
        left, top, width, height = rect
        right = left + width - 1
        bottom = top + height - 1
        base = getattr(self.palette, material)
        seed = int(hashlib.sha256(
            f"{self.salt}:{label}:{face_name}:hd".encode("utf-8")).hexdigest()[:8], 16)
        family = self.salt.removeprefix("armor.")
        dathomir_surface = family in DATHOMIR_RECRUIT_IDS
        cloth_surface = (
            material == "cloth"
            or family == "nightsister_weave" and (
                "hood" in label or "skirt" in label or "shell" in label)
            or dathomir_surface and any(token in label for token in DATHOMIR_CLOTH_LABELS)
        )
        directional_light = {
            "up": 22,
            "down": -22,
            "north": 7,
            "south": -8,
            "east": -3,
            "west": 2,
        }[face_name]

        for dy in range(height):
            for dx in range(width):
                edge_distance = min(dx, dy, width - 1 - dx, height - 1 - dy)
                grain = ((seed + dx * 73 + dy * 151 + dx * dy * 11) % 17) - 8
                vertical = round(12 * (1 - dy / max(1, height - 1)))
                amount = directional_light + vertical + grain // (2 if cloth_surface else 4)
                if cloth_surface:
                    amount += 5 if (dx + dy + seed) % 7 == 0 else -3 if (dx - dy + seed) % 9 == 0 else 0
                elif edge_distance == 0:
                    amount -= 28
                elif edge_distance == 1:
                    amount += 14
                elif edge_distance == 2:
                    amount += 5
                self.image.putpixel((left + dx, top + dy), (*clamp_color(base, amount), 255))

        scale = self.texel_density
        dark = (*clamp_color(base, -52), 255)
        shadow = (*clamp_color(base, -27), 255)
        highlight = (*clamp_color(base, 27), 255)
        accent = (*self.palette.accent, 255)
        accent_light = (*clamp_color(self.palette.accent, 45), 255)
        plate_surface = not cloth_surface and material not in ("dark", "shadow")

        if cloth_surface:
            self.paint_weave(rect, seed)
        elif plate_surface and width >= 4 * scale and height >= 3 * scale:
            inset = max(2, scale)
            self.draw.rectangle(
                (left + inset, top + inset, right - inset, bottom - inset),
                outline=shadow,
                width=max(1, scale // 2),
            )
            self.draw.line(
                (left + inset + 1, top + inset + 1, right - inset - 1, top + inset + 1),
                fill=highlight,
                width=max(1, scale // 2),
            )
            hardware_labels = (
                "helmet_shell", "breastplate", "backplate", "pauldron",
                "gauntlet", "thigh", "shin", "heavy_collar", "power_cell",
            )
            if width >= 20 and height >= 16 and any(token in label for token in hardware_labels):
                fastener = max(1, scale // 3)
                for fx, fy in (
                        (left + inset + scale, top + inset + scale),
                        (right - inset - scale, bottom - inset - scale)):
                    self.draw.ellipse(
                        (fx - fastener, fy - fastener, fx + fastener, fy + fastener),
                        fill=dark,
                    )
                    self.draw.point((fx, fy), fill=highlight)

        if face_name == "north":
            self.paint_front_motif(rect, label, family, dark, shadow, highlight, accent, accent_light)
        elif face_name in ("east", "west") and height >= 5 * scale:
            seam_x = left + width // 2
            self.draw.line((seam_x, top + scale, seam_x, bottom - scale), fill=shadow, width=max(1, scale // 2))
        elif face_name == "south" and any(token in label for token in ("backplate", "compact_pack", "shell")):
            self.paint_back_motif(rect, dark, highlight, accent)

        if plate_surface:
            self.paint_wear(rect, seed, family, highlight, shadow)
        if dathomir_surface:
            self.paint_dathomir_detail(
                rect, label, material, face_name, family, seed)

    def paint_weave(self, rect: tuple[int, int, int, int], seed: int) -> None:
        left, top, width, height = rect
        right = left + width - 1
        bottom = top + height - 1
        thread_dark = (*clamp_color(self.palette.cloth, -18), 255)
        thread_light = (*clamp_color(self.palette.cloth, 20), 255)
        magic = (*clamp_color(self.palette.accent, 8), 255)
        step = max(3, self.texel_density + 1)
        for offset in range(-height, width, step):
            x0 = left + max(0, offset)
            y0 = top + max(0, -offset)
            length = min(right - x0, bottom - y0)
            if length > 0:
                self.draw.line((x0, y0, x0 + length, y0 + length), fill=thread_light)
        for offset in range(0, width + height, step):
            x0 = left + max(0, offset - height)
            y0 = bottom - min(height - 1, offset)
            length = min(right - x0, y0 - top)
            if length > 0:
                self.draw.line((x0, y0, x0 + length, y0 - length), fill=thread_dark)
        if width >= 12 and height >= 12:
            phase = seed % max(step * 2, 1)
            for y in range(top + step + phase // 2, bottom - step, step * 3):
                self.draw.line((left + step, y, right - step, y), fill=magic, width=1)

    def paint_front_motif(
            self,
            rect: tuple[int, int, int, int],
            label: str,
            family: str,
            dark: tuple[int, int, int, int],
            shadow: tuple[int, int, int, int],
            highlight: tuple[int, int, int, int],
            accent: tuple[int, int, int, int],
            accent_light: tuple[int, int, int, int],
    ) -> None:
        left, top, width, height = rect
        right = left + width - 1
        bottom = top + height - 1
        cx = left + width // 2
        scale = self.texel_density
        line = max(1, scale // 2)

        if "visor" in label:
            self.draw.rectangle((left + 1, top + 1, right - 1, bottom - 1), fill=dark)
            self.draw.line((left + scale, top + scale, right - scale, top + scale), fill=accent_light, width=line)
            self.draw.line((left + scale, bottom - scale, right - scale, bottom - scale), fill=accent, width=line)
            return
        if "face_guard" in label:
            for x in range(left + scale, right - scale, max(2, scale)):
                self.draw.line((x, top + scale, x, bottom - scale), fill=shadow, width=1)
            return
        if "helmet_shell" in label or "woven_hood" in label:
            self.draw.line((cx, top + scale, cx, bottom - scale), fill=shadow, width=line)
            self.draw.line((cx + line, top + scale, cx + line, bottom - scale), fill=highlight, width=1)
            vent_y = top + round(height * 0.72)
            for side in (-1, 1):
                start = cx + side * round(width * 0.18)
                end = cx + side * round(width * 0.38)
                self.draw.line((start, vent_y, end, vent_y), fill=dark, width=line)
                self.draw.line((start, vent_y + scale, end, vent_y + scale), fill=dark, width=1)
            if family == "republic_plastoid":
                stripe = max(scale, width // 8)
                self.draw.rectangle((cx - stripe // 2, top + scale, cx + stripe // 2, top + height // 3), fill=accent)
            elif family == "mandalorian_alloy":
                self.draw.line((left + scale, bottom - scale, right - scale, top + scale), fill=accent, width=line)
            elif family == "nightsister_weave":
                self.draw.line((left + scale, top + scale, cx, bottom - scale), fill=accent, width=line)
                self.draw.line((right - scale, top + scale, cx, bottom - scale), fill=accent_light, width=line)
            return
        if "breastplate" in label:
            shoulder_y = top + scale * 2
            sternum_y = top + round(height * 0.68)
            self.draw.line((left + scale, shoulder_y, cx, sternum_y, right - scale, shoulder_y), fill=shadow, width=line)
            self.draw.line((cx, top + scale, cx, bottom - scale), fill=dark, width=line)
            self.draw.line((left + scale, shoulder_y - 1, cx, sternum_y - 1), fill=highlight, width=1)
            self.draw.line((right - scale, shoulder_y - 1, cx, sternum_y - 1), fill=highlight, width=1)
            if family == "republic_plastoid":
                self.draw.rectangle((cx - scale, top + scale, cx + scale, top + scale * 2), fill=accent)
            elif family == "separatist_alloy":
                self.draw.rectangle((cx - scale, sternum_y, cx + scale, bottom - scale), fill=accent)
            elif family == "mandalorian_alloy":
                self.draw.line((left + scale, bottom - scale, cx, sternum_y), fill=accent, width=line)
            elif family == "nightsister_weave":
                self.draw.line((left + scale, top + scale, right - scale, bottom - scale), fill=accent, width=line)
                self.draw.line((right - scale, top + scale, left + scale, bottom - scale), fill=accent, width=line)
            return
        if "abdomen" in label or "power_cell" in label:
            for y in range(top + scale, bottom - scale, max(scale * 2, 3)):
                self.draw.line((left + scale, y, right - scale, y), fill=shadow, width=line)
                self.draw.line((left + scale, y - 1, right - scale, y - 1), fill=highlight, width=1)
            if "power_cell" in label:
                self.draw.rectangle((cx - scale, top + scale, cx + scale, bottom - scale), fill=accent)
                self.draw.line((cx, top + scale, cx, bottom - scale), fill=accent_light, width=line)
            return
        if "pauldron" in label or "collar" in label:
            self.draw.line((left + scale, bottom - scale, right - scale, top + scale), fill=accent, width=max(line, scale))
            self.draw.line((left + scale, bottom - scale - line, right - scale, top + scale - line), fill=accent_light, width=1)
            return
        if "gauntlet" in label:
            for y in range(top + scale * 2, bottom - scale, max(scale * 2, 3)):
                self.draw.line((left + scale, y, right - scale, y), fill=shadow, width=line)
            self.draw.rectangle((left + scale, top + scale, left + scale * 2, top + scale * 2), fill=accent)
            return
        if "thigh" in label or "shin" in label:
            panel = max(scale, width // 5)
            self.draw.rectangle((cx - panel, top + scale, cx + panel, bottom - scale), outline=shadow, width=line)
            if family in ("republic_plastoid", "mandalorian_alloy"):
                self.draw.line((cx, top + scale * 2, cx, bottom - scale * 2), fill=accent, width=line)
            return
        if "knee" in label:
            self.draw.polygon(
                ((cx, top + scale), (right - scale, top + height // 2),
                 (cx, bottom - scale), (left + scale, top + height // 2)),
                outline=highlight,
            )
            return
        if "boot" in label:
            toe_y = top + round(height * 0.62)
            self.draw.line((left + scale, toe_y, right - scale, toe_y), fill=shadow, width=line)
            self.draw.line((left + scale, toe_y - 1, right - scale, toe_y - 1), fill=highlight, width=1)
            return
        if "belt" in label:
            segment = max(scale * 2, width // 6)
            inset_y = max(1, min(scale, (height - 1) // 3))
            for x in range(left + scale, right - scale, segment):
                x1 = min(right - scale, x + segment - scale)
                if x1 >= x and bottom - inset_y >= top + inset_y:
                    self.draw.rectangle(
                        (x, top + inset_y, x1, bottom - inset_y),
                        outline=highlight,
                    )

    def paint_back_motif(
            self,
            rect: tuple[int, int, int, int],
            dark: tuple[int, int, int, int],
            highlight: tuple[int, int, int, int],
            accent: tuple[int, int, int, int],
    ) -> None:
        left, top, width, height = rect
        right = left + width - 1
        bottom = top + height - 1
        scale = self.texel_density
        cx = left + width // 2
        self.draw.line((cx, top + scale, cx, bottom - scale), fill=dark, width=max(1, scale // 2))
        for y in range(top + scale * 2, bottom - scale, max(3, scale * 2)):
            self.draw.line((left + scale, y, right - scale, y), fill=highlight)
        self.draw.rectangle((cx - scale, top + scale, cx + scale, top + scale * 2), fill=accent)

    def paint_wear(
            self,
            rect: tuple[int, int, int, int],
            seed: int,
            family: str,
            highlight: tuple[int, int, int, int],
            shadow: tuple[int, int, int, int],
    ) -> None:
        left, top, width, height = rect
        count = width * height // 800
        for index in range(min(6, count)):
            x = left + 2 + (seed // (index + 1) + index * 17) % max(1, width - 4)
            y = top + 2 + (seed // (index + 3) + index * 29) % max(1, height - 4)
            length = 1 + (seed + index * 7) % max(2, min(6, width // 4 + 1))
            color = highlight if (index + seed) % 3 else shadow
            self.draw.line((x, y, min(left + width - 2, x + length), max(top + 1, y - 1)), fill=color)
        if family == "beskar" and width >= 16 and height >= 12:
            for y in range(top + 4, top + height - 3, max(6, self.texel_density * 3)):
                tone = highlight if (y + seed) % 2 else shadow
                x0 = left + 3 + (seed + y) % max(1, width // 4)
                x1 = min(left + width - 4, x0 + max(4, width // 3))
                self.draw.line((x0, y, x1, y), fill=tone)

    def paint_dathomir_detail(
            self,
            rect: tuple[int, int, int, int],
            label: str,
            material: str,
            face_name: str,
            family: str,
            seed: int,
    ) -> None:
        """Layer authored Dathomirian pixel motifs over deterministic material grain."""
        left, top, width, height = rect
        right = left + width - 1
        bottom = top + height - 1
        scale = self.texel_density
        line = max(1, scale // 2)
        cx = left + width // 2
        palette = self.palette
        near_black = (*clamp_color(palette.dark, -4), 255)
        oxblood = (*palette.base, 255)
        burgundy = (*palette.cloth, 255)
        rust = (*palette.accent, 255)
        bone = (*palette.light, 255)
        leather = (*palette.shadow, 255)
        pale_shadow = (*clamp_color(palette.skin, -28), 255)
        pale_light = (*clamp_color(palette.skin, 20), 255)

        if label == "dathomir_face":
            if face_name == "north":
                self.draw.rectangle(
                    (left + scale, top + scale, right - scale, bottom - scale),
                    outline=pale_shadow,
                    width=line,
                )
                self.draw.line(
                    (left + scale * 2, top + height // 3,
                     cx, top + scale, right - scale * 2, top + height // 3),
                    fill=pale_light,
                    width=line,
                )
                cheek_y = top + round(height * 0.63)
                self.draw.line(
                    (left + scale * 2, cheek_y, cx - scale, bottom - scale * 2),
                    fill=pale_shadow,
                    width=line,
                )
                self.draw.line(
                    (right - scale * 2, cheek_y, cx + scale, bottom - scale * 2),
                    fill=pale_shadow,
                    width=line,
                )
            elif face_name in ("east", "west"):
                self.draw.line(
                    (left + scale, top + height // 2,
                     right - scale, top + height // 2),
                    fill=pale_shadow,
                    width=line,
                )
            return

        nightbrother_skin = (
            family == "nightbrother_brute"
            and material == "skin"
            and any(token in label for token in (
                "nightbrother_head", "nightbrother_torso", "massive_arm"))
        )
        if nightbrother_skin and face_name in ("north", "south", "east", "west"):
            band = max(scale, min(width, height) // 7)
            self.draw.line(
                (left + scale, top + scale,
                 right - scale, bottom - scale),
                fill=near_black,
                width=band,
            )
            self.draw.line(
                (right - scale, top + scale,
                 left + scale, bottom - scale),
                fill=near_black,
                width=max(line, band - scale),
            )
            if height >= 8 * scale:
                y = top + height // 2
                self.draw.line(
                    (left + scale, y, right - scale, y),
                    fill=leather,
                    width=line,
                )
            return

        cloth_piece = any(token in label for token in DATHOMIR_CLOTH_LABELS)
        if cloth_piece and width >= 4 * scale and height >= 3 * scale:
            fold_step = max(scale * 3, width // 4)
            for index, x in enumerate(range(left + scale * 2, right - scale, fold_step)):
                tone = near_black if index % 2 == 0 else oxblood
                self.draw.line(
                    (x, top + scale, x, bottom - scale),
                    fill=tone,
                    width=line,
                )
                if x + line + 1 < right:
                    self.draw.line(
                        (x + line + 1, top + scale, x + line + 1, bottom - scale),
                        fill=burgundy,
                    )
            if face_name in ("north", "south"):
                hem_y = bottom - scale
                self.draw.line(
                    (left + scale, hem_y, right - scale, hem_y),
                    fill=rust if family == "nightsister_acolyte" else leather,
                    width=line,
                )
                stitch_step = max(3, scale * 2)
                for x in range(left + scale, right - scale, stitch_step):
                    self.draw.point((x, hem_y), fill=bone)

        if any(token in label for token in (
                "hood", "spire", "headwrap", "shawl", "mantle")):
            if (
                    face_name in ("north", "south")
                    and width >= 5 * scale
                    and height >= 3 * scale
            ):
                patch_width = max(scale * 2, width // 4)
                patch_height = max(scale * 2, height // 4)
                patch_x = left + scale + seed % max(1, width - patch_width - scale)
                patch_y = top + scale + (seed // 7) % max(1, height - patch_height - scale)
                patch_right = min(right - scale, patch_x + patch_width)
                patch_bottom = min(bottom - scale, patch_y + patch_height)
                self.draw.rectangle(
                    (patch_x, patch_y, patch_right, patch_bottom),
                    outline=near_black,
                    fill=burgundy,
                )
                self.draw.line(
                    (patch_x, patch_y, patch_right, patch_bottom),
                    fill=oxblood,
                )

        if "patterned_apron" in label and face_name == "north":
            cell = max(2, scale * 2)
            for y in range(top + scale, bottom - scale, cell):
                for x in range(left + scale, right - scale, cell):
                    if ((x - left) // cell + (y - top) // cell) % 2 == 0:
                        self.draw.rectangle(
                            (x, y, min(right - scale, x + cell - 1),
                             min(bottom - scale, y + cell - 1)),
                            fill=oxblood,
                        )
                    else:
                        self.draw.point((x, y), fill=leather)

        leather_piece = any(token in label for token in (
            "belt", "quiver", "satchel", "harness", "bracer", "boot", "cuff",
            "buckle", "strap",
        ))
        if leather_piece and width >= 3 * scale and height >= 3 * scale:
            self.draw.rectangle(
                (left + scale, top + scale, right - scale, bottom - scale),
                outline=near_black,
                width=line,
            )
            if face_name in ("north", "south"):
                stitch_step = max(3, scale * 2)
                for x in range(left + scale * 2, right - scale, stitch_step):
                    self.draw.point((x, top + scale), fill=bone)
                    self.draw.point((x, bottom - scale), fill=bone)

        bone_piece = any(token in label for token in (
            "bone", "talisman", "horn", "tusk", "tooth", "clasp", "arrow",
        ))
        if bone_piece and max(width, height) >= 3 * scale:
            segment_step = max(scale * 2, (height if height >= width else width) // 4)
            if height >= width:
                for y in range(top + scale, bottom - scale, segment_step):
                    self.draw.line(
                        (left + line, y, right - line, y),
                        fill=leather,
                        width=line,
                    )
            else:
                for x in range(left + scale, right - scale, segment_step):
                    self.draw.line(
                        (x, top + line, x, bottom - line),
                        fill=leather,
                        width=line,
                    )

        if any(token in label for token in (
                "tattoo", "cheek_mark", "forehead_chevron")):
            inset = max(line, scale)
            self.draw.line(
                (left + inset, top + inset, right - inset, bottom - inset),
                fill=near_black,
                width=inset,
            )


class ModelBuilder:
    def __init__(
            self,
            identifier: str,
            palette: Palette,
            *,
            atlas_size: int = ATLAS_SIZE,
            texel_density: int = ENTITY_TEXEL_DENSITY,
    ):
        self.identifier = identifier
        self.atlas = Atlas(
            palette,
            identifier,
            size=atlas_size,
            texel_density=texel_density,
        )
        self.bones: dict[str, dict] = {}

    def bone(self, name: str, pivot: list[float], parent: str | None = None) -> None:
        bone = {"name": name, "pivot": pivot, "cubes": []}
        if parent is not None:
            bone["parent"] = parent
        self.bones[name] = bone

    def cube(
            self,
            bone: str,
            label: str,
            origin: list[float],
            size: list[float],
            material: str,
            *,
            inflate: float | None = None,
            rotation: list[float] | None = None,
            pivot: list[float] | None = None,
            mirror: bool = False,
    ) -> None:
        cube = {"origin": origin, "size": size, "uv": self.atlas.allocate(tuple(size), label, material)}
        if inflate is not None:
            cube["inflate"] = inflate
        if rotation is not None:
            cube["rotation"] = rotation
        if pivot is not None:
            cube["pivot"] = pivot
        if mirror:
            cube["mirror"] = True
        self.bones[bone]["cubes"].append(cube)

    def write(self, model_path: Path, texture_path: Path) -> None:
        model_path.parent.mkdir(parents=True, exist_ok=True)
        texture_path.parent.mkdir(parents=True, exist_ok=True)
        document = {
            "format_version": "1.12.0",
            "minecraft:geometry": [{
                "description": {
                    "identifier": f"geometry.galacticwars.{self.identifier}",
                    "texture_width": self.atlas.size,
                    "texture_height": self.atlas.size,
                    "visible_bounds_width": 3,
                    "visible_bounds_height": 4,
                    "visible_bounds_offset": [0, 1.5, 0],
                },
                "bones": list(self.bones.values()),
            }],
        }
        model_path.write_text(json.dumps(document, indent=2) + "\n", encoding="utf-8")
        save_png(self.atlas.image, texture_path)


def humanoid_bones(builder: ModelBuilder) -> None:
    builder.bone("head", [0, 24, 0])
    builder.bone("body", [0, 24, 0])
    builder.bone("right_arm", [-5, 22, 0])
    builder.bone("left_arm", [5, 22, 0])
    builder.bone("right_leg", [-1.9, 12, 0])
    builder.bone("left_leg", [1.9, 12, 0])
    builder.bone("RightHandItem", [-6, 12, 0], "right_arm")
    builder.bone("LeftHandItem", [6, 12, 0], "left_arm")


def recruit_child_bones(builder: ModelBuilder, design: RecruitDesign) -> None:
    """Declare stable animation and silhouette bones without replacing held-item anchors."""
    if design.style == "plate":
        builder.bone("helmet", [0, 24, 0], "head")
        builder.bone("chest_armor", [0, 24, 0], "body")
        builder.bone("right_gauntlet", [-6, 16, 0], "right_arm")
        builder.bone("left_gauntlet", [6, 16, 0], "left_arm")
        builder.bone("right_boot", [-2, 5, 0], "right_leg")
        builder.bone("left_boot", [2, 5, 0], "left_leg")
        builder.bone("backpack", [0, 19, 2], "body")
        if design.variant in ("warrior", "heavy", "hunter"):
            builder.bone("jetpack", [0, 19, 2], "body")
        if design.variant in ("arc", "phase_i_arc"):
            builder.bone("rangefinder", [4.5, 29, 0], "helmet")
            builder.bone("pauldron", [-4.5, 21, 0], "chest_armor")
            builder.bone("kama", [0, 12, 0], "body")
        elif design.variant in ("marksman", "hunter"):
            builder.bone("rangefinder", [4.5, 29, 0], "helmet")
    elif design.style.startswith("droid"):
        builder.bone("neck", [0, 25, 0], "body")
        builder.bone("torso_core", [0, 18, 0], "body")
        builder.bone("right_forearm", [-6, 17, 0], "right_arm")
        builder.bone("left_forearm", [6, 17, 0], "left_arm")
        builder.bone("right_shin", [-2, 6, 0], "right_leg")
        builder.bone("left_shin", [2, 6, 0], "left_leg")
        builder.bone("right_foot", [-2, 1, -1], "right_shin")
        builder.bone("left_foot", [2, 1, -1], "left_shin")
        builder.bone("utility_pack", [0, 18, 2], "body")
        if design.variant == "b2":
            builder.bone("forearm_cannon", [-6, 16, -2], "right_forearm")
        elif design.variant == "commando":
            builder.bone("commando_head_profile", [0, 28, 0], "head")
        if design.variant == "technician":
            builder.bone("headgear", [0, 29, 0], "head")
            builder.bone("utility_belt", [0, 13, 0], "body")
    elif design.style == "robe":
        builder.bone("hood", [0, 29, 0], "head")
        builder.bone("robe_mantle", [0, 22, 0], "body")
        builder.bone("robe_skirt", [0, 12, 0], "body")
        builder.bone("utility_gear", [0, 15, 0], "body")
        builder.bone("cloth_panels", [0, 12, 0], "robe_skirt")
        if design.variant == "acolyte":
            builder.bone("ritual_spires", [0, 30, 0], "hood")
            builder.bone("talisman_cords", [0, 19, -2], "robe_mantle")
        if design.variant == "archer":
            builder.bone("quiver", [3, 19, 2], "body")
            builder.bone("hair_locks", [0, 28, 2], "head")
            builder.bone("archer_sash", [0, 14, 0], "body")
    elif design.style == "civilian":
        builder.bone("hair", [0, 29, 0], "head")
        builder.bone("outerwear", [0, 20, 0], "body")
        builder.bone("utility_belt", [0, 13, 0], "body")
        builder.bone("footwear", [0, 4, 0], "body")
        if design.variant == "smuggler":
            builder.bone("holster", [4, 11, 0], "utility_belt")
        elif design.variant == "nightsister":
            builder.bone("hood", [0, 29, 0], "head")
            builder.bone("robe_skirt", [0, 12, 0], "body")
            builder.bone("shawl", [0, 21, 0], "outerwear")
            builder.bone("woven_apron", [0, 13, -2], "body")
            builder.bone("satchel", [4, 13, 1], "utility_belt")
        elif design.variant == "clansperson":
            builder.bone("helmet", [0, 29, 0], "head")
            builder.bone("chest_armor", [0, 20, 0], "body")
            builder.bone("right_gauntlet", [-6, 16, 0], "right_arm")
            builder.bone("left_gauntlet", [6, 16, 0], "left_arm")
    elif design.style == "brute":
        builder.bone("neck", [0, 24, 0], "body")
        builder.bone("harness", [0, 19, 0], "body")
        builder.bone("chest_armor", [0, 19, 0], "body")
        builder.bone("right_bracer", [-7, 15, 0], "right_arm")
        builder.bone("left_bracer", [7, 15, 0], "left_arm")
        builder.bone("horns", [0, 31, 0], "head")
        if design.variant == "nightbrother":
            builder.bone("face_tattoo_vertical", [0, 28, -4], "head")
            builder.bone("shoulder_harness", [-3, 21, 0], "chest_armor")
            builder.bone("waist_cloth", [0, 12, 0], "body")
        if design.variant == "enforcer":
            builder.bone("shoulder_armor", [0, 22, 0], "chest_armor")
            builder.bone("utility_belt", [0, 12, 0], "body")


def basic_humanoid(builder: ModelBuilder, head_material: str = "skin", body_material: str = "cloth") -> None:
    builder.cube("head", "face", [-4, 24, -4], [8, 8, 8], head_material)
    builder.cube("body", "tunic", [-4, 12, -2], [8, 12, 4], body_material)
    builder.cube("right_arm", "right_sleeve", [-8, 12, -2], [4, 12, 4], body_material)
    builder.cube("left_arm", "left_sleeve", [4, 12, -2], [4, 12, 4], body_material, mirror=True)
    builder.cube("right_leg", "right_trouser", [-4, 0, -2], [4, 12, 4], "dark")
    builder.cube("left_leg", "left_trouser", [0, 0, -2], [4, 12, 4], "dark", mirror=True)


def add_human_face(builder: ModelBuilder) -> None:
    """Add readable facial features without relying on a flat skin template."""
    builder.cube("head", "right_eye", [-2.8, 28.0, -4.24], [1.35, 0.8, 0.28], "dark")
    builder.cube("head", "left_eye", [1.45, 28.0, -4.24], [1.35, 0.8, 0.28], "dark")
    builder.cube("head", "nose", [-0.55, 26.55, -4.34], [1.1, 1.45, 0.38], "skin")
    builder.cube("head", "mouth", [-1.35, 25.35, -4.25], [2.7, 0.55, 0.26], "shadow")


def add_hair_cap(builder: ModelBuilder, prefix: str, material: str = "shadow") -> None:
    """Build hair around the face instead of covering it with an enclosing cube."""
    bone = "hood" if "hood" in builder.bones else "hair" if "hair" in builder.bones else "head"
    builder.cube(bone, f"{prefix}_top", [-4.15, 30.55, -4.15], [8.3, 1.6, 8.3], material)
    builder.cube(bone, f"{prefix}_back", [-4.15, 24.0, 3.45], [8.3, 6.8, 0.7], material)
    builder.cube(bone, f"{prefix}_right", [-4.15, 24.0, -4.15], [0.7, 6.8, 8.3], material)
    builder.cube(bone, f"{prefix}_left", [3.45, 24.0, -4.15], [0.7, 6.8, 8.3], material)
    builder.cube(bone, f"{prefix}_fringe", [-4.0, 29.55, -4.28], [8.0, 1.15, 0.45], material)


def add_open_hood(builder: ModelBuilder, prefix: str, material: str = "cloth") -> None:
    """Create a deep hood with an open face and a visible inner shadow."""
    bone = "hood" if "hood" in builder.bones else "hair" if "hair" in builder.bones else "head"
    builder.cube(bone, f"{prefix}_crown", [-4.55, 30.45, -4.55], [9.1, 2.3, 9.1], material)
    builder.cube(bone, f"{prefix}_back", [-4.55, 23.8, 3.45], [9.1, 7.0, 1.1], material)
    builder.cube(bone, f"{prefix}_right", [-4.55, 23.8, -4.55], [1.25, 7.0, 8.4], material)
    builder.cube(bone, f"{prefix}_left", [3.3, 23.8, -4.55], [1.25, 7.0, 8.4], material)
    builder.cube(bone, f"{prefix}_brow", [-3.35, 29.75, -4.65], [6.7, 0.85, 0.65], "shadow")


def add_plate_details(builder: ModelBuilder, variant: str) -> None:
    clone = variant in ("clone", "arc", "phase_i_clone", "phase_i_arc")
    phase_i = variant in ("phase_i_clone", "phase_i_arc")
    helmet = "helmet"
    chest = "chest_armor"
    builder.cube(helmet, "helmet_shell", [-4, 24, -4], [8, 8, 8], "light", inflate=0.42)
    builder.cube(helmet, "visor", [-3.6, 27.2 if phase_i else 27.7, -4.7], [7.2, 1.25, 0.8], "dark" if clone else "accent")
    if phase_i:
        builder.cube(helmet, "phase_i_helmet_fin", [-0.65, 31.5, -3.25], [1.3, 1.5, 6.5], "accent")
        builder.cube(helmet, "phase_i_mouth_grille", [-2.5, 24.2, -4.78], [5, 1.5, 0.85], "shadow")
        builder.cube(helmet, "phase_i_cheek_plate", [-3.45, 24.7, -4.72], [6.9, 2.3, 0.72], "base")
    elif clone:
        builder.cube(helmet, "phase_ii_brow", [-3.7, 28.85, -4.74], [7.4, 0.7, 0.72], "accent")
        builder.cube(helmet, "phase_ii_resp_knot", [-1.55, 24.0, -5.0], [3.1, 1.8, 1.0], "shadow")
        builder.cube(helmet, "phase_ii_cheek_right", [-3.5, 24.5, -4.75], [2.2, 2.7, 0.7], "light")
        builder.cube(helmet, "phase_ii_cheek_left", [1.3, 24.5, -4.75], [2.2, 2.7, 0.7], "light")
    else:
        builder.cube(helmet, "vertical_visor", [-0.72, 24.5, -4.76], [1.44, 3.0, 0.82], "dark")
    builder.cube(helmet, "jaw_guard", [-3.2, 24.2, -4.65], [6.4, 2.1, 0.75], "base")
    builder.cube(chest, "breastplate", [-4.25, 17.5, -2.55], [8.5, 5.7, 1.2], "light")
    builder.cube(chest, "abdomen", [-3.7, 13.2, -2.45], [7.4, 4.0, 0.9], "shadow")
    builder.cube(chest, "backplate", [-4.2, 15.0, 1.75], [8.4, 7.7, 1.1], "base")
    builder.cube(chest, "belt", [-4.5, 11.7, -2.6], [9, 1.7, 5.2], "dark")
    if clone:
        builder.cube(chest, "chest_unit_marking", [-0.7, 18.0, -2.72], [1.4, 4.4, 0.35], "accent")
    for side, bone, x in (("right", "right_arm", -8.65), ("left", "left_arm", 3.65)):
        builder.cube(bone, f"{side}_pauldron", [x, 18.4, -2.6], [5, 4.6, 5.2], "base", inflate=0.18)
        builder.cube(f"{side}_gauntlet", f"{side}_gauntlet_shell", [x + 0.45, 12.1, -2.35], [4.1, 4.8, 4.7], "shadow")
    for side, bone, x in (("right", "right_leg", -4.15), ("left", "left_leg", 0.15)):
        builder.cube(bone, f"{side}_thigh", [x, 6.3, -2.35], [4, 5.2, 4.7], "base")
        builder.cube(bone, f"{side}_knee", [x - 0.1, 4.4, -2.8], [4.2, 2.2, 1.2], "accent")
        builder.cube(f"{side}_boot", f"{side}_shin", [x, 0.3, -2.45], [4, 4.1, 4.9], "light")
    if variant in ("arc", "phase_i_arc", "marksman", "hunter"):
        builder.cube("rangefinder", "rangefinder_stem", [4.35, 27.0, -0.7], [0.8, 5.0, 1.2], "accent")
        builder.cube("rangefinder", "rangefinder_sensor", [3.8, 31.0, -3.8], [1.8, 1.2, 3.4], "dark")
    if variant in ("arc", "phase_i_arc"):
        builder.cube("pauldron", "command_pauldron", [-6.7, 18.0, -2.8], [5.2, 5.2, 5.6], "accent")
        builder.cube("kama", "right_kama", [-4.2, 7.5, -2.4], [3.6, 4.5, 4.8], "cloth")
        builder.cube("kama", "left_kama", [0.6, 7.5, -2.4], [3.6, 4.5, 4.8], "cloth")
    if variant in ("warrior", "marksman", "heavy", "hunter"):
        builder.cube(helmet, "visor_lower", [-0.75, 24.3, -4.82], [1.5, 4.2, 0.3], "accent")
        pack_bone = "jetpack" if "jetpack" in builder.bones else "backpack"
        builder.cube(pack_bone, "jetpack", [-2.8, 15.0, 2.0], [5.6, 7.4, 2.2], "dark")
    if variant == "hunter":
        builder.cube("backpack", "weathered_cape", [1.0, 9.0, 2.35], [4.2, 12.5, 0.7], "cloth",
                     rotation=[0, 0, -4], pivot=[2.5, 20, 2])
        builder.cube(chest, "hunter_bandolier", [-4.2, 14.5, -2.85], [8.4, 1.35, 0.7], "accent",
                     rotation=[0, 0, -28], pivot=[0, 18, -2.5])
    if variant == "heavy":
        builder.cube(chest, "heavy_collar", [-5.0, 20.7, -2.8], [10, 2.7, 5.6], "light")
        builder.cube("right_arm", "right_heavy_shoulder", [-9.7, 17.8, -3.2], [6.4, 5.8, 6.4], "light")
        builder.cube("left_arm", "left_heavy_shoulder", [3.3, 17.8, -3.2], [6.4, 5.8, 6.4], "light")


def add_robe_details(builder: ModelBuilder, variant: str) -> None:
    if variant in ("acolyte", "archer", "sith"):
        add_open_hood(builder, "ritual_hood")
    else:
        add_hair_cap(builder, "hair", "dark")
        builder.cube("hood", "lowered_hood_roll", [-4.35, 21.4, -2.8], [8.7, 2.2, 5.6], "cloth")
    builder.cube("robe_mantle", "cross_tunic", [-4.25, 15.5, -2.45], [8.5, 7.8, 4.9], "light")
    builder.cube("utility_gear", "sash", [-4.45, 12.1, -2.55], [8.9, 2.0, 5.1], "accent")
    builder.cube("robe_skirt", "robe_skirt", [-4.3, 7.4, -2.4], [8.6, 4.8, 4.8], "base")
    builder.cube("cloth_panels", "right_front_panel", [-3.8, 4.2, -2.72], [3.3, 7.7, 0.72], "cloth",
                 rotation=[0, 0, 3], pivot=[-1.9, 11.5, -2.3])
    builder.cube("cloth_panels", "left_front_panel", [0.5, 4.2, -2.72], [3.3, 7.7, 0.72], "light",
                 rotation=[0, 0, -3], pivot=[1.9, 11.5, -2.3])
    builder.cube("robe_mantle", "right_wrap", [-4.15, 18.2, -2.72], [5.0, 1.1, 0.7], "cloth",
                 rotation=[0, 0, -24], pivot=[0, 19, -2.5])
    builder.cube("robe_mantle", "left_wrap", [-0.85, 18.2, -2.78], [5.0, 1.1, 0.72], "light",
                 rotation=[0, 0, 24], pivot=[0, 19, -2.5])
    builder.cube("right_leg", "right_boot", [-4.15, 0, -2.2], [4.1, 5.5, 4.4], "shadow")
    builder.cube("left_leg", "left_boot", [0.05, 0, -2.2], [4.1, 5.5, 4.4], "shadow", mirror=True)
    if variant in ("acolyte", "archer", "sith"):
        builder.cube("robe_mantle", "right_talisman", [-3.2, 13.5, -2.7], [1.2, 7.5, 0.8], "accent")
        builder.cube("robe_mantle", "left_talisman", [2.0, 13.5, -2.7], [1.2, 7.5, 0.8], "accent")
        builder.cube("right_arm", "right_cloth_wrap", [-8.25, 13.0, -2.35], [4.5, 2.0, 4.7], "light")
        builder.cube("left_arm", "left_cloth_wrap", [3.75, 13.0, -2.35], [4.5, 2.0, 4.7], "light")
    if variant == "sith":
        builder.cube("robe_mantle", "right_armored_pauldron", [-7.4, 20.0, -3.0], [4.0, 3.0, 6.0], "dark")
        builder.cube("robe_mantle", "left_armored_pauldron", [3.4, 20.0, -3.0], [4.0, 3.0, 6.0], "dark")
        builder.cube("hood", "mask_brow", [-3.0, 27.6, -4.75], [6.0, 1.0, 0.7], "accent")
        builder.cube("hood", "mask_guard", [-2.5, 24.4, -4.7], [5.0, 3.3, 0.65], "dark")
        builder.cube("utility_gear", "sith_belt_clasp", [-1.2, 11.7, -2.9], [2.4, 2.4, 0.6], "light")
    if variant == "archer":
        builder.cube("quiver", "quiver_case", [2.5, 15.0, 1.8], [2.6, 8.5, 2.6], "shadow",
                     rotation=[0, 0, -12], pivot=[3.5, 18, 2])
        for index, x in enumerate((2.8, 3.6, 4.4)):
            builder.cube("quiver", f"arrow_{index}", [x, 22.2, 2.45], [0.35, 3.5, 0.35], "accent")


def add_dathomir_face(builder: ModelBuilder, *, tattoos: bool = True) -> None:
    """Paint a pale, angular Dathomirian face as layered geometry."""
    builder.cube("head", "dathomir_face", [-4, 24, -4], [8, 8, 8], "skin")
    builder.cube("head", "right_eye", [-2.9, 27.9, -4.28], [1.45, 0.75, 0.32], "dark")
    builder.cube("head", "left_eye", [1.45, 27.9, -4.28], [1.45, 0.75, 0.32], "dark")
    builder.cube("head", "nose_bridge", [-0.55, 26.2, -4.34], [1.1, 1.8, 0.4], "skin")
    builder.cube("head", "mouth_mark", [-1.45, 25.15, -4.28], [2.9, 0.5, 0.3], "dark")
    if tattoos:
        builder.cube("head", "forehead_chevron", [-0.5, 28.8, -4.36], [1.0, 2.0, 0.34], "base")
        builder.cube(
            "head", "right_cheek_mark", [-3.2, 26.2, -4.34], [2.0, 0.48, 0.3],
            "base", rotation=[0, 0, -18], pivot=[-2.1, 26.4, -4.1])
        builder.cube(
            "head", "left_cheek_mark", [1.2, 26.2, -4.34], [2.0, 0.48, 0.3],
            "base", rotation=[0, 0, 18], pivot=[2.1, 26.4, -4.1])


def add_dathomir_sister(builder: ModelBuilder, variant: str) -> None:
    """Build distinct ritualist and archer silhouettes from the project concept board."""
    add_dathomir_face(builder)
    if variant == "acolyte":
        builder.cube("body", "wrapped_underrobe", [-4.1, 12, -2.15], [8.2, 12, 4.3], "dark")
        builder.cube("right_arm", "right_layered_sleeve", [-8.15, 12, -2.2], [4.15, 12, 4.4], "cloth")
        builder.cube("left_arm", "left_layered_sleeve", [4, 12, -2.2], [4.15, 12, 4.4], "cloth", mirror=True)
        builder.cube("right_leg", "right_wrapped_leg", [-4, 0, -2.1], [4, 12, 4.2], "shadow")
        builder.cube("left_leg", "left_wrapped_leg", [0, 0, -2.1], [4, 12, 4.2], "shadow", mirror=True)

        builder.cube("hood", "split_hood_back", [-4.7, 23.7, 3.45], [9.4, 7.4, 1.2], "cloth")
        builder.cube("hood", "split_hood_right", [-4.7, 23.7, -4.5], [1.3, 7.5, 8.0], "cloth")
        builder.cube("hood", "split_hood_left", [3.4, 23.7, -4.5], [1.3, 7.5, 8.0], "cloth")
        builder.cube("hood", "hood_brow", [-3.35, 29.65, -4.7], [6.7, 1.0, 0.75], "shadow")
        for side, x, angle in (("right", -4.55, -5), ("left", 2.0, 5)):
            builder.cube(
                "ritual_spires", f"{side}_ritual_spire_lower",
                [x, 29.4, -3.5], [2.55, 3.0, 6.5],
                "base", rotation=[0, 0, angle], pivot=[x + 1.25, 29.5, 0])
            builder.cube(
                "ritual_spires", f"{side}_ritual_spire_middle",
                [x + 0.2, 32.2, -3.15], [2.15, 2.7, 5.8],
                "cloth", rotation=[0, 0, angle], pivot=[x + 1.25, 32.2, 0])
            builder.cube(
                "ritual_spires", f"{side}_ritual_spire_tip",
                [x + 0.45, 34.7, -2.75], [1.65, 2.25, 5.0],
                "base", rotation=[0, 0, angle], pivot=[x + 1.25, 34.7, 0])

        builder.cube("robe_mantle", "upper_shoulder_mantle", [-5.1, 20.2, -2.75], [10.2, 3.2, 5.5], "base")
        builder.cube("robe_mantle", "lower_shoulder_mantle", [-4.7, 18.8, -2.62], [9.4, 2.1, 5.24], "cloth")
        builder.cube(
            "robe_mantle", "cross_wrap_right", [-4.2, 16.0, -2.7], [5.2, 1.4, 0.78],
            "cloth", rotation=[0, 0, -29], pivot=[0, 19, -2.4])
        builder.cube(
            "robe_mantle", "cross_wrap_left", [-1.0, 16.0, -2.76], [5.2, 1.4, 0.8],
            "light", rotation=[0, 0, 29], pivot=[0, 19, -2.4])
        builder.cube("utility_gear", "wide_ritual_sash", [-4.55, 11.6, -2.6], [9.1, 2.2, 5.2], "shadow")
        builder.cube("utility_gear", "bone_clasp", [-1.1, 11.2, -3.0], [2.2, 2.4, 0.7], "light")
        builder.cube("robe_skirt", "layered_skirt", [-4.45, 6.0, -2.35], [8.9, 6.0, 4.7], "base")
        builder.cube("robe_skirt", "ritual_apron", [-2.65, 4.4, -2.82], [5.3, 7.7, 0.72], "base")
        builder.cube("cloth_panels", "right_long_panel", [-4.1, 0.6, -2.72], [3.5, 10.8, 0.75], "cloth")
        builder.cube("cloth_panels", "left_long_panel", [0.6, 1.6, -2.74], [3.5, 9.8, 0.75], "light")
        builder.cube("cloth_panels", "rear_long_panel", [-3.2, 1.2, 2.1], [6.4, 10.0, 0.72], "shadow")
        for index, x in enumerate((-2.5, 0.0, 2.5)):
            builder.cube(
                "talisman_cords", f"ritual_cord_{index}", [x - 0.18, 14.0, -2.96],
                [0.36, 6.5 - abs(index - 1), 0.36], "accent")
            builder.cube(
                "talisman_cords", f"bone_talisman_{index}", [x - 0.42, 13.1, -3.08],
                [0.84, 1.2, 0.6], "light")
        for side, bone, x in (("right", "right_arm", -8.35), ("left", "left_arm", 3.85)):
            builder.cube(bone, f"{side}_forearm_wrap", [x, 12.0, -2.45], [4.5, 4.0, 4.9], "shadow")
        return

    builder.cube("body", "archer_fitted_tunic", [-3.7, 12, -2.0], [7.4, 12, 4.0], "dark")
    builder.cube("right_arm", "right_fitted_sleeve", [-7.7, 12, -1.9], [3.7, 12, 3.8], "cloth")
    builder.cube("left_arm", "left_fitted_sleeve", [4, 12, -1.9], [3.7, 12, 3.8], "cloth", mirror=True)
    builder.cube("right_leg", "right_archer_leg", [-3.85, 0, -1.95], [3.85, 12, 3.9], "shadow")
    builder.cube("left_leg", "left_archer_leg", [0, 0, -1.95], [3.85, 12, 3.9], "shadow", mirror=True)
    builder.cube("hood", "swept_headwrap", [-4.25, 29.1, -4.25], [8.5, 3.0, 8.5], "base")
    builder.cube(
        "hood", "angled_brow_wrap", [-4.2, 28.7, -4.45], [8.4, 1.15, 0.7],
        "accent", rotation=[0, 0, -7], pivot=[0, 29, -4])
    for index, (x, z, angle) in enumerate((
            (-3.7, -3.9, -15), (-2.0, -4.15, -8),
            (-0.2, -4.25, 2), (1.65, -4.05, 12))):
        builder.cube(
            "hair_locks", f"swept_crown_lock_{index}",
            [x, 30.2 + index % 2 * 0.25, z], [2.1, 1.35, 6.5],
            "dark", rotation=[-7, 0, angle], pivot=[x + 1.0, 30.5, 0])
    for index, x in enumerate((-3.4, -1.8, -0.2, 1.4, 3.0)):
        builder.cube(
            "hair_locks", f"hair_lock_{index}", [x, 23.0 + index % 2, 3.2],
            [1.0, 7.2 - index % 2, 1.1], "dark",
            rotation=[-4 - index, 0, -7 + index * 3], pivot=[x + 0.5, 28, 3.5])
    builder.cube("robe_mantle", "archer_chest_wrap", [-4.0, 16.0, -2.45], [8.0, 7.3, 4.9], "base")
    builder.cube(
        "robe_mantle", "diagonal_archer_wrap", [-4.2, 17.5, -2.82], [8.5, 1.35, 0.75],
        "light", rotation=[0, 0, -23], pivot=[0, 19, -2.5])
    builder.cube("utility_gear", "archer_belt", [-4.2, 11.7, -2.4], [8.4, 1.8, 4.8], "dark")
    builder.cube("archer_sash", "long_side_sash", [2.2, 4.0, -2.4], [2.1, 8.5, 4.8], "base")
    builder.cube("cloth_panels", "front_split_panel", [-3.3, 5.0, -2.58], [2.8, 7.0, 0.72], "cloth")
    builder.cube("cloth_panels", "rear_split_panel", [0.5, 6.0, 1.9], [2.8, 6.0, 0.72], "shadow")
    for side, bone, x in (("right", "right_arm", -7.95), ("left", "left_arm", 3.95)):
        builder.cube(bone, f"{side}_archer_bracer", [x, 11.7, -2.25], [4.0, 4.8, 4.5], "shadow")
    builder.cube(
        "quiver", "high_quiver", [2.3, 14.0, 1.6], [3.0, 10.0, 3.0],
        "shadow", rotation=[0, 0, -10], pivot=[3.5, 19, 2])
    builder.cube("quiver", "quiver_rim", [2.0, 22.7, 1.3], [3.6, 1.2, 3.6], "light")
    builder.cube(
        "quiver", "quiver_binding", [2.05, 18.0, 1.35], [3.5, 1.25, 3.5],
        "base", rotation=[0, 0, -10], pivot=[3.5, 19, 2])
    for index, x in enumerate((2.45, 3.25, 4.05, 4.85)):
        builder.cube("quiver", f"arrow_{index}", [x, 23.2, 2.15], [0.34, 4.3, 0.34], "accent")


def add_civilian_details(builder: ModelBuilder, variant: str) -> None:
    add_hair_cap(builder, "hair_or_cap")
    builder.cube("outerwear", "jacket", [-4.2, 14.0, -2.35], [8.4, 9.8, 4.7], "base", inflate=0.12)
    builder.cube("utility_belt", "utility_belt", [-4.4, 11.8, -2.45], [8.8, 1.8, 4.9], "dark")
    builder.cube("utility_belt", "belt_pouch", [2.6, 11.4, -2.85], [2.0, 2.3, 1.1], "accent")
    builder.cube("right_leg", "right_boot", [-4.1, 0, -2.2], [4, 4.5, 4.4], "shadow")
    builder.cube("left_leg", "left_boot", [0.1, 0, -2.2], [4, 4.5, 4.4], "shadow", mirror=True)
    if variant == "republic":
        builder.cube("outerwear", "republic_vest", [-3.5, 15.5, -2.7], [7, 7.2, 1.0], "light")
        builder.cube("outerwear", "right_lapel", [-3.3, 18.0, -2.95], [3.2, 1.0, 0.55], "accent",
                     rotation=[0, 0, -30], pivot=[0, 20, -2.5])
        builder.cube("outerwear", "left_lapel", [0.1, 18.0, -2.95], [3.2, 1.0, 0.55], "base",
                     rotation=[0, 0, 30], pivot=[0, 20, -2.5])
        builder.cube("outerwear", "comlink_badge", [2.2, 18.0, -3.02], [1.2, 1.7, 0.4], "accent")
    elif variant == "clansperson":
        builder.cube("helmet", "clan_work_helmet", [-4.1, 29.6, -4.1], [8.2, 2.4, 8.2], "base")
        builder.cube("chest_armor", "clan_shoulder", [-6.1, 18.2, -2.6], [3.8, 4.6, 5.2], "light")
        builder.cube("right_gauntlet", "right_work_bracer", [-8.25, 12.2, -2.3], [4.5, 4.8, 4.6], "accent")
        builder.cube("left_gauntlet", "left_work_bracer", [3.75, 12.2, -2.3], [4.5, 4.8, 4.6], "accent")
    elif variant == "hutt":
        builder.cube("outerwear", "merchant_sash", [-4.5, 14.0, -2.75], [9, 2.0, 5.5], "accent", rotation=[0, 0, -12], pivot=[0, 15, 0])
    elif variant == "nightsister":
        add_open_hood(builder, "civilian_hood")
        builder.cube("robe_skirt", "woven_apron", [-3, 8, -2.65], [6, 6, 1.0], "accent")
    elif variant == "smuggler":
        builder.cube("holster", "smuggler_holster", [3.4, 7.5, -2.55], [2.1, 4.0, 1.6], "shadow")


def add_dathomir_civilian(builder: ModelBuilder) -> None:
    """Build a broad, visibly unarmed matron/weaver silhouette."""
    add_dathomir_face(builder, tattoos=False)
    builder.cube("body", "weaver_underrobe", [-4.3, 12, -2.2], [8.6, 12, 4.4], "dark")
    builder.cube("right_arm", "right_weaver_sleeve", [-8.2, 12, -2.25], [4.2, 12, 4.5], "cloth")
    builder.cube("left_arm", "left_weaver_sleeve", [4, 12, -2.25], [4.2, 12, 4.5], "cloth", mirror=True)
    builder.cube("right_leg", "right_weaver_leg", [-4.0, 0, -2.15], [4.0, 12, 4.3], "shadow")
    builder.cube("left_leg", "left_weaver_leg", [0, 0, -2.15], [4.0, 12, 4.3], "shadow", mirror=True)

    builder.cube("hair", "braided_crown", [-4.4, 29.2, -4.2], [8.8, 3.0, 8.4], "shadow")
    for index, x in enumerate((-3.25, -1.25, 1.0, 3.0)):
        builder.cube(
            "hair", f"crown_braid_{index}", [x, 28.8, -4.55], [1.25, 2.2, 0.75],
            "light", rotation=[0, 0, -9 + index * 6], pivot=[x + 0.6, 29.3, -4])
    builder.cube("hood", "lowered_matron_hood", [-4.8, 22.0, 2.8], [9.6, 8.0, 1.6], "base")
    builder.cube("shawl", "upper_shawl", [-5.5, 20.2, -2.9], [11.0, 3.2, 5.8], "base")
    builder.cube("shawl", "middle_shawl", [-5.2, 18.2, -2.78], [10.4, 2.5, 5.56], "cloth")
    builder.cube("shawl", "lower_shawl", [-4.9, 16.5, -2.65], [9.8, 2.1, 5.3], "base")
    builder.cube("shawl", "shawl_clasp", [-0.8, 17.0, -3.05], [1.6, 2.0, 0.7], "light")
    builder.cube("outerwear", "draped_outer_robe", [-4.65, 9.0, -2.5], [9.3, 8.5, 5.0], "cloth")
    builder.cube("robe_skirt", "full_weaver_skirt", [-4.8, 0.3, -2.5], [9.6, 9.2, 5.0], "base")
    builder.cube("woven_apron", "patterned_apron", [-3.25, 2.0, -2.95], [6.5, 11.2, 1.0], "light")
    builder.cube("woven_apron", "apron_border", [-3.5, 1.6, -3.02], [7.0, 1.2, 1.1], "accent")
    builder.cube("utility_belt", "weaver_belt", [-4.65, 11.4, -2.6], [9.3, 1.8, 5.2], "shadow")
    builder.cube("utility_belt", "herb_pouch", [-4.9, 8.8, -2.8], [2.4, 3.0, 1.4], "shadow")
    builder.cube("satchel", "loom_satchel", [3.5, 7.0, 1.5], [3.2, 6.0, 2.4], "shadow")
    builder.cube(
        "satchel", "satchel_strap", [-3.8, 17.0, -2.8], [8.0, 0.8, 0.6],
        "light", rotation=[0, 0, -44], pivot=[0, 18, -2.5])
    builder.cube("satchel", "loom_shuttle", [4.1, 8.8, -1.3], [1.8, 0.8, 4.4], "accent")
    for side, bone, x in (("right", "right_arm", -8.4), ("left", "left_arm", 3.9)):
        builder.cube(bone, f"{side}_woven_cuff", [x, 11.8, -2.55], [4.5, 3.2, 5.1], "light")


def add_droid(builder: ModelBuilder, style: str, variant: str) -> None:
    if style == "droid_heavy":
        builder.cube("head", "sensor_head", [-4, 25, -4], [8, 7, 8], "base")
        builder.cube("head", "sensor_bar", [-3.5, 28.0, -4.6], [7, 1.2, 0.8], "accent")
        builder.cube("neck", "heavy_neck_piston", [-1.7, 22.8, -1.7], [3.4, 3.2, 3.4], "dark")
        builder.cube("body", "heavy_torso", [-5.3, 12.5, -3], [10.6, 11.2, 6], "base")
        builder.cube("torso_core", "reactor", [-2.8, 15.5, -3.6], [5.6, 5.5, 1.0], "accent")
        builder.cube("utility_pack", "rear_reactor_housing", [-3.2, 15.0, 2.8], [6.4, 6.5, 1.7], "shadow")
        for side, bone, x in (("right", "right_arm", -9.2), ("left", "left_arm", 3.2)):
            builder.cube(bone, f"{side}_heavy_upper_arm", [x, 17.0, -3], [6, 6.5, 6], "base")
            builder.cube(bone, f"{side}_shoulder_pivot", [x + 1.35, 21.4, -1.65], [3.3, 3.3, 3.3], "shadow")
            builder.cube(f"{side}_forearm", f"{side}_heavy_forearm", [x + 0.25, 11.5, -2.75], [5.5, 6.0, 5.5], "base")
            builder.cube(f"{side}_forearm", f"{side}_wrist", [x + 0.7, 10.5, -2.5], [4.6, 2.0, 5], "shadow")
        builder.cube("forearm_cannon", "cannon_shroud", [-9.35, 11.1, -4.2], [6.2, 5.8, 2.2], "dark")
        builder.cube("forearm_cannon", "cannon_barrel", [-7.9, 11.8, -8.4], [3.3, 3.3, 5.2], "shadow")
        builder.cube("forearm_cannon", "cannon_emitter", [-8.15, 12.0, -8.85], [3.8, 2.9, 0.65], "accent")
        for side, bone, x in (("right", "right_leg", -4.4), ("left", "left_leg", 0.4)):
            builder.cube(bone, f"{side}_heavy_thigh", [x, 6.0, -2.5], [4.4, 6.5, 5], "base")
            builder.cube(f"{side}_shin", f"{side}_heavy_shin", [x, 0.5, -2.5], [4.4, 5.7, 5], "base")
            builder.cube(f"{side}_foot", f"{side}_foot_plate", [x - 0.2, -0.2, -3.4], [4.8, 2.5, 6.8], "dark")
        return
    half_width = {"b1": 4.0, "commando": 4.6, "technician": 4.8}[variant]
    if variant == "commando":
        head_y = 25.1
        builder.cube("head", "droid_head", [-3.0, head_y, -5.4], [6.0, 5.0, 9.6], "base")
        builder.cube("head", "optic_bar", [-2.55, head_y + 2.0, -5.95], [5.1, 0.9, 0.8], "accent")
        builder.cube("commando_head_profile", "angular_brow", [-3.35, 28.6, -5.2], [6.7, 1.0, 7.8], "shadow",
                     rotation=[-5, 0, 0], pivot=[0, 28.5, 0])
        builder.cube("commando_head_profile", "rear_wedge", [-2.5, 25.4, 3.2], [5.0, 3.5, 2.3], "dark")
    else:
        head_y = 25.2
        builder.cube("head", "droid_head", [-3.8, head_y, -4.4], [7.6, 5.5, 8.8], "base")
        builder.cube("head", "optic_bar", [-3.2, head_y + 2.2, -5.0], [6.4, 1.0, 0.8], "accent")
    builder.cube("neck", "neck_piston", [-1.2, 23.2, -1.2], [2.4, 3.0, 2.4], "dark")
    builder.cube("body", "droid_chest", [-3.5, 14, -2.2], [7, 9.5, 4.4], "base")
    builder.cube("torso_core", "droid_core", [-2, 16, -2.8], [4, 4.5, 1.0], "accent")
    builder.cube("body", "droid_waist", [-2, 11.5, -1.6], [4, 2.5, 3.2], "dark")
    arm_width = 2.2 if variant == "b1" else 2.6
    right_x = -half_width
    left_x = half_width - arm_width
    for side, bone, x in (("right", "right_arm", right_x), ("left", "left_arm", left_x)):
        builder.cube(bone, f"{side}_upper_arm", [x, 17.3, -1.5], [arm_width, 6.2, 3], "base")
        builder.cube(bone, f"{side}_shoulder_joint", [x + 0.25, 21.7, -1.25], [arm_width - 0.5, 2.0, 2.5], "dark")
        builder.cube(f"{side}_forearm", f"{side}_forearm_shell", [x + 0.2, 11.5, -1.35], [arm_width - 0.4, 5.8, 2.7], "shadow")
    for side, bone, x in (("right", "right_leg", -3.4), ("left", "left_leg", 0.9)):
        builder.cube(bone, f"{side}_thigh_rod", [x, 6.0, -1.4], [2.5, 6, 2.8], "base")
        builder.cube(bone, f"{side}_knee_joint", [x - 0.15, 5.1, -1.55], [2.8, 2.0, 3.1], "dark")
        builder.cube(f"{side}_shin", f"{side}_shin_rod", [x + 0.2, 0, -1.2], [2.1, 6, 2.4], "shadow")
        builder.cube(f"{side}_foot", f"{side}_droid_foot", [x - 0.2, -0.2, -2.7], [3.0, 1.8, 5.4], "dark")
    if variant == "commando":
        builder.cube("commando_head_profile", "commando_crest", [-0.85, 29.8, -3.6], [1.7, 1.1, 6.0], "accent")
        builder.cube("body", "commando_plate", [-3.8, 17, -2.7], [7.6, 5.5, 1.0], "light")
    elif variant == "technician":
        builder.cube("utility_pack", "tool_pack", [-3.0, 14.0, 2.0], [6, 7, 2.6], "accent")
        builder.cube("headgear", "technician_antenna", [3.4, 27.2, -0.5], [0.8, 3.0, 1.0], "accent")
        builder.cube("utility_belt", "technician_tool_belt", [-3.2, 11.4, -1.9], [6.4, 1.4, 3.8], "shadow")


def add_brute(builder: ModelBuilder, variant: str) -> None:
    builder.cube("head", "brute_head", [-4.5, 23.7, -4.2], [9, 8.5, 8.4], "skin")
    add_human_face(builder)
    builder.cube("head", "brow", [-4.0, 28.0, -4.8], [8, 1.5, 0.9], "shadow")
    builder.cube("body", "brute_torso", [-5, 11.5, -2.8], [10, 12.5, 5.6], "base")
    arm_width = 4.7 if variant == "enforcer" else 6
    arm_right = -6.7 if variant == "enforcer" else -9.4
    arm_left = 2.0 if variant == "enforcer" else 3.4
    for side, bone, x in (("right", "right_arm", arm_right), ("left", "left_arm", arm_left)):
        builder.cube(bone, f"{side}_brute_arm", [x, 11, -2.8], [arm_width, 13, 5.6], "skin")
        builder.cube(f"{side}_bracer", f"{side}_bracer_shell", [x + 0.3, 11.3, -3.0], [arm_width - 0.6, 4.0, 6], "shadow")
    for side, bone, x in (("right", "right_leg", -4.5), ("left", "left_leg", 0.2)):
        builder.cube(bone, f"{side}_brute_leg", [x, 0, -2.5], [4.5, 12, 5], "cloth")
        builder.cube(bone, f"{side}_boot", [x - 0.2, -0.2, -3.0], [4.9, 4.2, 6], "dark")
    builder.cube("chest_armor", "chest_harness", [-5.2, 17.3, -3.2], [10.4, 2.0, 6.4], "accent")
    builder.cube("neck", "neck_wrap", [-3.7, 22.8, -3.0], [7.4, 1.5, 6.0], "shadow")
    builder.cube("harness", "harness_buckle", [-1.35, 16.7, -3.48], [2.7, 2.2, 0.6], "light")
    if variant == "enforcer":
        builder.cube("shoulder_armor", "right_shoulder_plate", [-6.65, 20.0, -3.05], [3.3, 3.6, 6.1], "base")
        builder.cube("shoulder_armor", "left_shoulder_plate", [3.35, 20.0, -3.05], [3.3, 3.6, 6.1], "base")
        builder.cube("utility_belt", "enforcer_utility_belt", [-5.0, 10.9, -3.0], [10, 1.8, 6], "dark")
    if variant == "nightbrother":
        for index, x in enumerate((-3.6, -1.35, 1.05, 3.0)):
            builder.cube("horns", f"horn_{index}", [x, 31.0, -1.35], [1.15, 2.65, 1.7], "light",
                         rotation=[0, 0, -10 if x < 0 else 10], pivot=[x + 0.55, 31.0, -0.5])
        builder.cube("face_tattoo_vertical", "face_tattoo", [-0.45, 24.8, -4.72], [0.9, 6.2, 0.38], "dark")
        builder.cube("body", "chest_tattoo_right", [-4.9, 15.5, -3.08], [5.0, 1.0, 0.45], "dark",
                     rotation=[0, 0, -24], pivot=[0, 18, -2.8])
        builder.cube("body", "chest_tattoo_left", [-0.1, 15.5, -3.08], [5.0, 1.0, 0.45], "dark",
                     rotation=[0, 0, 24], pivot=[0, 18, -2.8])


def add_dathomir_nightbrother(builder: ModelBuilder) -> None:
    """Build the concept-board Nightbrother with a broad tattooed silhouette."""
    builder.cube("head", "nightbrother_head", [-4.6, 23.5, -4.25], [9.2, 8.8, 8.5], "skin")
    builder.cube("head", "heavy_brow", [-4.2, 27.9, -4.8], [8.4, 1.5, 0.9], "dark")
    builder.cube("head", "right_eye", [-3.0, 27.6, -4.72], [1.5, 0.8, 0.45], "light")
    builder.cube("head", "left_eye", [1.5, 27.6, -4.72], [1.5, 0.8, 0.45], "light")
    builder.cube("head", "broad_nose", [-0.8, 25.8, -4.65], [1.6, 2.0, 0.6], "shadow")
    builder.cube("head", "jaw_mark", [-2.2, 24.5, -4.62], [4.4, 0.7, 0.45], "dark")
    builder.cube("body", "nightbrother_torso", [-5.8, 11.5, -3.0], [11.6, 12.7, 6.0], "skin")
    builder.cube("right_arm", "right_massive_arm", [-10.1, 10.6, -3.1], [6.3, 13.6, 6.2], "skin")
    builder.cube("left_arm", "left_massive_arm", [3.8, 10.6, -3.1], [6.3, 13.6, 6.2], "skin", mirror=True)
    builder.cube("right_leg", "right_powerful_leg", [-4.8, 0, -2.7], [4.8, 12, 5.4], "base")
    builder.cube("left_leg", "left_powerful_leg", [0, 0, -2.7], [4.8, 12, 5.4], "base", mirror=True)
    builder.cube("right_leg", "right_grounded_boot", [-5.0, -0.2, -3.35], [5.2, 4.4, 6.7], "dark")
    builder.cube("left_leg", "left_grounded_boot", [-0.2, -0.2, -3.35], [5.2, 4.4, 6.7], "dark", mirror=True)

    horn_positions = (-4.0, -2.1, 0.45, 2.65)
    for index, x in enumerate(horn_positions):
        outward = -13 if x < 0 else 13
        builder.cube(
            "horns", f"crown_horn_{index}", [x, 31.0, -1.5], [1.35, 3.4 + index % 2, 1.9],
            "light", rotation=[-5, 0, outward], pivot=[x + 0.65, 31.0, -0.55])
        builder.cube(
            "horns", f"horn_tip_{index}", [x + 0.2, 33.9 + index % 2, -1.25], [0.95, 1.8, 1.4],
            "light", rotation=[-8, 0, outward], pivot=[x + 0.65, 34.0, -0.55])
    builder.cube("head", "right_tusk", [-2.5, 24.7, -4.75], [0.7, 1.4, 0.55], "light")
    builder.cube("head", "left_tusk", [1.8, 24.7, -4.75], [0.7, 1.4, 0.55], "light")

    builder.cube("neck", "bone_necklace", [-4.2, 21.7, -3.35], [8.4, 2.0, 6.7], "shadow")
    builder.cube(
        "harness", "diagonal_harness_right", [-5.2, 17.2, -3.35], [10.4, 1.6, 0.8],
        "dark", rotation=[0, 0, -35], pivot=[0, 19, -3])
    builder.cube(
        "harness", "diagonal_harness_left", [-5.2, 17.2, -3.42], [10.4, 1.3, 0.75],
        "light", rotation=[0, 0, 35], pivot=[0, 19, -3])
    builder.cube("shoulder_harness", "lower_right_pauldron", [-10.4, 19.0, -3.5], [7.2, 2.0, 7.0], "shadow")
    builder.cube("shoulder_harness", "middle_right_pauldron", [-9.9, 20.8, -3.25], [6.2, 1.7, 6.5], "dark")
    builder.cube("shoulder_harness", "upper_right_pauldron", [-9.25, 22.2, -2.9], [5.0, 1.5, 5.8], "shadow")
    builder.cube(
        "shoulder_harness", "pauldron_spike", [-8.6, 23.0, -1.2], [1.5, 3.2, 2.2],
        "light", rotation=[0, 0, -18], pivot=[-7.8, 23, 0])
    builder.cube("chest_armor", "bone_harness_clasp", [-1.3, 17.0, -3.55], [2.6, 2.8, 0.7], "light")
    for index, x in enumerate((-2.0, 0.0, 2.0)):
        builder.cube(
            "chest_armor", f"chest_bone_trophy_{index}",
            [x - 0.35, 14.1 - abs(index - 1) * 0.45, -3.55],
            [0.7, 2.5 + (index == 1) * 0.8, 0.6], "light")
    for side, bone, x in (("right", "right_bracer", -10.35), ("left", "left_bracer", 3.75)):
        builder.cube(bone, f"{side}_layered_bracer", [x, 10.8, -3.4], [6.6, 4.8, 6.8], "shadow")
        builder.cube(bone, f"{side}_bracer_wrap", [x + 0.35, 15.1, -3.25], [5.9, 1.3, 6.5], "light")
    builder.cube("waist_cloth", "wide_war_belt", [-5.6, 10.8, -3.0], [11.2, 2.0, 6.0], "shadow")
    builder.cube("waist_cloth", "front_war_panel", [-3.2, 3.2, -3.15], [6.4, 8.5, 1.0], "cloth")
    builder.cube("waist_cloth", "rear_war_panel", [-3.5, 4.0, 2.35], [7.0, 7.5, 0.9], "base")
    for index, x in enumerate((-2.2, 0.0, 2.2)):
        builder.cube("waist_cloth", f"belt_talisman_{index}", [x - 0.4, 9.7, -3.5], [0.8, 1.8, 0.7], "light")


def build_recruit(design: RecruitDesign) -> None:
    builder = ModelBuilder(design.id, design.palette, texel_density=ENTITY_TEXEL_DENSITY)
    humanoid_bones(builder)
    recruit_child_bones(builder, design)
    if design.style == "plate":
        basic_humanoid(builder, "dark", "dark")
        add_plate_details(builder, design.variant)
    elif design.style == "robe":
        if design.variant in ("acolyte", "archer"):
            add_dathomir_sister(builder, design.variant)
        else:
            basic_humanoid(builder)
            add_human_face(builder)
            add_robe_details(builder, design.variant)
    elif design.style == "civilian":
        if design.variant == "nightsister":
            add_dathomir_civilian(builder)
        else:
            basic_humanoid(builder)
            add_human_face(builder)
            add_civilian_details(builder, design.variant)
    elif design.style.startswith("droid"):
        add_droid(builder, design.style, design.variant)
    elif design.style == "brute":
        if design.variant == "nightbrother":
            add_dathomir_nightbrother(builder)
        else:
            add_brute(builder, design.variant)
    else:
        raise ValueError(f"Unknown recruit style: {design.style}")
    builder.write(
        ENTITY_MODELS / f"{design.id}.geo.json",
        ENTITY_TEXTURES / f"{design.id}.png",
    )
    write_recruit_animation(design, builder)
    write_spawn_egg(design)


def write_recruit_animation(design: RecruitDesign, builder: ModelBuilder) -> None:
    """Write a distinct locomotion set that exercises the authored child rig."""
    seed = int(hashlib.sha256(design.id.encode("utf-8")).hexdigest()[:4], 16)
    idle_tilt = 1.0 + (seed % 5) * 0.35
    if design.style.startswith("droid"):
        idle_length = 1.6
    elif design.style == "robe":
        idle_length = 2.4
    elif design.style == "civilian":
        idle_length = 2.8
    else:
        idle_length = 2.0
    if design.variant == "acolyte":
        idle_length = 3.2
    elif design.variant == "archer":
        idle_length = 2.0
    elif design.variant == "nightsister":
        idle_length = 3.4
    elif design.variant == "nightbrother":
        idle_length = 1.8
    idle_midpoint = idle_length / 2
    stride = 22 + seed % 9
    attack = 82 + seed % 15
    child_idle: dict[str, dict] = {}
    child_walk: dict[str, dict] = {}
    child_attack: dict[str, dict] = {}

    if design.style.startswith("droid"):
        child_idle = {
            "neck": {"rotation": {"0.0": [0, -4, 0], "0.8": [0, 5, 0], "1.6": [0, -4, 0]}},
            "torso_core": {"scale": {"0.0": [1, 1, 1], "0.8": [1.02, 1.02, 1.02], "1.6": [1, 1, 1]}},
        }
        child_walk = {
            "right_forearm": {"rotation": {"0.0": [12, 0, -3], "0.5": [-18, 0, 3], "1.0": [12, 0, -3]}},
            "left_forearm": {"rotation": {"0.0": [-18, 0, 3], "0.5": [12, 0, -3], "1.0": [-18, 0, 3]}},
            "right_shin": {"rotation": {"0.0": [14, 0, 0], "0.5": [-8, 0, 0], "1.0": [14, 0, 0]}},
            "left_shin": {"rotation": {"0.0": [-8, 0, 0], "0.5": [14, 0, 0], "1.0": [-8, 0, 0]}},
        }
        child_attack = {
            "right_forearm": {"rotation": {"0.0": [0, 0, 0], "0.18": [-55, 0, 8], "0.45": [0, 0, 0]}},
            "neck": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 8, 0], "0.45": [0, 0, 0]}},
        }
    elif design.style == "plate":
        child = "rangefinder" if "rangefinder" in builder.bones else "helmet"
        child_idle = {
            child: {"rotation": {"0.0": [0, 0, -1], "1.0": [0, 0, 1], "2.0": [0, 0, -1]}},
            "chest_armor": {"rotation": {"0.0": [0, 0, 0], "1.0": [idle_tilt, 0, 0], "2.0": [0, 0, 0]}},
        }
        child_walk = {
            "right_gauntlet": {"rotation": {"0.0": [5, 0, 0], "0.5": [-6, 0, 0], "1.0": [5, 0, 0]}},
            "left_gauntlet": {"rotation": {"0.0": [-6, 0, 0], "0.5": [5, 0, 0], "1.0": [-6, 0, 0]}},
            "right_boot": {"rotation": {"0.0": [-4, 0, 0], "0.5": [8, 0, 0], "1.0": [-4, 0, 0]}},
            "left_boot": {"rotation": {"0.0": [8, 0, 0], "0.5": [-4, 0, 0], "1.0": [8, 0, 0]}},
        }
        child_attack = {
            "right_gauntlet": {"rotation": {"0.0": [0, 0, 0], "0.18": [-22, 0, 8], "0.45": [0, 0, 0]}},
            "chest_armor": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 9, 0], "0.45": [0, 0, 0]}},
        }
        if "kama" in builder.bones:
            child_walk["kama"] = {"rotation": {"0.0": [3, 0, 0], "0.5": [-3, 0, 0], "1.0": [3, 0, 0]}}
    elif design.style == "robe":
        child_idle = {"hood": {"rotation": {"0.0": [0, -2, 0], "1.2": [0, 2, 0], "2.4": [0, -2, 0]}}}
        child_walk = {"robe_skirt": {"rotation": {"0.0": [4, 0, 0], "0.5": [-4, 0, 0], "1.0": [4, 0, 0]}}}
        child_attack = {"robe_mantle": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 10, 0], "0.45": [0, 0, 0]}}}
    elif design.style == "civilian":
        child_idle = {"hair": {"rotation": {"0.0": [0, -1, 0], "1.4": [0, 2, 0], "2.8": [0, -1, 0]}}}
        child_walk = {"outerwear": {"rotation": {"0.0": [1, 0, -1], "0.5": [-1, 0, 1], "1.0": [1, 0, -1]}}}
        attack_child = "holster" if "holster" in builder.bones else "utility_belt"
        child_attack = {attack_child: {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 7, 0], "0.45": [0, 0, 0]}}}
    else:
        child_idle = {"neck": {"rotation": {"0.0": [0, -2, 0], "1.0": [0, 2, 0], "2.0": [0, -2, 0]}}}
        child_walk = {"harness": {"rotation": {"0.0": [1, 0, -1], "0.5": [-1, 0, 1], "1.0": [1, 0, -1]}}}
        child_attack = {"chest_armor": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 14, 0], "0.45": [0, 0, 0]}}}

    if design.variant == "acolyte":
        child_idle = {
            "hood": {"rotation": {"0.0": [0, -2, 0], "1.6": [0, 2, 0], "3.2": [0, -2, 0]}},
            "ritual_spires": {"rotation": {"0.0": [0, 0, -1], "1.6": [0, 0, 1], "3.2": [0, 0, -1]}},
            "talisman_cords": {"rotation": {"0.0": [-1, 0, -2], "1.6": [2, 0, 2], "3.2": [-1, 0, -2]}},
        }
        child_walk = {
            "robe_skirt": {"rotation": {"0.0": [5, 0, 0], "0.5": [-5, 0, 0], "1.0": [5, 0, 0]}},
            "cloth_panels": {"rotation": {"0.0": [-7, 0, 2], "0.5": [7, 0, -2], "1.0": [-7, 0, 2]}},
            "talisman_cords": {"rotation": {"0.0": [-8, 0, 3], "0.5": [10, 0, -3], "1.0": [-8, 0, 3]}},
        }
        child_attack = {
            "robe_mantle": {"rotation": {"0.0": [0, 0, 0], "0.18": [-5, 15, 0], "0.45": [0, 0, 0]}},
            "left_arm": {"rotation": {"0.0": [-8, 0, 0], "0.18": [-72, -24, -18], "0.45": [-8, 0, 0]}},
            "talisman_cords": {"rotation": {"0.0": [0, 0, 0], "0.18": [-18, 0, 12], "0.45": [0, 0, 0]}},
        }
    elif design.variant == "archer":
        child_idle = {
            "hair_locks": {"rotation": {"0.0": [0, -2, -1], "1.0": [0, 2, 1], "2.0": [0, -2, -1]}},
            "quiver": {"rotation": {"0.0": [0, 0, -1], "1.0": [0, 0, 1], "2.0": [0, 0, -1]}},
        }
        child_walk = {
            "hair_locks": {"rotation": {"0.0": [-7, 0, 2], "0.5": [8, 0, -2], "1.0": [-7, 0, 2]}},
            "archer_sash": {"rotation": {"0.0": [-8, 0, 3], "0.5": [8, 0, -3], "1.0": [-8, 0, 3]}},
            "cloth_panels": {"rotation": {"0.0": [-5, 0, 0], "0.5": [6, 0, 0], "1.0": [-5, 0, 0]}},
        }
        child_attack = {
            "left_arm": {"rotation": {"0.0": [-8, 0, 0], "0.18": [-88, 0, -4], "0.45": [-8, 0, 0]}},
            "right_arm": {"rotation": {"0.0": [-8, 0, 0], "0.18": [-64, -46, 12], "0.45": [-8, 0, 0]}},
            "quiver": {"rotation": {"0.0": [0, 0, 0], "0.18": [5, 0, -4], "0.45": [0, 0, 0]}},
        }
    elif design.variant == "nightsister":
        child_idle = {
            "hair": {"rotation": {"0.0": [0, -1, 0], "1.7": [0, 2, 0], "3.4": [0, -1, 0]}},
            "shawl": {"rotation": {"0.0": [0, 0, -1], "1.7": [1.5, 0, 1], "3.4": [0, 0, -1]}},
            "satchel": {"rotation": {"0.0": [0, 0, -1], "1.7": [0, 0, 1], "3.4": [0, 0, -1]}},
        }
        child_walk = {
            "shawl": {"rotation": {"0.0": [2, 0, -2], "0.5": [-2, 0, 2], "1.0": [2, 0, -2]}},
            "woven_apron": {"rotation": {"0.0": [-4, 0, 0], "0.5": [5, 0, 0], "1.0": [-4, 0, 0]}},
            "satchel": {"rotation": {"0.0": [-7, 0, 4], "0.5": [8, 0, -4], "1.0": [-7, 0, 4]}},
        }
        child_attack = {
            "right_arm": {"rotation": {"0.0": [-8, 0, 0], "0.18": [-48, 12, 25], "0.45": [-8, 0, 0]}},
            "left_arm": {"rotation": {"0.0": [-8, 0, 0], "0.18": [-48, -12, -25], "0.45": [-8, 0, 0]}},
            "shawl": {"rotation": {"0.0": [0, 0, 0], "0.18": [5, 0, 0], "0.45": [0, 0, 0]}},
        }
    elif design.variant == "nightbrother":
        child_idle = {
            "neck": {"rotation": {"0.0": [0, -3, 0], "0.9": [0, 3, 0], "1.8": [0, -3, 0]}},
            "shoulder_harness": {"rotation": {"0.0": [0, 0, -1], "0.9": [1.5, 0, 1], "1.8": [0, 0, -1]}},
        }
        child_walk = {
            "harness": {"rotation": {"0.0": [3, 0, -2], "0.5": [-3, 0, 2], "1.0": [3, 0, -2]}},
            "waist_cloth": {"rotation": {"0.0": [-6, 0, 0], "0.5": [7, 0, 0], "1.0": [-6, 0, 0]}},
        }
        child_attack = {
            "chest_armor": {"rotation": {"0.0": [0, 0, 0], "0.18": [-8, 22, 0], "0.45": [0, 0, 0]}},
            "shoulder_harness": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 0, -10], "0.45": [0, 0, 0]}},
            "waist_cloth": {"rotation": {"0.0": [0, 0, 0], "0.18": [-12, 0, 0], "0.45": [0, 0, 0]}},
        }

    walk_bones = {
        "right_arm": {"rotation": {"0.0": [stride, 0, 0], "0.5": [-stride, 0, 0], "1.0": [stride, 0, 0]}},
        "left_arm": {"rotation": {"0.0": [-stride, 0, 0], "0.5": [stride, 0, 0], "1.0": [-stride, 0, 0]}},
        "right_leg": {"rotation": {"0.0": [-stride, 0, 0], "0.5": [stride, 0, 0], "1.0": [-stride, 0, 0]}},
        "left_leg": {"rotation": {"0.0": [stride, 0, 0], "0.5": [-stride, 0, 0], "1.0": [stride, 0, 0]}},
        **child_walk,
    }
    animations = {
        "format_version": "1.8.0",
        "animations": {
            "misc.idle": {"loop": True, "animation_length": idle_length, "bones": {
                "head": {"rotation": {
                    "0.0": [0, -2, 0],
                    f"{idle_midpoint:.1f}": [0, 2, 0],
                    f"{idle_length:.1f}": [0, -2, 0],
                }},
                **child_idle,
            }},
            "move.walk": {"loop": True, "animation_length": 1.0, "bones": walk_bones},
            "move.run": {"loop": True, "animation_length": 0.65, "bones": {
                key: {"rotation": {"0.0": [value * 1.45, 0, 0], "0.325": [-value * 1.45, 0, 0], "0.65": [value * 1.45, 0, 0]}}
                for key, value in (("right_arm", stride), ("left_arm", -stride), ("right_leg", -stride), ("left_leg", stride))
            }},
            "attack.swing": {"animation_length": 0.45, "bones": {
                "right_arm": {"rotation": {"0.0": [-12, 0, 0], "0.18": [-attack, 0, 12], "0.45": [-8, 0, 0]}},
                "body": {"rotation": {"0.0": [0, 0, 0], "0.18": [0, 12, 0], "0.45": [0, 0, 0]}},
                **child_attack,
            }},
        },
    }
    path = ENTITY_ANIMATIONS / f"{design.id}.animation.json"
    path.write_text(json.dumps(animations, indent=2) + "\n", encoding="utf-8")


def armor_bones(builder: ModelBuilder) -> None:
    # GeckoLib's armor template uses non-rendering biped anchors. The armor
    # bones must be children of these anchors so Bedrock-space cube origins
    # resolve around the vanilla player part instead of around the model root.
    builder.bone("bipedHead", [0, 24, 0])
    builder.bone("armorHead", [0, 24, 0], "bipedHead")
    builder.bone("bipedBody", [0, 24, 0])
    builder.bone("armorBody", [0, 24, 0], "bipedBody")
    builder.bone("bipedRightArm", [-5, 22, 0])
    builder.bone("armorRightArm", [-5, 22, 0], "bipedRightArm")
    builder.bone("bipedLeftArm", [5, 22, 0])
    builder.bone("armorLeftArm", [5, 22, 0], "bipedLeftArm")
    builder.bone("bipedRightLeg", [-1.9, 12, 0])
    builder.bone("armorRightLeg", [-1.9, 12, 0], "bipedRightLeg")
    builder.bone("armorRightBoot", [-1.9, 12, 0], "bipedRightLeg")
    builder.bone("bipedLeftLeg", [1.9, 12, 0])
    builder.bone("armorLeftLeg", [1.9, 12, 0], "bipedLeftLeg")
    builder.bone("armorLeftBoot", [1.9, 12, 0], "bipedLeftLeg")


def add_common_armor(
        builder: ModelBuilder,
        primary_plate: str,
        secondary_plate: str,
) -> None:
    """Author a segmented suit around the exact GeckoLib humanoid armor anchors."""
    builder.cube("armorBody", "torso_undersuit", [-4, 12, -2], [8, 12, 4], "dark", inflate=0.2)
    builder.cube("armorBody", "collar", [-4.35, 21.5, -2.45], [8.7, 1.8, 4.9], primary_plate)
    builder.cube(
        "armorBody", "right_pectoral", [-4.25, 17.7, -2.9], [3.95, 4.1, 1.05],
        primary_plate, rotation=[0, 0, -5], pivot=[-2.15, 20.0, -2.5])
    builder.cube(
        "armorBody", "left_pectoral", [0.3, 17.7, -2.9], [3.95, 4.1, 1.05],
        primary_plate, rotation=[0, 0, 5], pivot=[2.15, 20.0, -2.5])
    builder.cube("armorBody", "sternum_insert", [-0.65, 17.2, -3.02], [1.3, 4.6, 1.1], "accent")
    builder.cube("armorBody", "upper_abdomen", [-3.5, 15.0, -2.75], [7, 2.4, 0.9], secondary_plate)
    builder.cube("armorBody", "lower_abdomen", [-3.15, 12.9, -2.65], [6.3, 1.9, 0.8], "shadow")
    builder.cube("armorBody", "backplate", [-4.2, 14.2, 1.85], [8.4, 7.9, 1.15], primary_plate)
    builder.cube("armorBody", "back_spine", [-0.7, 14.8, 2.9], [1.4, 6.4, 0.65], "accent")
    builder.cube("armorBody", "utility_belt", [-4.55, 11.35, -2.55], [9.1, 1.75, 5.1], "shadow")
    builder.cube("armorBody", "belt_buckle", [-1.15, 11.15, -2.92], [2.3, 2.0, 0.65], "accent")
    builder.cube("armorBody", "right_belt_pouch", [-4.7, 11.1, -2.9], [2.05, 2.15, 1.0], secondary_plate)
    builder.cube("armorBody", "left_belt_pouch", [2.65, 11.1, -2.9], [2.05, 2.15, 1.0], secondary_plate)
    builder.cube("armorBody", "codpiece", [-1.8, 9.25, -2.68], [3.6, 2.2, 0.85], secondary_plate)

    for side, bone, x, direction in (
            ("right", "armorRightArm", -8.0, -1),
            ("left", "armorLeftArm", 4.0, 1)):
        builder.cube(bone, f"{side}_arm_undersuit", [x, 12, -2], [4, 12, 4], "dark", inflate=0.2)
        builder.cube(
            bone, f"{side}_shoulder_bell", [x - 0.55, 18.8, -2.65], [5.1, 4.5, 5.3],
            primary_plate, rotation=[0, 0, direction * 5], pivot=[x + 2.0, 21.2, 0])
        builder.cube(bone, f"{side}_bicep_plate", [x - 0.1, 16.8, -2.45], [4.2, 2.2, 4.9], secondary_plate)
        builder.cube(bone, f"{side}_elbow_guard", [x - 0.28, 15.0, -2.92], [4.55, 2.0, 1.05], "accent")
        builder.cube(bone, f"{side}_forearm_bracer", [x - 0.22, 12.15, -2.52], [4.45, 3.25, 5.05], primary_plate)
        builder.cube(bone, f"{side}_wrist_cuff", [x - 0.1, 11.75, -2.3], [4.2, 1.15, 4.6], "shadow")

    for side, leg_bone, boot_bone, x in (
            ("right", "armorRightLeg", "armorRightBoot", -4.0),
            ("left", "armorLeftLeg", "armorLeftBoot", 0.0)):
        builder.cube(leg_bone, f"{side}_leg_undersuit", [x, 0, -2], [4, 12, 4], "dark", inflate=0.18)
        builder.cube(leg_bone, f"{side}_thigh_shell", [x - 0.12, 6.3, -2.42], [4.25, 5.2, 4.85], primary_plate)
        builder.cube(leg_bone, f"{side}_thigh_front", [x + 0.35, 7.0, -2.92], [3.3, 3.8, 0.65], secondary_plate)
        builder.cube(leg_bone, f"{side}_knee_guard", [x - 0.28, 4.1, -2.95], [4.55, 2.3, 1.2], "accent")
        builder.cube(boot_bone, f"{side}_shin_guard", [x - 0.15, 0.5, -2.55], [4.3, 3.8, 5.1], primary_plate)
        builder.cube(boot_bone, f"{side}_boot_shell", [x - 0.3, -0.05, -2.85], [4.6, 2.0, 5.7], secondary_plate)
        builder.cube(boot_bone, f"{side}_boot_toe", [x - 0.38, -0.35, -3.35], [4.75, 1.3, 6.7], "shadow")


def add_nightsister_cloth_base(builder: ModelBuilder) -> None:
    """Build wearable Dathomirian layers as cloth and wraps, never as rigid plate."""
    builder.cube("armorBody", "wrapped_tunic", [-4.15, 12, -2.2], [8.3, 12, 4.4], "dark", inflate=0.16)
    builder.cube("armorBody", "upper_mantle", [-4.6, 19.1, -2.6], [9.2, 3.8, 5.2], "cloth")
    builder.cube("armorBody", "cross_wrap_right", [-4.2, 16.4, -2.78], [5.2, 1.35, 0.72], "base",
                 rotation=[0, 0, -27], pivot=[0, 19, -2.5])
    builder.cube("armorBody", "cross_wrap_left", [-1.0, 16.4, -2.82], [5.2, 1.35, 0.74], "light",
                 rotation=[0, 0, 27], pivot=[0, 19, -2.5])
    builder.cube("armorBody", "wide_sash", [-4.55, 11.3, -2.52], [9.1, 2.2, 5.04], "shadow")
    builder.cube("armorBody", "sash_clasp", [-1.0, 11.0, -2.9], [2, 2.3, 0.65], "accent")
    for side, bone, x in (("right", "armorRightArm", -8.0), ("left", "armorLeftArm", 4.0)):
        builder.cube(bone, f"{side}_cloth_sleeve", [x, 12, -2], [4, 12, 4], "cloth", inflate=0.14)
        builder.cube(bone, f"{side}_upper_wrap", [x - 0.2, 17.1, -2.3], [4.4, 2.0, 4.6], "base")
        builder.cube(bone, f"{side}_forearm_wrap", [x - 0.2, 12.2, -2.35], [4.4, 3.8, 4.7], "light")
    for side, leg_bone, boot_bone, x in (
            ("right", "armorRightLeg", "armorRightBoot", -4.0),
            ("left", "armorLeftLeg", "armorLeftBoot", 0.0)):
        builder.cube(leg_bone, f"{side}_legging", [x, 0, -2], [4, 12, 4], "dark", inflate=0.12)
        builder.cube(boot_bone, f"{side}_wrapped_boot", [x - 0.18, -0.15, -2.45], [4.35, 5.1, 4.9], "shadow")
        builder.cube(boot_bone, f"{side}_boot_wrap", [x - 0.28, 2.2, -2.62], [4.55, 1.2, 5.25], "base")


def add_republic_helmet(builder: ModelBuilder) -> None:
    """Late-war Phase II helmet with compact cheek recesses and blue unit marks."""
    builder.cube("armorHead", "helmet_shell", [-4, 24, -4], [8, 8, 8], "light", inflate=0.42)
    builder.cube("armorHead", "helmet_crown", [-3.55, 31.45, -3.55], [7.1, 1.25, 7.1], "light")
    builder.cube("armorHead", "helmet_fin", [-0.65, 31.8, -3.2], [1.3, 1.25, 6.4], "accent")
    builder.cube("armorHead", "helmet_brow", [-3.8, 28.7, -4.72], [7.6, 0.75, 0.75], "light")
    builder.cube("armorHead", "visor_bar", [-3.45, 27.75, -4.82], [6.9, 1.05, 0.72], "dark")
    builder.cube("armorHead", "visor_nose", [-0.58, 25.95, -4.88], [1.16, 1.9, 0.78], "dark")
    builder.cube(
        "armorHead", "right_cheek_plate", [-3.72, 24.75, -4.9], [2.72, 2.8, 0.72],
        "light", rotation=[0, 0, 8], pivot=[-2.2, 26.0, -4.6])
    builder.cube(
        "armorHead", "left_cheek_plate", [1.0, 24.75, -4.9], [2.72, 2.8, 0.72],
        "light", rotation=[0, 0, -8], pivot=[2.2, 26.0, -4.6])
    builder.cube("armorHead", "mouth_grille", [-2.25, 24.25, -5.0], [4.5, 1.35, 0.82], "shadow")
    for index, x in enumerate((-1.55, -0.55, 0.45, 1.45)):
        builder.cube("armorHead", f"mouth_vent_{index}", [x, 24.4, -5.12], [0.35, 1.0, 0.28], "light")
    builder.cube("armorHead", "chin_plate", [-1.65, 23.85, -4.82], [3.3, 0.8, 1.0], "light")
    builder.cube("armorHead", "right_ear_module", [-4.82, 26.1, -2.2], [1.05, 3.4, 4.4], "base")
    builder.cube("armorHead", "left_ear_module", [3.77, 26.1, -2.2], [1.05, 3.4, 4.4], "base")
    builder.cube("armorHead", "rear_filter", [-2.7, 24.5, 3.85], [5.4, 2.2, 0.9], "shadow")
    builder.cube("armorHead", "command_stripe", [-0.6, 28.9, -4.9], [1.2, 3.0, 0.4], "accent")


def add_phase_i_clone_helmet(builder: ModelBuilder) -> None:
    """Early-war Phase I helmet: high crown fin, broad visor, grille, and rear filter."""
    builder.cube("armorHead", "phase_i_helmet_shell", [-4, 24, -4], [8, 8, 8], "light", inflate=0.42)
    builder.cube("armorHead", "phase_i_helmet_crown", [-3.6, 31.35, -3.6], [7.2, 1.35, 7.2], "light")
    builder.cube("armorHead", "phase_i_helmet_fin", [-0.7, 31.75, -3.15], [1.4, 1.8, 6.3], "accent")
    builder.cube("armorHead", "phase_i_brow", [-3.75, 28.65, -4.72], [7.5, 0.85, 0.78], "light")
    builder.cube("armorHead", "phase_i_visor", [-3.4, 27.45, -4.86], [6.8, 1.25, 0.78], "dark")
    builder.cube("armorHead", "phase_i_visor_drop", [-0.62, 26.0, -4.9], [1.24, 1.55, 0.82], "dark")
    builder.cube(
        "armorHead", "phase_i_cheek_plate", [-3.5, 24.65, -4.82], [7.0, 2.7, 0.7],
        "light")
    builder.cube("armorHead", "phase_i_mouth_grille", [-2.35, 24.05, -4.98], [4.7, 1.45, 0.88], "shadow")
    for index, x in enumerate((-1.65, -0.55, 0.55, 1.65)):
        builder.cube("armorHead", f"phase_i_grille_vent_{index}", [x, 24.22, -5.1], [0.3, 1.0, 0.28], "base")
    builder.cube("armorHead", "phase_i_chin", [-1.7, 23.72, -4.78], [3.4, 0.85, 1.0], "light")
    builder.cube("armorHead", "phase_i_right_ear", [-4.82, 26.0, -2.2], [1.05, 3.7, 4.4], "base")
    builder.cube("armorHead", "phase_i_left_ear", [3.77, 26.0, -2.2], [1.05, 3.7, 4.4], "base")
    builder.cube("armorHead", "phase_i_rear_filter", [-2.75, 24.45, 3.86], [5.5, 2.35, 0.9], "shadow")


def add_mandalorian_helmet(builder: ModelBuilder, *, heavy: bool = False) -> None:
    shell_material = "light" if heavy else "base"
    builder.cube("armorHead", "helmet_shell", [-4, 24, -4], [8, 8, 8], shell_material, inflate=0.4)
    builder.cube("armorHead", "helmet_crown", [-3.55, 31.45, -3.55], [7.1, 1.2, 7.1], shell_material)
    builder.cube("armorHead", "helmet_brow", [-3.75, 28.65, -4.75], [7.5, 0.8, 0.78], shell_material)
    builder.cube("armorHead", "visor_bar", [-3.45, 27.7, -4.86], [6.9, 1.05, 0.72], "dark")
    builder.cube("armorHead", "vertical_visor", [-0.65, 24.45, -4.9], [1.3, 3.4, 0.76], "dark")
    builder.cube(
        "armorHead", "right_cheek_plate", [-3.65, 24.45, -4.84], [2.7, 3.2, 0.72],
        shell_material, rotation=[0, 0, 7], pivot=[-2.25, 26.0, -4.5])
    builder.cube(
        "armorHead", "left_cheek_plate", [0.95, 24.45, -4.84], [2.7, 3.2, 0.72],
        shell_material, rotation=[0, 0, -7], pivot=[2.25, 26.0, -4.5])
    builder.cube("armorHead", "right_ear_module", [-4.8, 26.0, -2.0], [1.0, 3.5, 4.0], "shadow")
    builder.cube("armorHead", "left_ear_module", [3.8, 26.0, -2.0], [1.0, 3.5, 4.0], "shadow")
    builder.cube("armorHead", "rear_filter", [-2.6, 24.6, 3.9], [5.2, 2.1, 0.8], "shadow")
    if not heavy:
        builder.cube("armorHead", "rangefinder_stem", [4.35, 27.0, -0.65], [0.75, 5.0, 1.1], "accent")
        builder.cube("armorHead", "rangefinder_sensor", [3.75, 31.0, -3.75], [1.8, 1.2, 3.2], "dark")


def add_separatist_helmet(builder: ModelBuilder) -> None:
    builder.cube("armorHead", "helmet_shell", [-4, 24, -4], [8, 8, 8], "base", inflate=0.48)
    builder.cube("armorHead", "helmet_crown", [-3.6, 31.45, -3.6], [7.2, 1.3, 7.2], "shadow")
    builder.cube("armorHead", "crown_reinforcement", [-1.5, 31.9, -3.2], [3, 1.0, 6.4], "base")
    builder.cube("armorHead", "sensor_visor", [-3.5, 27.6, -4.9], [7, 1.35, 0.85], "accent")
    builder.cube("armorHead", "face_guard", [-2.8, 24.2, -4.86], [5.6, 3.25, 0.82], "shadow")
    builder.cube("armorHead", "chin_filter", [-1.8, 23.8, -4.75], [3.6, 1.0, 1.0], "base")
    builder.cube("armorHead", "right_sensor_pod", [-4.9, 26.4, -2.2], [1.2, 3.1, 4.4], "base")
    builder.cube("armorHead", "left_sensor_pod", [3.7, 26.4, -2.2], [1.2, 3.1, 4.4], "base")
    builder.cube("armorHead", "rear_power_node", [-2.2, 25.0, 3.9], [4.4, 2.8, 0.9], "accent")


def add_nightsister_helmet(builder: ModelBuilder) -> None:
    builder.cube("armorHead", "helmet_shell", [-3.75, 24.2, -3.75], [7.5, 7.6, 7.5], "shadow", inflate=0.3)
    builder.cube("armorHead", "mask_brow", [-3.25, 28.1, -4.55], [6.5, 0.9, 0.75], "base")
    builder.cube("armorHead", "mask_visor", [-2.9, 27.2, -4.68], [5.8, 0.95, 0.7], "dark")
    builder.cube("armorHead", "mask_spine", [-0.6, 24.4, -4.72], [1.2, 2.9, 0.72], "accent")
    builder.cube("armorHead", "mask_chin", [-1.8, 23.95, -4.62], [3.6, 0.8, 0.9], "base")
    builder.cube("armorHead", "hood_crown", [-4.6, 30.1, -4.55], [9.2, 2.8, 9.1], "cloth")
    builder.cube("armorHead", "hood_back", [-4.65, 23.7, 3.6], [9.3, 6.7, 1.15], "cloth")
    builder.cube("armorHead", "hood_right", [-4.65, 23.7, -4.55], [1.25, 6.7, 8.3], "cloth")
    builder.cube("armorHead", "hood_left", [3.4, 23.7, -4.55], [1.25, 6.7, 8.3], "cloth")
    builder.cube("armorHead", "hood_brow", [-3.4, 29.45, -4.75], [6.8, 1.0, 0.8], "cloth")


def add_family_armor_details(builder: ModelBuilder, family: str) -> None:
    if family == "republic_plastoid":
        add_republic_helmet(builder)
        builder.cube("armorBody", "chest_command_stripe", [-0.55, 18.0, -3.15], [1.1, 3.6, 0.4], "accent")
        builder.cube("armorRightArm", "right_rank_plate", [-8.35, 18.3, -2.9], [4.7, 2.4, 0.45], "accent")
    elif family == "phase_i_clone":
        add_phase_i_clone_helmet(builder)
        builder.cube("armorBody", "phase_i_chest_rank_panel", [-2.15, 18.25, -3.16], [4.3, 2.25, 0.42], "accent")
        builder.cube("armorBody", "phase_i_upper_back_module", [-2.6, 18.0, 2.95], [5.2, 3.7, 0.8], "base")
    elif family == "separatist_alloy":
        add_separatist_helmet(builder)
        builder.cube("armorBody", "power_cell", [-2.4, 16.5, -3.18], [4.8, 2.8, 0.55], "accent")
        builder.cube("armorBody", "rear_reactor", [-2.6, 15.2, 2.9], [5.2, 5.4, 1.4], "accent")
        builder.cube("armorBody", "mechanical_spine", [-0.7, 13.4, 4.0], [1.4, 8.2, 0.7], "light")
        builder.cube("armorRightArm", "right_servo", [-8.65, 15.0, -3.0], [5.3, 2.2, 1.0], "accent")
        builder.cube("armorLeftArm", "left_servo", [3.35, 15.0, -3.0], [5.3, 2.2, 1.0], "accent")
        builder.cube("armorRightLeg", "right_knee_piston", [-3.2, 4.2, -3.35], [2.4, 2.1, 0.7], "light")
        builder.cube("armorLeftLeg", "left_knee_piston", [0.8, 4.2, -3.35], [2.4, 2.1, 0.7], "light")
    elif family == "mandalorian_alloy":
        add_mandalorian_helmet(builder)
        builder.cube("armorBody", "compact_pack", [-2.8, 15.0, 2.8], [5.6, 7.1, 2.0], "shadow")
        builder.cube("armorBody", "pack_light", [-1.2, 17.2, 4.75], [2.4, 2.0, 0.45], "accent")
    elif family == "nightsister_weave":
        add_nightsister_helmet(builder)
        builder.cube(
            "armorBody", "ritual_sash", [-4.45, 14.6, -3.0], [8.9, 1.35, 5.7],
            "cloth", rotation=[0, 0, -11], pivot=[0, 15.2, 0])
        builder.cube("armorBody", "right_skirt", [-4.2, 7.0, -2.35], [3.7, 4.6, 4.7], "cloth")
        builder.cube("armorBody", "left_skirt", [0.5, 7.0, -2.35], [3.7, 4.6, 4.7], "cloth")
        builder.cube("armorBody", "front_tabard", [-1.55, 6.7, -2.8], [3.1, 5.0, 0.65], "accent")
        builder.cube("armorBody", "back_tabard", [-1.55, 6.7, 2.15], [3.1, 5.0, 0.65], "cloth")
    elif family == "beskar":
        add_mandalorian_helmet(builder, heavy=True)
        builder.cube("armorBody", "heavy_collar", [-5, 20.5, -2.8], [10, 2.8, 5.6], "light")
        builder.cube("armorRightArm", "right_heavy_pauldron", [-9.4, 18.0, -3.1], [6, 5.8, 6.2], "light")
        builder.cube("armorLeftArm", "left_heavy_pauldron", [3.4, 18.0, -3.1], [6, 5.8, 6.2], "light")
        builder.cube("armorBody", "beskar_spine", [-0.7, 14.5, 3.2], [1.4, 7.0, 0.65], "accent")
    else:
        raise ValueError(f"Unknown armor family: {family}")


def build_armor(family: str, palette: Palette) -> None:
    builder = ModelBuilder(
        f"armor.{family}",
        palette,
        atlas_size=ARMOR_ATLAS_SIZE,
        texel_density=ARMOR_TEXEL_DENSITY,
    )
    armor_bones(builder)
    primary_plate = {
        "separatist_alloy": "base",
        "mandalorian_alloy": "base",
        "nightsister_weave": "base",
    }.get(family, "light")
    secondary_plate = "dark" if family == "nightsister_weave" else "base"
    if family == "nightsister_weave":
        add_nightsister_cloth_base(builder)
    else:
        add_common_armor(builder, primary_plate, secondary_plate)
    add_family_armor_details(builder, family)
    builder.write(ARMOR_MODELS / f"{family}.geo.json", ARMOR_TEXTURES / f"{family}.png")
    # GeckoLib parses the declared animation resource even though armor pose
    # comes from the wearer. Keep one valid no-op clip instead of an empty
    # animation table, which GeckoLib rejects during resource loading.
    animation = {
        "format_version": "1.8.0",
        "animations": {
            "armor.pose": {
                "loop": True,
                "animation_length": 1,
                "bones": {},
            }
        },
    }
    (ARMOR_ANIMATIONS / f"{family}.animation.json").write_text(
        json.dumps(animation, indent=2) + "\n", encoding="utf-8")


def write_phase_i_clone_inventory_assets() -> None:
    """Create a cohesive early-war armor icon set and modern item-model declarations."""
    palette = ARMOR_PALETTES["phase_i_clone"]
    outline = (*palette.dark, 255)
    shadow = (*palette.shadow, 255)
    base = (*palette.base, 255)
    light = (*palette.light, 255)
    accent = (*palette.accent, 255)
    for piece in ("helmet", "chestplate", "leggings", "boots"):
        item_id = f"phase_i_clone_{piece}"
        image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        draw = ImageDraw.Draw(image)
        if piece == "helmet":
            draw.polygon(((4, 2), (6, 1), (9, 1), (11, 2), (12, 5), (11, 12), (9, 14), (6, 14), (4, 12), (3, 5)), fill=outline)
            draw.rectangle((5, 3, 10, 10), fill=light)
            draw.rectangle((4, 5, 11, 7), fill=base)
            draw.rectangle((5, 6, 10, 7), fill=outline)
            draw.rectangle((7, 1, 8, 4), fill=accent)
            draw.rectangle((6, 10, 9, 12), fill=shadow)
            draw.point((5, 4), fill=(255, 255, 250, 255))
        elif piece == "chestplate":
            draw.polygon(((3, 2), (6, 1), (7, 3), (8, 3), (9, 1), (12, 2), (14, 6), (12, 8), (11, 14), (4, 14), (3, 8), (1, 6)), fill=outline)
            draw.polygon(((4, 3), (6, 2), (7, 5), (8, 5), (10, 2), (11, 3), (12, 7), (10, 13), (5, 13), (3, 7)), fill=light)
            draw.rectangle((5, 7, 10, 9), fill=base)
            draw.rectangle((7, 4, 8, 10), fill=accent)
            draw.line((5, 12, 10, 12), fill=shadow)
        elif piece == "leggings":
            draw.polygon(((3, 2), (12, 2), (12, 7), (10, 14), (7, 14), (7, 8), (6, 8), (6, 14), (3, 14)), fill=outline)
            draw.rectangle((4, 3, 11, 7), fill=light)
            draw.rectangle((4, 8, 6, 13), fill=base)
            draw.rectangle((8, 8, 10, 13), fill=base)
            draw.rectangle((4, 7, 11, 8), fill=accent)
            draw.point((5, 9), fill=(255, 255, 250, 255))
        else:
            draw.rectangle((2, 7, 6, 13), fill=outline)
            draw.rectangle((9, 7, 13, 13), fill=outline)
            draw.rectangle((3, 6, 6, 11), fill=light)
            draw.rectangle((9, 6, 12, 11), fill=light)
            draw.rectangle((2, 12, 6, 14), fill=shadow)
            draw.rectangle((9, 12, 13, 14), fill=shadow)
            draw.rectangle((4, 7, 5, 9), fill=accent)
            draw.rectangle((10, 7, 11, 9), fill=accent)
        save_png(image, ITEM_TEXTURES / f"{item_id}.png")
        (ITEM_MODELS / f"{item_id}.json").write_text(json.dumps({
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"galacticwars:item/{item_id}"},
        }, indent=2) + "\n", encoding="utf-8")
        (ITEM_DEFINITIONS / f"{item_id}.json").write_text(json.dumps({
            "model": {"type": "minecraft:model", "model": f"galacticwars:item/{item_id}"},
        }, indent=2) + "\n", encoding="utf-8")


def write_spawn_egg(design: RecruitDesign) -> None:
    """Build a shared 3D recruitment capsule with a material set for this recruit."""
    builder = ModelBuilder(
        "item.spawn_capsule",
        design.palette,
        atlas_size=512,
        texel_density=4,
    )
    builder.bone("root", [0, 0, 0])
    builder.bone("shell", [0, 0, 0], "root")
    builder.bone("core", [0, 0, 0], "root")
    builder.cube("shell", "capsule_core", [-3.5, -3.5, -3.5], [7, 7, 7], "dark")
    builder.cube("shell", "front_shell", [-3.3, -2.8, -4.15], [6.6, 5.6, 0.85], "base")
    builder.cube("shell", "rear_shell", [-3.3, -2.8, 3.3], [6.6, 5.6, 0.85], "base")
    builder.cube("shell", "right_shell", [-4.15, -2.8, -3.3], [0.85, 5.6, 6.6], "base")
    builder.cube("shell", "left_shell", [3.3, -2.8, -3.3], [0.85, 5.6, 6.6], "base")
    builder.cube("shell", "top_cap", [-3.15, 3.2, -3.15], [6.3, 1.0, 6.3], "light")
    builder.cube("shell", "top_step", [-2.45, 4.1, -2.45], [4.9, 0.8, 4.9], "base")
    builder.cube("shell", "top_node", [-1.3, 4.8, -1.3], [2.6, 0.75, 2.6], "accent")
    builder.cube("shell", "bottom_cap", [-3.15, -4.2, -3.15], [6.3, 1.0, 6.3], "light")
    builder.cube("shell", "bottom_step", [-2.45, -5.0, -2.45], [4.9, 0.8, 4.9], "base")
    builder.cube("shell", "front_band", [-3.65, -0.7, -4.3], [7.3, 1.4, 0.65], "shadow")
    builder.cube("shell", "rear_band", [-3.65, -0.7, 3.65], [7.3, 1.4, 0.65], "shadow")
    builder.cube("shell", "right_band", [-4.3, -0.7, -3.65], [0.65, 1.4, 7.3], "shadow")
    builder.cube("shell", "left_band", [3.65, -0.7, -3.65], [0.65, 1.4, 7.3], "shadow")
    builder.cube("core", "front_status_light", [-1.0, -1.0, -4.55], [2.0, 2.0, 0.55], "accent")
    builder.cube("core", "rear_status_light", [-1.0, -1.0, 4.0], [2.0, 2.0, 0.55], "accent")
    builder.cube("core", "right_status_light", [-4.55, -1.0, -1.0], [0.55, 2.0, 2.0], "accent")
    builder.cube("core", "left_status_light", [4.0, -1.0, -1.0], [0.55, 2.0, 2.0], "accent")

    texture_path = SPAWN_CAPSULE_TEXTURES / f"{design.id}.png"
    builder.write(ITEM_GEO_MODELS / "spawn_capsule.geo.json", texture_path)
    write_spawn_capsule_glowmask(texture_path, design.palette)
    write_spawn_capsule_icon(design)
    write_spawn_capsule_item_files(design.id)


def write_spawn_capsule_glowmask(texture_path: Path, palette: Palette) -> None:
    source = Image.open(texture_path).convert("RGBA")
    glowmask = Image.new("RGBA", source.size, (0, 0, 0, 0))
    competing = (
        palette.skin, palette.shadow, palette.dark, palette.base,
        palette.light, palette.cloth,
    )
    accent = palette.accent
    for y in range(source.height):
        for x in range(source.width):
            red, green, blue, alpha = source.getpixel((x, y))
            if alpha == 0:
                continue
            color = red, green, blue
            accent_distance = sum((left - right) ** 2 for left, right in zip(color, accent))
            other_distance = min(
                sum((left - right) ** 2 for left, right in zip(color, candidate))
                for candidate in competing)
            if accent_distance <= other_distance:
                glowmask.putpixel((x, y), (red, green, blue, alpha))
    save_png(glowmask, texture_path.with_name(texture_path.stem + "_glowmask.png"))


def write_spawn_capsule_icon(design: RecruitDesign) -> None:
    image = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    outline = (*design.palette.dark, 255)
    shadow = (*design.palette.shadow, 255)
    base = (*design.palette.base, 255)
    light = (*design.palette.light, 255)
    accent = (*design.palette.accent, 255)
    draw.rectangle((5, 1, 10, 1), fill=outline)
    draw.rectangle((4, 2, 11, 3), fill=light, outline=outline)
    draw.rectangle((3, 4, 12, 11), fill=base, outline=outline)
    draw.rectangle((2, 6, 13, 9), fill=shadow, outline=outline)
    draw.rectangle((4, 12, 11, 13), fill=light, outline=outline)
    draw.rectangle((5, 14, 10, 14), fill=outline)
    draw.rectangle((6, 6, 9, 9), fill=outline)
    draw.rectangle((7, 7, 8, 8), fill=accent)
    draw.point((4, 5), fill=(*clamp_color(design.palette.base, 28), 255))
    ITEM_TEXTURES.mkdir(parents=True, exist_ok=True)
    save_png(image, ITEM_TEXTURES / f"{design.id}_spawn_egg.png")


def write_spawn_capsule_item_files(visual_id: str) -> None:
    item_id = f"{visual_id}_spawn_egg"
    ITEM_DEFINITIONS.mkdir(parents=True, exist_ok=True)
    ITEM_MODELS.mkdir(parents=True, exist_ok=True)
    (ITEM_MODELS / "spawn_capsule_base.json").write_text(json.dumps({
        "parent": "builtin/entity",
        "ambientocclusion": False,
        "gui_light": "front",
        "textures": {
            "particle": "galacticwars:item/clone_trooper_spawn_egg",
        },
        "display": {
            "thirdperson_righthand": {
                "rotation": [0, -35, 20], "translation": [0, 2.5, 0], "scale": [0.72, 0.72, 0.72]},
            "thirdperson_lefthand": {
                "rotation": [0, 35, -20], "translation": [0, 2.5, 0], "scale": [0.72, 0.72, 0.72]},
            "firstperson_righthand": {
                "rotation": [0, -35, 12], "translation": [1.0, 3.0, 1.0], "scale": [0.7, 0.7, 0.7]},
            "firstperson_lefthand": {
                "rotation": [0, 35, -12], "translation": [-1.0, 3.0, 1.0], "scale": [0.7, 0.7, 0.7]},
            "gui": {
                "rotation": [24, 225, 0], "translation": [0, 0, 0], "scale": [0.9, 0.9, 0.9]},
            "ground": {
                "rotation": [0, 0, 0], "translation": [0, 2.0, 0], "scale": [0.55, 0.55, 0.55]},
            "fixed": {
                "rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.75, 0.75, 0.75]},
        },
    }, indent=2) + "\n", encoding="utf-8")
    (ITEM_MODELS / f"{item_id}.json").write_text(json.dumps({
        "parent": "galacticwars:item/spawn_capsule_base",
        "textures": {
            "particle": f"galacticwars:item/{item_id}",
        },
    }, indent=2) + "\n", encoding="utf-8")
    (ITEM_DEFINITIONS / f"{item_id}.json").write_text(json.dumps({
        "model": {
            "type": "minecraft:special",
            "base": f"galacticwars:item/{item_id}",
            "model": {"type": "geckolib:geckolib"},
        },
    }, indent=2) + "\n", encoding="utf-8")
    (ITEM_GEO_ANIMATIONS / "spawn_capsule.animation.json").write_text(json.dumps({
        "format_version": "1.8.0",
        "animations": {
            "animation.spawn_capsule.idle": {
                "loop": True,
                "animation_length": 1.0,
                "bones": {
                    "core": {
                        "scale": {
                            "0.0": [1.0, 1.0, 1.0],
                            "0.5": [1.06, 1.06, 1.06],
                            "1.0": [1.0, 1.0, 1.0],
                        },
                    },
                },
            },
        },
    }, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    # These five checked-in assets contain final hand-authored adjustments made
    # after their licensed source conversion. Keep those curated bytes while the
    # importer refreshes every other authorized asset.
    curated_overrides = {
        path: path.read_bytes()
        for path in CURATED_AUTHORIZED_OVERRIDES
        if path.is_file()
    }
    try:
        for directory in (
                ENTITY_MODELS, ENTITY_ANIMATIONS, ENTITY_TEXTURES,
                ARMOR_MODELS, ARMOR_ANIMATIONS, ARMOR_TEXTURES,
                ITEM_GEO_MODELS, ITEM_GEO_ANIMATIONS, SPAWN_CAPSULE_TEXTURES,
                ITEM_DEFINITIONS, ITEM_MODELS, ITEM_TEXTURES):
            directory.mkdir(parents=True, exist_ok=True)
        for design in RECRUITS:
            build_recruit(design)
        for family, palette in ARMOR_PALETTES.items():
            build_armor(family, palette)
        write_phase_i_clone_inventory_assets()
        from import_authorized_character_assets import main as import_authorized_assets
        import_authorized_assets()
    finally:
        for path, content in curated_overrides.items():
            path.write_bytes(content)
    print(
        f"Generated {len(RECRUITS)} recruit sets and {len(ARMOR_PALETTES)} armor sets; "
        "authorized upstream conversions applied last"
    )


if __name__ == "__main__":
    main()
