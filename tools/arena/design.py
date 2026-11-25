"""Build the circular Blossom Arena as four editable v16 architectural stages."""
import json
import math
import sys
from pathlib import Path

OUT = Path("build/arena-design")
HALF = 56
SIZE = {"x": 113, "y": 38, "z": 113}
WHITE = "smooth_quartz"
SHELL = "white_concrete"
SILVER = "polished_diorite"
ROSE = "pink_concrete"
GLASS = "light_blue_stained_glass"
LIGHT = "pearlescent_froglight[axis=y]"
SLAB = "smooth_quartz_slab[type=bottom,waterlogged=false]"
STAGES = ("envelope", "roof", "ornament")
cells = {}


def put(x, y, z, block):
    if not (-HALF <= x <= HALF and -HALF <= z <= HALF and 0 <= y < SIZE["y"]):
        raise ValueError((x, y, z))
    cells[x, y, z] = block


def pillar(x, z, low, high, block):
    for y in range(low, high + 1):
        put(x, y, z, block)


def polar_cells(inner, outer):
    """Yield block centres within a concentric band; angle zero is the south gate."""
    reach = math.floor(outer)
    for z in range(-reach, reach + 1):
        for x in range(-reach, reach + 1):
            r = math.hypot(x, z)
            if inner <= r <= outer:
                yield x, z, r, math.atan2(x, z)


def radial_gap(r, angle, count, offset=0):
    """Perpendicular distance to the nearest spoke for aisles, fins and glazing bars."""
    return r * abs(math.sin(math.remainder(angle - offset, math.tau / count)))


MAST_OFFSET = math.pi / 16


def roof_height(radius, angle):
    # One inclined roof ribbon, rising towards its outer edge and the two entries.
    return 17 + round((radius - 41) * .4 + 2.5 * math.cos(2 * angle))


def frame():
    for y, radius, block in [(0,45,SILVER),(1,49,"quartz_bricks"),(2,52,SHELL),
                             (3,54,WHITE),(4,55,"white_terracotta"),(5,55,WHITE)]:
        for x,z,_,_ in polar_cells(0,radius):
            put(x,y,z,block)
    # Outer masts and their continuous radial beams expose the cantilever load path.
    for x,z,r,a in polar_cells(41,54.5):
        gap = radial_gap(r,a,16,MAST_OFFSET)
        if gap <= .85:
            y = roof_height(r,a)
            if r >= 51 and gap <= .70:
                pillar(x,z,6,y-1,SHELL)
            pillar(x,z,y-1,y,WHITE)
    for sign in (-1,1):
        for z in range(45,50):
            pillar(sign*5,z,6,12,WHITE)
        for x in range(0,6):
            put(sign*x,12,47,SHELL)


def envelope():
    for x,z,r,a in polar_cells(34.5,55):
        tunnel = z > 0 and abs(x) <= 4
        if r < 35.5:
            if not tunnel:
                put(x,5,z,"white_terracotta")
        elif r < 47.5:
            d = math.floor(r-35.5)
            top = 6+d//2
            if tunnel:
                continue
            aisle = radial_gap(r,a,8) <= 1.45
            pillar(x,z,6,top-1,"quartz_bricks")
            if aisle:
                put(x,top,z,SLAB if d%2 == 0 else WHITE)
            else:
                # Wrap pale and deeper pink seating in alternating sections of four rows.
                section = math.floor((a+math.pi/8)/(math.pi/4)) % 2
                seat = ("pink_terracotta" if section else "pink_concrete") if d//2 in (1,4) else "white_terracotta"
                put(x,top,z,seat if d%2 else "quartz_bricks")
        elif r < 54.6:
            put(x,11,z,WHITE)
            if r >= 53 and not tunnel:
                put(x,6,z,SHELL)
                pillar(x,z,7,10,GLASS)
                put(x,12,z,"white_stained_glass")
    # The northern box follows the curve of the concourse. Its two ends open onto the ring.
    for x,z,r,a in polar_cells(47.8,53.5):
        if z >= 0 or abs(x)>14:
            continue
        if r < 49 or r > 52.3:
            panel = SHELL if abs(x)%6 == 2 else GLASS
            pillar(x,z,12,14,panel)
        put(x,15,z,SLAB)
    # Glazed perimeter alternates with leaning uprights, following the circular plinth.
    for y in range(6,13):
        for x,z,r,a in polar_cells(53,55):
            if z>0 and abs(x)<6:
                continue
            if radial_gap(r,a+(y-6)*.012,32,MAST_OFFSET) < .72:
                put(x,y,z,SHELL)


