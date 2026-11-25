"""Replace the north gallery and east trees of the 0.11.1 arena in three bounded passes."""
import math
import sys

from design import LIGHT, SLAB, WHITE, cells, operations, pillar, put, roof_height, save
from gallery_details import (
    BLUSH, CHERRY_SLAB, FROST, GREEN, LEAVES, TIMBER, clear_old_canopy, east_orchard,
    former_stadium, material_pass, north_gallery,
)

CHERRY = "cherry_planks"
CLEAR = "glass"
WOOD = "cherry_wood"
QUARTZ_STAIR = "smooth_quartz_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]"

# Paths are offsets from each root in x, y, z. Each leaf pad grows around its own branch endpoint.
# The windswept crown spreads towards the seats, the central crown forks upwards,
# and the southern tree carries descending sprays on its outer limbs.
TREES = (
    ((49, 13, -12), (
        ([(0,0,0), (0,2,0), (-2,4,0), (-4,5,-1), (-6,6,-4)], (3.5,1.5,3.0)),
        ([(-2,4,0), (-3,7,1), (-6,9,2)], (3.7,1.9,2.5)),
        ([(-3,7,1), (-2,10,0), (-5,10,-3)], (3.8,1.8,3.0)),
        ([(-4,5,-1), (-3,6,-3), (-4,7,-5)], (2.6,1.4,2.0)),
        ([(0,2,0), (2,4,1), (3,6,2)], (2.5,1.6,2.0)),
    )),
    ((51, 13, 0), (
        ([(0,0,0), (0,3,0), (-1,5,0), (-2,8,-1), (-5,10,-3)], (3.4,2.2,2.6)),
        ([(-2,8,-1), (-3,11,-1), (-2,14,-2)], (3.6,2.6,2.7)),
        ([(-1,5,0), (1,8,1), (2,11,2), (2,13,4)], (3.2,2.5,2.8)),
        ([(1,8,1), (3,9,0), (3,11,-2)], (2.6,2.2,2.4)),
        ([(-2,8,-1), (-4,9,0), (-5,12,0)], (3.3,2.0,2.0)),
        ([(1,8,1), (0,10,3), (1,11,4)], (2.6,1.7,2.4)),
        ([(-1,5,0), (-4,6,2), (-7,8,3)], (3.4,1.8,2.7)),
    )),
    ((48, 13, 11), (
        ([(0,0,0), (1,3,0), (0,5,1), (-2,7,2), (-4,7,4)], (3.0,2.0,2.4)),
        ([(-2,7,2), (-3,9,0), (-5,9,-2)], (3.3,2.1,2.3)),
        ([(0,5,1), (2,8,3), (3,9,4)], (2.7,2.3,2.6)),
        ([(0,5,1), (1,8,0), (0,10,1)], (3.8,2.1,3.0)),
        ([(1,3,0), (2,5,-2), (4,6,-3)], (2.5,1.8,2.0)),
        ([(-2,7,2), (-3,6,4), (-4,5,5)], (1.5,2.1,1.4)),
        ([(2,8,3), (3,8,5), (3,6,6)], (1.6,2.0,1.4)),
    )),
)


def remove_previous_landmarks():
    # Restore exactly the cells touched by the two replaced landmarks, including their ground.
    original = former_stadium()
    material_pass(original)
    original.update(cells)
    cells.clear()
    clear_old_canopy(original)
    original.update(cells)
    cells.clear()
    north_gallery()
    east_orchard()
    for pos in list(cells):
        put(*pos, original.get(pos, "air"))


def rounded_plan(left, right, front, rear, radius):
    """Rounded ends and a shallow arc follow the seats; coordinates are in arena space."""
    plan = set()
    for x in range(left, right + 1):
        across = max(left + radius - x, x - right + radius, 0)
        for depth in range(front, rear + 1):
            along = max(front + radius - depth, depth - rear + radius, 0)
            if across**2 + along**2 <= radius**2:
                setback = max(0, abs(x) - 5)**2 // 68
                plan.add((x, -depth + setback))
    return plan


def perimeter(plan):
    return {(x, z) for x, z in plan
            if any((x + dx, z + dz) not in plan for dx, dz in ((0,1),(1,0),(0,-1),(-1,0)))}


DECK = rounded_plan(-17, 17, 40, 53, 4)
LOWER = rounded_plan(-16, 16, 43, 52, 3)
UPPER = rounded_plan(-14, 3, 45, 52, 3)
ROOF = rounded_plan(-16, 5, 43, 53, 4)


