"""Convert the reviewed v16 single region to a Java 1.21.1 structure template."""
import math
import sys
from pathlib import Path

skill, schematic, output = map(Path, sys.argv[1:4])
sys.path.insert(0, str(skill / "scripts"))
from litematic_core import TAG_COMPOUND, TAG_INT, n_compound, n_int, n_list, palette_entry, write_nbt_gzip
from validate_litematic import decode_indices, palette_entry_to_state, read_litematic, validate

ok, errors, _, _ = validate(schematic)
if not ok:
    raise ValueError(errors)
region, = read_litematic(schematic)["Regions"].values()
sx, sy, sz = (region["Size"][axis] for axis in "xyz")
palette = [palette_entry_to_state(x) for x in region["BlockStatePalette"]]
indices = decode_indices(region["BlockStates"], sx * sy * sz, max(2, math.ceil(math.log2(len(palette)))))
blocks = []
for i, material in enumerate(indices):
    if palette[material] != "minecraft:air":
        pos = [i % sx, i // (sx * sz), i // sx % sz]
        blocks.append(n_compound({"pos":n_list(TAG_INT,pos), "state":n_int(material)}))
output.parent.mkdir(parents=True, exist_ok=True)
write_nbt_gzip(output, n_compound({"DataVersion":n_int(3955), "size":n_list(TAG_INT,[sx,sy,sz]),
                                 "palette":n_list(TAG_COMPOUND,[palette_entry(x) for x in palette]),
                                 "blocks":n_list(TAG_COMPOUND,blocks), "entities":n_list(TAG_COMPOUND,[])}))
print(f"{len(blocks)} occupied positions, {len(palette)} palette states, {sx} by {sz} by {sy}")