def roof():
    for x,z,r,a in polar_cells(40.5,55.5):
        # Narrow open joints separate the roof into panels. The ribs span each joint.
        joint = radial_gap(r,a,16,MAST_OFFSET)
        if .85 < joint < 1.50:
            continue
        y = roof_height(r,a)
        rib = joint <= .85
        if 47.5 < r < 50.8 and not rib:
            put(x,y,z,"white_stained_glass")
        else:
            put(x,y,z,WHITE if rib or r < 42 else SHELL)
            if r < 42 or r > 54:
                put(x,y-1,z,"white_terracotta")
        if rib and r >= 51:
            put(x,y+1,z,SLAB)


def ornament():
    # Two horizontal light bands articulate the floating base and gallery soffit.
    for x,z,r,a in polar_cells(53.6,55):
        put(x,4,z,LIGHT if radial_gap(r,a,32) < 1 else "pink_terracotta")
        if r <= 54.5 and not (z > 0 and abs(x)<6):
            put(x,10,z,LIGHT if radial_gap(r,a,32) < 1.1 else WHITE)
    for x,z,r,a in polar_cells(40.5,55):
        rib = radial_gap(r,a,16,MAST_OFFSET)
        if rib <= .80 and 41.3 < r < 46.5:
            put(x,roof_height(r,a)-2,z,LIGHT)
        if 46.5 < r < 47.5 and radial_gap(r,a,8)<1 and not (z>0 and abs(x)<=4):
            put(x,11,z,WHITE)
    # Recessed glass and two light pylons mark the portal without crossing its 7x4 opening.
    for sign in (-1,1):
        for z in range(42,51):
            pillar(sign*6,z,6,9,"pink_stained_glass" if z%4 else WHITE)
        for x in range(4,8):
            put(sign*x,10,47,WHITE)
        pillar(sign*4,47,6,9,LIGHT)
        pillar(sign*5,46,6,10,"pink_terracotta")
        pillar(sign*5,48,6,10,SHELL)
    for x in range(-4,5):
        put(x,10,47,WHITE)
        put(x,11,46,LIGHT)
    # Frosted-glass balcony rails flank the press box. Leave four openings for circulation.
    for x,z,r,a in polar_cells(47.4,48.45):
        if z < 0 and 16<=abs(x)<=23:
            put(x,12,z,"pink_stained_glass")


