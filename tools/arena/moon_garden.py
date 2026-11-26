"""Shape the Moonlit Blossom Trial Garden in staged edits on the approved floating foundation."""
import json
import math
import sys
from pathlib import Path

from design import HALF, LIGHT, WHITE, cells, operations, polar_cells, put, save
from gallery_details import FROST, LEAVES, PINK_GLASS
from pavilion_garden import blossom_canopies, garden_frame

PLAN = json.loads(Path(__file__).with_name("moon_garden.scene.json").read_text(encoding="utf-8"))
ANCHORS = {asset["id"]: (asset["anchor"][0] - HALF, asset["anchor"][1] - HALF) for asset in PLAN["assets"]}
CHERRY = "cherry_planks"
PALE = "white_terracotta"
PEARL = "calcite"
TWIG = "stripped_cherry_wood[axis=y]"
MOSS = "moss_block"
WATER = "water[level=0]"
CHAIN = "chain[axis=y,waterlogged=false]"

# Each cluster has a broad petal and a shorter turned companion. Angle is measured from the south gate.
# Curvature and overlap vary between the two budding clusters.
PETALS = (
    (134, 47, 6.0, 7, 3.6, 5.0), (146, 46, 4.7, 5, -2.8, 3.7),
    (-36, 47, 6.2, 8, 3.3, 5.0), (-48, 46, 4.9, 6, -2.5, 3.4),
)

# Control points are local to the south gate or the north altar. Their first points are rooted in soil.
GATE_BOUGHS = (
    ((-10,6,1), (-10,10,1), (-8,15,0), (-5,19,-1), (0,21,-1), (5,20,-2)),
    ((-9,13,0), (-12,16,0), (-15,17,1)),
    ((-6,18,-1), (-7,21,0), (-10,22,1)),
    ((9,6,-1), (10,10,-1), (9,14,0), (6,17,1), (2,18,2)),
    ((9,13,0), (12,15,1), (15,16,2)),
    ((-10,10,1), (-8,10,0), (-5,11,0), (-2,12,0), (1,11,0)),
    ((10,8,-1), (7,9,-1), (5,11,-1), (3,10,0)),
)
MOON_BOUGHS = (
    ((15,6,0), (15,10,0), (13,15,0), (12,19,1), (9,22,1), (6,26,1), (2,28,0)),
    ((13,15,0), (16,18,-1), (19,20,-1)),
    ((12,19,1), (10,19,3), (7,21,4)),
    ((9,22,1), (12,25,0), (14,26,-1)),
)


def ground():
    for x, z, radius, _ in polar_cells(0, 55):
        # Pale stone hems two meandering approaches; the fighting floor is uninterrupted.
        if radius <= 34.5:
            material = CHERRY
        elif radius > 54:
            material = PEARL
        elif z > 32 and abs(x - 2 * math.sin((z - 38) / 6)) < 4:
            material = CHERRY
        elif radius < 35.5:
            material = PALE
        else:
            material = PALE if radius < 44.5 else CHERRY
        put(x, 5, z, material)


def crescent():
    cx, cz = ANCHORS["moon_altar"]
    for depth in range(-3, 4):
        # Two offset circular sections cut an open crescent, rounded on both faces.
        outer = 13.8 - .17 * depth**2
        inner = 11.7 + .14 * depth**2
        for dx in range(-14, 15):
            for y in range(6, 34):
                dy = y - 19
                radius = math.hypot(dx, dy)
                hollow = math.hypot(dx - 5.1, dy - 3.7)
                if radius > outer or hollow < inner:
                    continue
                block = WHITE
                if depth < -1:
                    block = CHERRY
                elif hollow < inner + 1 and depth < 2:
                    block = PEARL
                elif depth == 0 and radius > outer - 1.1 and dy < 0:
                    block = LIGHT
                put(cx + dx, y, cz + depth, block)

    # A low water mirror joins the foot of the crescent to the garden rather than lifting the field.
    for dx in range(-17, 18):
        for dz in range(-6, 8):
            ellipse = (dx / 16.5)**2 + ((dz - .6) / 6.2)**2
            if ellipse > 1:
                continue
            x, z = cx + dx, cz + dz
            if ellipse < .69 and (x, 6, z) not in cells:
                put(x, 4, z, LIGHT if (dx - 2 * dz) % 13 == 0 else PEARL)
                put(x, 5, z, WATER)
            else:
                put(x, 5, z, PEARL)