def pavilion_frame():
    for x, z in DECK:
        put(x, 12, z, CHERRY)
        put(x, 18, z, WHITE)
    for x, z in ROOF:
        put(x, 24, z, SLAB)
    for x, z in [(-13,-45), (-9,-50), (-4,-44), (4,-46), (13,-45), (11,-50)]:
        pillar(x, z, 13, 17, TIMBER)
    for x, z in [(-11,-47), (-7,-50), (0,-47)]:
        pillar(x, z, 19, 23, TIMBER)
    # An open stair from the right-hand promenade arrives beside the upper lounge.
    for step in range(6):
        x, y = 15 - step, 13 + step
        for z in (-47, -46, -45):
            pillar(x, z, 13, y - 1, BLUSH)
            put(x, y, z, QUARTZ_STAIR)
            for above in range(y + 1, 21):
                put(x, above, z, "air")


def pavilion_finish():
    for plan, base in [(LOWER, 12), (UPPER, 18)]:
        for x, z in perimeter(plan):
            front = (x, z + 1) not in plan
            if (front and ((base == 12 and 7 <= x <= 10) or (base == 18 and -3 <= x <= -1))
                    or (base == 12 and x >= 14 and -48 <= z <= -44)):
                continue
            # A solid rear wall makes the glass read against an interior from the court.
            back = (x, z - 1) not in plan
            for y in range(base + 1, base + 6):
                material = CLEAR if front else FROST
                if back:
                    material = TIMBER if x % 5 == 0 else BLUSH
                elif y == base + 1:
                    material = CHERRY
                elif x in (-12, -5, 2, 11) and front:
                    material = "stripped_cherry_log[axis=y]"
                put(x, y, z, material)
    for footprint, y in [(DECK, 18), (ROOF, 24)]:
        for x, z in perimeter(footprint):
            put(x, y - 1, z, CHERRY_SLAB)
    for x, z in ROOF - perimeter(ROOF):
        put(x, 24, z, CHERRY_SLAB if z > -49 else "quartz_slab[type=bottom,waterlogged=false]")
    # Panoramic terrace and a short timber brise-soleil on its eastern garden terrace.
    rail = {(x, z) for x, z in perimeter(DECK) if z >= -44 and -15 <= x <= 14}
    for x, z in rail:
        # A thin balustrade preserves the seated spectator's sightline over the fascia.
        adjacent = (("east", 1, 0), ("north", 0, -1), ("south", 0, 1), ("west", -1, 0))
        joints = [direction + "=" + str((x + dx, z + dz) in rail).lower()
                  for direction, dx, dz in adjacent]
        put(x, 19, z, "glass_pane[" + ",".join(joints) + ",waterlogged=false]")
    for x in (-12, -11, -10, -9, 5, 6, 7, 8):
        z = -42 if x < 0 else -41
        put(x, 19, z, TIMBER)
        put(x, 20, z, GREEN)
    # The timber screens at the ends meet the terrace, sheltering the lower waiting area.
    for x in (-15, -13, 12, 14):
        z = -44 if x < 0 else -43
        pillar(x, z, 13, 17, "stripped_cherry_log[axis=y]")
        put(x, 12, z + 1, BLUSH)
    for x in (-11, -10, -8, -7, -5):
        put(x, 19, -43, "cherry_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]")
    for x in (5, 8, 11, 14):
        pillar(x, -49, 19, 21, "stripped_cherry_log[axis=y]")
        for z in range(-50, -45):
            put(x, 22, z, CHERRY_SLAB)
    for x in range(5, 15):
        put(x, 21, -50, TIMBER)
    for x in (-10, -9, -8, 3, 4):
        put(x, 13, -47, "cherry_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]")
    for x, z in [(-10,-46), (-4,-49), (8,-49)]:
        put(x, 17, z, LIGHT)
    for x, z in [(-8,-48), (0,-49)]:
        put(x, 23, z, LIGHT)
    for x, z in [(6,-51), (7,-51), (8,-51), (4,-44), (5,-44)]:
        put(x, 19, z, BLUSH)
        put(x, 20, z, GREEN)