def seat_banks():
    # Short banks of seats have backs facing away from the court. Leave each radial aisle clear.
    for x,z,r,a in polar_cells(35.5,47.49):
        d = math.floor(r-35.5)
        if d%2 == 0 or radial_gap(r,a,8) < 2.3 or (z>0 and abs(x)<6):
            continue
        if radial_gap(r,a,48) < .65:
            continue
        direction = ("east" if x>0 else "west") if abs(x)>abs(z) else ("south" if z>0 else "north")
        material = "cherry" if d//2 in (1,2,4) else "smooth_quartz"
        put(x,7+d//2,z,f"{material}_stairs[facing={direction},half=bottom,shape=straight,waterlogged=false]")
    # Half-height outer and inner lips break up the stepped profile of the inclined canopy.
    for x,z,r,a in polar_cells(40.5,55.5):
        if radial_gap(r,a,16,MAST_OFFSET)<1.5 or not (r<42 or r>=54):
            continue
        put(x,roof_height(r,a),z,SLAB)


def rose_finish():
    # Use the already-approved block shapes so changes to the palette never fill an aisle or roof joint.
    for layer in (frame,envelope,roof,ornament,seat_banks):
        layer()
    architecture = cells.copy()
    cells.clear()
    for (x,y,z),material in architecture.items():
        r = math.hypot(x,z)
        a = math.atan2(x,z)
        finish = material
        if y==5 and r<34.5:
            finish = "cherry_planks"
        elif y>=14 and r>=40.5:
            if material=="white_stained_glass":
                finish = "pink_stained_glass"
            elif material==SHELL and r>=42 and radial_gap(r,a,16,MAST_OFFSET)>1.5:
                finish = ROSE
            elif material=="white_terracotta":
                finish = "pink_concrete"
            elif material==SLAB and r>=54:
                finish = "cherry_slab[type=bottom,waterlogged=false]"
        elif 35.5<=r<47.5 and y>=6:
            if material=="white_terracotta": finish = "pink_wool"
            elif material.startswith("smooth_quartz_stairs") and math.floor(r-35.5)//2!=0:
                finish = material.replace("smooth_quartz","cherry")
        elif r>=47.5 and y>=6 and material==GLASS:
            finish = "pink_stained_glass"
        if material != finish:
            put(x,y,z,finish)


def operations():
    # Encode contiguous block runs so the stage files stay readable and compact.
    result = []
    for (x,y,z), material in sorted(cells.items(),key=lambda pair:(pair[0][1],pair[0][2],pair[0][0])):
        pos = [x+HALF,y,z+HALF]
        if result and result[-1]["block"] == "minecraft:"+material and result[-1]["to"] == [pos[0]-1,y,pos[2]]:
            result[-1]["to"] = pos
        else:
            result.append({"op":"fill","from":pos,"to":pos,"block":"minecraft:"+material})
    return result


def save(name,value):
    OUT.mkdir(parents=True,exist_ok=True)
    (OUT/(name+".json")).write_text(json.dumps(value,indent=2),encoding="utf-8")


if __name__ == "__main__":
    stage = sys.argv[1]
    {"frame":frame,"envelope":envelope,"roof":roof,"ornament":ornament,"seat_banks":seat_banks,"rose_finish":rose_finish}[stage]()
    if stage == "frame":
        brief = {"name":"Blossom Forum","source_mode":"text","dimensions":SIZE,"front_direction":"south",
                 "hard_constraints":["Circular massing including plinth, seating and roof","Plain level floor of radius 34","Light roof surfaces","Portal and mirror coordinates preserved"],
                 "must_preserve":[{"feature":f,"priority":5,"evidence":e} for f,e in [
                     ("Unobstructed central fighting floor","Existing server radius and original mirror position"),
                     ("Circular stepped bowl with radial aisles","Arena sightline studies and requested circular silhouette"),
                     ("Pale cantilever canopy with an open centre","Requested contemporary style and light roof")]],
                 "massing":{"floor_count":2,"description":"Low floating circular stadium with a raised northern press box",
                            "major_volumes":["arena bowl","south entry tunnel","outer concourse","north viewing box"],"symmetry":"bilateral_x"},
                 "roof":{"system":"inclined annular cantilever ribbon","ridge_axis":"x","tiers":1,"pitch":"shallow","overhang":11,"silhouette_notes":"a wave crest at the north and south"},
                 "facade_rhythm":{"primary_bays":16,"vertical_layers":3,"repeating_modules":["radial cantilever","leaning facade fin"],"notes":"continuous pale fascia"},
                 "program":{"require_interior":False,"spaces":["plain field","stands","glazed concourse"],"circulation":"South tunnel to open floor, stair aisles to promenade"},
                 "palette_roles":{"floor":"minecraft:smooth_quartz","structure":"minecraft:white_concrete","roof":"minecraft:smooth_quartz","glazing":"minecraft:white_stained_glass"},
                 "uncertainties":["Material choices and roof profile are design decisions."]}
        save("brief",brief)
        save(stage,{"name":brief["name"],"dimensions":SIZE,"stage":stage,"strict_block_states":True,
                    "workflow":{"mode":"staged","refinement_passes":list(STAGES)},
                    "quality":{"require_interior":False,"require_roof_detail":True,"front_direction":"south"},"operations":operations()})
    elif stage == "rose_finish":
        save(stage,{"mode":"local_repair","name":"Finish the court, roof panels and seating in cherry and rose",
                    "scope":{"from":[1,5,1],"to":[111,26,111]},"operations":operations()})
    elif stage == "seat_banks":
        save(stage,{"mode":"local_repair","name":"Shape the seat backs and thin the canopy edges",
                    "scope":{"from":[1,7,1],"to":[111,26,111]},"operations":operations()})
    else:
        intents = {"envelope":"Circular seating, accessible stair aisles and glazed facade",
                   "roof":"Continuous pale canopy panels, rooflights and exposed radial ribs",
                   "ornament":"Integral lights and portal trim around a uniform unmarked field"}
        save(stage,{"mode":"refinement_pass","name":stage,"category":stage,"intent":[intents[stage]],"operations":operations()})