def limb(anchor, points, weight):
    """Trace a continuous curved bough, tapering from an oval trunk to a single twig."""
    for index, (a, b) in enumerate(zip(points, points[1:])):
        steps = max(abs(i-j) for i, j in zip(a, b)) * 5
        previous = None
        for step in range(steps + 1):
            t = step / steps
            x, y, z = (round(i + t * (j-i)) for i, j in zip(a, b))
            x += anchor[0]
            z += anchor[1]
            if previous:
                ox, oy, oz = previous
                if ox != x:
                    put(x, oy, oz, TWIG)
                if oz != z:
                    put(x, oy, z, TWIG)
            put(x, y, z, TWIG)
            previous = (x, y, z)
            if weight and index < 2:
                put(x, y, z + 1, TWIG)
                put(x + 1, y, z, TWIG)


def branches():
    for key, paths in (("blossom_gate", GATE_BOUGHS), ("moon_altar", MOON_BOUGHS)):
        for i, path in enumerate(paths):
            limb(ANCHORS[key], path, i in (0, 3) if key == "blossom_gate" else i == 0)


def petal_shells():
    for degrees, radius, width, height, lean, curl in PETALS:
        angle = math.radians(degrees)
        radial = (math.sin(angle), math.cos(angle))
        tangent = (radial[1], -radial[0])
        cx, cz = (round(radius * component) for component in radial)
        for y in range(6, 6 + height + 1):
            t = (y - 5.5) / (height + 1)
            span = 1.1 + width * math.sin(math.pi * t)**.59 * (.72 + .50*t)
            for x in range(cx - 13, cx + 14):
                for z in range(cz - 13, cz + 14):
                    dx, dz = x - cx, z - cz
                    across = dx * tangent[0] + dz * tangent[1] - lean * t*t
                    if abs(across) > span:
                        continue
                    u = across / span
                    if t > .85 + .36 * abs(u - .08):
                        continue
                    outward = dx * radial[0] + dz * radial[1]
                    section = outward - (curl * t*t + 2.2*u*u - .9*math.sin(math.pi*t))
                    if abs(section) > .94:
                        continue
                    margin = abs(u) > .78 or t > .84
                    if margin:
                        block = PEARL if t < .75 else WHITE
                    elif section > .25:
                        block = FROST
                    else:
                        block = PINK_GLASS if abs(u - .23) < .22 and t > .24 else CHERRY
                    put(x, y, z, block)


def open_blossoms():
    # A cupped five-lobed surface, tilted towards the field; the petal edges turn towards the sky.
    for cx, cy, cz, width, twist in ((-29, 13, -37, 9.3, .30), (30, 12, 36, 8.1, -.45)):
        radius = math.hypot(cx, cz)
        rx, rz = cx / radius, cz / radius
        tx, tz = rz, -rx
        for x in range(cx - 12, cx + 13):
            for z in range(cz - 12, cz + 13):
                across = (x - cx) * tx + (z - cz) * tz
                outward = (x - cx) * rx + (z - cz) * rz
                for y in range(6, cy + 12):
                    rise = y - cy
                    vertical = .83 * rise + .56 * outward
                    front = .56 * rise - .83 * outward
                    r = math.hypot(across, vertical)
                    angle = math.atan2(vertical, across) - twist
                    lobe = .5 + .5 * math.cos(5 * angle)
                    outline = width * (.36 + .72 * lobe**.62 - .12 * lobe**18)
                    if r > outline:
                        continue
                    cup = .038*r*r + .16*across + .4*math.sin(2*angle)
                    face = front - cup
                    if abs(face) > 1.25:
                        continue
                    if r < 1.7:
                        block = LIGHT
                    elif r < 2.7:
                        block = "birch_planks"
                    elif r > outline - 1.1:
                        block = FROST if face > 0 else PEARL
                    elif face > .22:
                        block = PINK_GLASS
                    else:
                        block = WHITE if lobe > .69 else CHERRY
                    put(x, y, z, block)
        # The calyx and tapered branch support the centre of the tilted blossom from behind.
        limb((cx, cz), ((2,6,1), (2,8,1), (1,cy-2,1), (0,cy,0)), True)


