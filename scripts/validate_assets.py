#!/usr/bin/env python3
"""Fail the build if Hollow Hands item models reference missing/broken assets."""
from pathlib import Path
import json
import struct
import zlib

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/hollowhands"
PNG_SIG = b"\x89PNG\r\n\x1a\n"
EXPECTED = {"gear_backpack", "gear_belt", "gear_satchel", "gear_bandolier"}


def validate_png(path: Path) -> None:
    raw = path.read_bytes()
    if not raw.startswith(PNG_SIG):
        raise ValueError(f"{path}: invalid PNG signature")
    pos = len(PNG_SIG)
    idat = bytearray()
    width = height = color_type = bit_depth = interlace = None
    saw_ihdr = saw_iend = False
    while pos < len(raw):
        if pos + 12 > len(raw):
            raise ValueError(f"{path}: truncated PNG chunk")
        length = struct.unpack(">I", raw[pos:pos + 4])[0]
        kind = raw[pos + 4:pos + 8]
        end = pos + 12 + length
        if end > len(raw):
            raise ValueError(f"{path}: truncated {kind!r} chunk")
        data = raw[pos + 8:pos + 8 + length]
        stored_crc = struct.unpack(">I", raw[pos + 8 + length:end])[0]
        actual_crc = zlib.crc32(kind + data) & 0xffffffff
        if stored_crc != actual_crc:
            raise ValueError(f"{path}: CRC mismatch in {kind.decode('ascii', 'replace')} chunk")
        if kind == b"IHDR":
            if saw_ihdr or length != 13:
                raise ValueError(f"{path}: invalid IHDR")
            width, height, bit_depth, color_type, compression, filtering, interlace = struct.unpack(">IIBBBBB", data)
            if compression != 0 or filtering != 0:
                raise ValueError(f"{path}: unsupported PNG compression/filter method")
            saw_ihdr = True
        elif kind == b"IDAT":
            idat.extend(data)
        elif kind == b"IEND":
            if length != 0:
                raise ValueError(f"{path}: invalid IEND")
            saw_iend = True
            pos = end
            break
        pos = end
    if not saw_ihdr or not saw_iend or pos != len(raw):
        raise ValueError(f"{path}: missing/invalid PNG end or trailing bytes")
    if width != 64 or height != 64 or bit_depth != 8 or color_type not in (2, 6) or interlace != 0:
        raise ValueError(f"{path}: expected a non-interlaced 64x64 8-bit RGB/RGBA texture")
    try:
        decoded = zlib.decompress(idat)
    except zlib.error as exc:
        raise ValueError(f"{path}: invalid compressed pixel data: {exc}") from exc
    channels = 4 if color_type == 6 else 3
    expected_size = height * (1 + width * channels)
    if len(decoded) != expected_size:
        raise ValueError(f"{path}: decoded pixel data is {len(decoded)} bytes; expected {expected_size}")
    stride = width * channels + 1
    if any(decoded[row * stride] > 4 for row in range(height)):
        raise ValueError(f"{path}: invalid PNG row filter")


def main() -> None:
    items_dir = ASSETS / "items"
    models_dir = ASSETS / "models/item"
    textures_dir = ASSETS / "textures/item"
    item_defs = sorted(items_dir.glob("*.json"))
    if {p.stem for p in item_defs} != EXPECTED:
        raise ValueError("Expected exactly the four Hollow Hands gear item definitions")

    referenced_models = set()
    for path in item_defs:
        definition = json.loads(path.read_text())
        model_id = definition.get("model", {}).get("model")
        if not isinstance(model_id, str) or not model_id.startswith("hollowhands:item/"):
            raise ValueError(f"{path}: invalid namespaced model identifier {model_id!r}")
        model_path = models_dir / (model_id.split(":", 1)[1].split("/", 1)[1] + ".json")
        if not model_path.exists():
            raise ValueError(f"{path}: model does not exist: {model_path}")
        referenced_models.add(model_path)

    for model_path in sorted(referenced_models):
        model = json.loads(model_path.read_text())
        textures = model.get("textures", {})
        if not {"front", "side", "particle"}.issubset(textures):
            raise ValueError(f"{model_path}: expected front, side and particle texture keys")
        if len(model.get("elements", [])) != 1:
            raise ValueError(f"{model_path}: gear should use one clean cuboid, not overlapping full-art cubes")
        for key, value in textures.items():
            if isinstance(value, dict):
                value = value.get("sprite")
            if not isinstance(value, str) or not value.startswith("hollowhands:item/"):
                raise ValueError(f"{model_path}: invalid texture entry {key}: {value!r}")
            texture_path = textures_dir / (value.split("/", 1)[1] + ".png")
            if not texture_path.exists():
                raise ValueError(f"{model_path}: missing texture {texture_path}")
        faces = model["elements"][0].get("faces", {})
        if set(faces) != {"north", "south", "east", "west", "up", "down"}:
            raise ValueError(f"{model_path}: expected all six cube faces")
        if faces["north"].get("texture") != "#front" or faces["south"].get("texture") != "#side":
            raise ValueError(f"{model_path}: front/back artwork is assigned to the wrong faces")
        for face_name, face in faces.items():
            ref = face.get("texture", "")
            if ref not in ("#front", "#side"):
                raise ValueError(f"{model_path}: {face_name} references undefined texture {ref}")
            uv = face.get("uv", [0, 0, 16, 16])
            if len(uv) != 4 or any(not isinstance(v, (int, float)) or v < 0 or v > 16 for v in uv):
                raise ValueError(f"{model_path}: invalid UV on {face_name}: {uv}")

    pngs = sorted(textures_dir.glob("*.png"))
    expected_pngs = {f"{name}{suffix}.png" for name in EXPECTED for suffix in ("", "_side")}
    if {p.name for p in pngs} != expected_pngs:
        raise ValueError("Expected exactly eight front/side gear textures")
    for png in pngs:
        validate_png(png)
    print(f"Validated {len(item_defs)} item definitions, {len(referenced_models)} clean models, and {len(pngs)} 64x64 PNG textures.")


if __name__ == "__main__":
    main()