def limb(start, end):
    """Follow the sloping centre line with face-connected wood and successive grain directions."""
    pos = list(start)
    delta = [end[i] - start[i] for i in range(3)]
    put(*pos, WOOD + "[axis=y]")
    while tuple(pos) != end:
        moving = [i for i in range(3) if pos[i] != end[i]]
        axis = min(moving, key=lambda i: (abs(pos[i] - start[i]) + .5) / abs(delta[i]))
        pos[axis] += 1 if delta[axis] > 0 else -1
        put(*pos, WOOD + "[axis=" + "xyz"[axis] + "]")


def offset(root, point):
    return tuple(a + b for a, b in zip(root, point))


def garden_frame():
    for index, (root, paths) in enumerate(TREES):
        x, y, z = root
        for dx in range(-3, 4):
            for dz in range(-2, 3):
                if (dx / 3.2)**2 + (dz / 2.5)**2 < 1 and math.hypot(x + dx, z + dz) <= 54:
                    put(x + dx, y - 1, z + dz, "moss_block")
        for dx, dz in [(-2,0), (1,2), (2,-1)]:
            limb((x + dx, y, z + dz), (x, y + 1, z))
        if index == 1:
            limb((x + 1, y, z), (x, y + 4, z))
        for points, _ in paths:
            for start, end in zip(points, points[1:]):
                limb(offset(root, start), offset(root, end))


def blossom_canopies():
    # Generate wood again so the foliage will wrap the limbs without ever replacing a branch.
    garden_frame()
    wood = set(cells)
    cells.clear()
    # Curve the ends of the neighbouring roof panels back around the outer crowns.
    # The branch skeleton is already present in the staged model.
    for x in range(40, 55):
        for side, centre, reach in [(-1, 44, 3), (1, 50, 4)]:
            depth = max(0, reach - abs(x - centre) // 3)
            for edge in range(17, 17 + depth):
                z = side * edge
                height = roof_height(math.hypot(x, z), math.atan2(x, z))
                for y in range(height - 2, height + 2):
                    if (x, y, z) not in wood:
                        put(x, y, z, "air")
    for index, (root, paths) in enumerate(TREES):
        for branch, (points, radii) in enumerate(paths):
            cx, cy, cz = offset(root, points[-1])
            rx, ry, rz = radii
            for dx in range(-math.ceil(rx), math.ceil(rx) + 1):
                for dz in range(-math.ceil(rz), math.ceil(rz) + 1):
                    ripple = math.sin(dx * 1.3 + branch) * math.cos(dz * .9 - index)
                    for dy in range(-math.ceil(ry), math.ceil(ry) + 1):
                        # Tilt the lobes in different planes. Gaps between crowns show the forks.
                        slope = (.16 if index == 0 else -.11) * dx + .12 * dz
                        distance = (dx / rx)**2 + ((dy - slope) / ry)**2 + (dz / rz)**2
                        site = (cx + dx, cy + dy, cz + dz)
                        if distance < 1 + .17 * ripple and site not in wood:
                            put(*site, LEAVES)
        if index == 2:
            # Individual sprays hang from the southern crown, at different depths and heights.
            for dx, dy, dz, length in [(-6,9,-2,4), (-4,9,-4,3), (-6,7,4,3),
                                       (-3,7,5,4), (4,9,4,5), (3,9,6,3), (5,6,-3,2)]:
                x, y, z = offset(root, (dx, dy, dz))
                for drop in range(length):
                    if (x, y - drop, z) not in wood:
                        put(x, y - drop, z, LEAVES)
    for x, z in [(47,-11),(50,1),(46,12),(47,12)]:
        put(x, 13, z, "flowering_azalea")
    for x, z in [(45,-6),(45,6)]:
        for dz in (-1,0,1):
            put(x, 12, z + dz, "cherry_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]")


if __name__ == "__main__":
    stage = sys.argv[1]
    if stage == "pavilion_frame":
        remove_previous_landmarks()
        pavilion_frame()
        garden_frame()
        bounds = ([37, 12, 2], [112, 35, 74])
    elif stage == "pavilion_finish":
        pavilion_finish()
        bounds = ([38, 12, 2], [74, 25, 19])
    elif stage == "blossom_canopies":
        blossom_canopies()
        bounds = ([94, 12, 34], [112, 32, 76])
    else:
        raise SystemExit("Expected pavilion_frame, pavilion_finish or blossom_canopies")
    save(stage, {"mode": "local_repair", "name": stage, "scope": {"from": bounds[0], "to": bounds[1]},
                 "operations": operations()})