def brook():
    cx, cz = ANCHORS["petal_brook"]
    for dz in range(-23, 24):
        mid = cx + 3.8 * math.sin((dz + 4) / 7.8)
        width = .45 + (2.75 + .85 * math.cos(dz / 4.3)) * max(0, 1 - (dz / 23)**2)**.65
        for x in range(math.floor(mid - width - 1), math.ceil(mid + width + 1)):
            z = cz + dz
            side = abs(x - mid)
            if side > width + .8:
                continue
            if side > width:
                put(x, 5, z, PEARL if (x + dz) % 5 == 0 else PALE)
            else:
                bed = LIGHT if (x + 2 * dz) % 11 == 0 else PALE
                put(x, 4, z, bed)
                put(x, 5, z, WATER)
    # Two staggered crossings stay flush with the surrounding paving.
    for dz in (-7, 9):
        for i in range(-5, 6):
            x = round(cx + 3.8 * math.sin((dz + 4)/7.8)) + i
            put(x, 5, cz + dz + int(i >= 1), PEARL)
            if i == 1:
                put(x, 5, cz + dz, PEARL)
    for dx, dz in ((-2,-12),(0,-3),(2,5),(1,14),(-1,16)):
        x, z = cx + dx, cz + dz
        if cells.get((x, 5, z)) == WATER:
            put(x, 6, z, "lily_pad")


def planting_beds():
    # Short crescents of planting gather at the foot of each large petal. Gaps frame the void.
    for degrees, radius, width, _, _, _ in PETALS[::2]:
        a = math.radians(degrees)
        for x, z, r, angle in polar_cells(radius - 4, radius + 5):
            along = radius * math.remainder(angle - a, math.tau)
            if abs(along) >= width + 3:
                continue
            offset = r - radius + 1.5 * math.cos(along / 4)
            if abs(offset) > 2.5 * (1 - (along / (width + 3))**2):
                continue
            put(x, 5, z, MOSS)
            if (x, 6, z) in cells:
                continue
            style = (x * 17 + z * 11) % 13
            if style in (0, 1):
                put(x, 6, z, "flowering_azalea")
            elif style < 8:
                put(x, 6, z, "pink_petals[facing=south,flower_amount=" + str(1 + style % 4) + "]")
            elif style == 10:
                put(x, 6, z, "azure_bluet")


def flower_drifts():
    # Small, overlapping beds soften the transition from timber to water and the feet of the blossoms.
    beds = [(-50,-17,3,5),(-48,-11,3,4),(-50,-2,3,6),(-36,1,3,5),
            (-37,10,3,5),(-47,16,3,6),(-46,20,3,4),
            (-29,-38,6,4),(-23,-42,5,3),(29,37,6,4),(25,40,4,3),
            (12,-45,4,3),(18,-42,4,2),(-11,47,4,3),(11,47,4,3)]
    visited = set()
    for cx, cz, rx, rz in beds:
        for x in range(cx-rx, cx+rx+1):
            for z in range(cz-rz, cz+rz+1):
                wave = .12 * math.sin(x * 1.2 + z * .6)
                if ((x-cx)/rx)**2 + ((z-cz)/rz)**2 > 1 + wave:
                    continue
                if (x,z) in visited or not 35.5 < math.hypot(x,z) <= 54.5 or cells.get((x,5,z)) in (WATER, PEARL):
                    continue
                visited.add((x,z))
                if (x,6,z) in cells or (x,7,z) in cells:
                    continue
                put(x, 5, z, MOSS)
                kind = (x*17 + z*31 + x*z) % 19
                if kind in (0, 1):
                    put(x, 6, z, "peony[half=lower]")
                    put(x, 7, z, "peony[half=upper]")
                elif kind in (2, 3):
                    put(x, 6, z, "pink_tulip")
                elif kind in (4, 5):
                    put(x, 6, z, "flowering_azalea")
                elif kind == 6:
                    put(x, 6, z, "azure_bluet")
                elif kind <= 15:
                    put(x, 6, z, "pink_petals[facing=west,flower_amount=" + str(1+kind%4) + "]")


