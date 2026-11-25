"""Export the approved arena with the air needed to replace older immutable arenas."""
import math
import sys
from pathlib import Path

skill, schematic, output = map(Path, sys.argv[1:4])
sys.path.insert(0, str(skill / "scripts"))
from litematic_core import TAG_COMPOUND, TAG_INT, n_compound, n_int, n_list, palette_entry, write_nbt_gzip
from validate_litematic import decode_indices, palette_entry_to_state, read_litematic, validate


def read_region(path):
    ok, errors, _, _ = validate(path)
    if not ok:
        raise ValueError(errors)
    region, = read_litematic(path)["Regions"].values()
    size = tuple(region["Size"][axis] for axis in "xyz")
    palette = [palette_entry_to_state(x) for x in region["BlockStatePalette"]]
    indices = decode_indices(region["BlockStates"], math.prod(size), max(2, (len(palette)-1).bit_length()))
    return size, palette, indices


size, palette, indices = read_region(schematic)
sx, sy, sz = size
archive = Path(__file__).resolve().parents[2] / "art/cherry_arena/grand-court-0.10.litematic"
prior_size, prior_palette, prior_indices = read_region(archive)
if prior_size != size:
    raise ValueError("Expected the new and previous arena to share a placement origin and canvas")
old_air = prior_palette.index("minecraft:air")
air = palette.index("minecraft:air")
blocks = []
for i, material in enumerate(indices):
    pos = [i % sx, i // (sx * sz), i // sx % sz]
    x, y, z = pos[0] - sx // 2, pos[1], pos[2] - sz // 2
    runtime_cell = (x, y, z) == (0, 6, -8) or (abs(x) <= 3 and 6 <= y <= 9 and z == 47)
    if runtime_cell:
        if material != air:
            raise ValueError(f"Architecture occupies a runtime block at {pos}")
        continue
    # The 0.9 exit is inside its original envelope. The 0.10 mirror and portal keep their positions.
    first_arena = abs(x) <= 24 and abs(z) <= 24 and y <= 16
    if material != air or first_arena or prior_indices[i] != old_air:
        blocks.append(n_compound({"pos": n_list(TAG_INT,pos), "state": n_int(material)}))
output.parent.mkdir(parents=True, exist_ok=True)
write_nbt_gzip(output, n_compound({"DataVersion":n_int(3955), "size":n_list(TAG_INT,list(size)),
                                 "palette":n_list(TAG_COMPOUND,[palette_entry(x) for x in palette]),
                                 "blocks":n_list(TAG_COMPOUND,blocks), "entities":n_list(TAG_COMPOUND,[])}))
print(f"{len(blocks)} template positions including migration air, {len(palette)} palette states, {sx} by {sz} by {sy}")