def blossom_spray(anchor, dx, y, dz, rx, ry, rz):
    for a in range(-math.ceil(rx), math.ceil(rx) + 1):
        for b in range(-math.ceil(ry), math.ceil(ry) + 1):
            for c in range(-math.ceil(rz), math.ceil(rz) + 1):
                ripple = .13 * math.sin(a*1.2 + y) * math.cos(c + dx)
                if (a/rx)**2 + ((b-.12*a)/ry)**2 + (c/rz)**2 > 1 + ripple:
                    continue
                p = (anchor[0]+dx+a, y+b, anchor[1]+dz+c)
                if p not in cells:
                    put(*p, LEAVES)


def flower_lamps():
    gate, moon = ANCHORS["blossom_gate"], ANCHORS["moon_altar"]
    for anchor, sprays in ((gate, [(-14,17,1,3,2,2),(-10,21,1,3,2,2),(-6,19,-1,3.5,2.2,2),
                                    (-2,21,-1,3.9,1.9,2.8),(4,20,-2,2.7,2,2),(10,15,0,3,2.2,2),
                                    (14,16,2,3.1,1.7,2.1),(3,18,2,2.6,1.6,2.1),(-5,12,0,2.5,1.4,1.6),(4,12,-1,2.6,1.5,1.9)]),
                           (moon, [(17,20,-1,3.2,2.1,2.1),(14,26,-1,3.6,2.0,2.2),(8,23,1,2.9,1.8,2),
                                   (2,28,0,3.5,2.1,2.1),(5,26,1,3.2,1.9,2.2),(16,19,0,3,1.8,1.9),(7,21,4,3.1,1.9,2.1)])):
        for spray in sprays:
            blossom_spray(anchor, *spray)
    for anchor, tags in ((gate, [(-5,19,-1,4),(6,17,1,3),(-12,16,0,3)]),
                         (moon, [(6,26,1,5),(14,26,-1,4),(7,21,4,3)])):
        for dx, y, dz, drop in tags:
            x, z = anchor[0] + dx, anchor[1] + dz
            for yy in range(y-drop+1, y):
                put(x, yy, z, CHAIN)
            put(x, y-drop, z, "lantern[hanging=true,waterlogged=false]")
    # Suspend a small blush star from the inner tip of the crescent. The open centre stays visible.
    sx, sz = moon[0] - 3, moon[1] + 1
    for y in range(26, 32):
        put(sx, y, sz, CHAIN)
    put(sx, 25, sz, LIGHT)
    for dx, dy, facing in ((1,0,"east"),(-1,0,"west"),(0,1,"up"),(0,-1,"down")):
        put(sx + dx, 25 + dy, sz, "end_rod[facing=" + facing + "]")


def grove():
    # The earlier ornamental trees keep their individual branches and crowns at the new ground level.
    garden_frame()
    tree = cells.copy()
    cells.clear()
    blossom_canopies()
    tree.update({p: state for p, state in cells.items() if state in (LEAVES, "flowering_azalea")})
    cells.clear()
    for (x, y, z), state in tree.items():
        put(x, y - 7, z, state)


if __name__ == "__main__":
    stage = sys.argv[1]
    prefix = []
    if stage == "garden_silhouette":
        prefix.append({"op":"fill","from":[0,6,0],"to":[112,37,112],"block":"minecraft:air"})
        ground()
        crescent()
        branches()
        petal_shells()
        open_blossoms()
    elif stage == "garden_planting":
        # Use the structural positions as a mask so small petals never replace a branch or shell.
        crescent()
        branches()
        petal_shells()
        open_blossoms()
        structure = cells.copy()
        planting_beds()
        brook()
        flower_drifts()
        flower_lamps()
        for p in list(cells):
            if structure.get(p) == cells[p]:
                del cells[p]
    elif stage == "garden_grove":
        grove()
    else:
        raise SystemExit("Expected garden_silhouette, garden_planting or garden_grove")
    save(stage, {"mode":"local_repair","name":stage,"scope":{"from":[0,4,0],"to":[112,37,112]},
                 "operations":prefix + operations()})
