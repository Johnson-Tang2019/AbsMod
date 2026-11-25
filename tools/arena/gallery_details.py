"""Refine the 0.11 arena in two v16 passes, keeping the playing floor open."""
import math
import sys

from design import (LIGHT, SHELL, SLAB, WHITE, cells, envelope, frame, operations, ornament,
                    pillar, polar_cells, put, radial_gap, roof, rose_finish, save,
                    seat_banks)

BLUSH = "white_terracotta"
TIMBER = "stripped_cherry_wood[axis=x]"
CHERRY_SLAB = "cherry_slab[type=bottom,waterlogged=false]"
FROST = "white_stained_glass"
PINK_GLASS = "pink_stained_glass"
LEAVES = "cherry_leaves[distance=1,persistent=true,waterlogged=false]"
GREEN = "azalea_leaves[distance=1,persistent=true,waterlogged=false]"


def former_stadium():
    for layer in (frame, envelope, roof, ornament, seat_banks):
        layer()
    base = cells.copy()
    cells.clear()
    rose_finish()
    base.update(cells)
    cells.clear()
    return base


def material_pass(base):
    for (x, y, z), block in base.items():
        r, angle = math.hypot(x, z), math.atan2(x, z)
        finish = block
        if block == "pink_concrete":
            # Segmented panels run between the white ribs, with a matte blush field and timber insets.
            if y < 14:
                finish = TIMBER
            elif r < 42 or r > 53:
                finish = "cherry_planks"
            elif radial_gap(r, angle, 16, math.pi/16) < 3.3:
                finish = WHITE
            elif r < 44.6 or 50.6 <= r < 52.4:
                finish = TIMBER
            else:
                finish = BLUSH
        elif block == PINK_GLASS and (r >= 47.5 or y >= 14):
            finish = PINK_GLASS if radial_gap(r, angle, 16) < 2.3 else FROST
        elif block in ("pink_terracotta", "pink_wool") and 35.5 < r < 48:
            finish = "cherry_planks" if x < 0 else BLUSH
        if finish != block:
            put(x, y, z, finish)


def clear_old_canopy(base):
    # Open the roof only over the four new landmarks. Old masts can still carry the adjacent bays.
    for x, y, z in base:
        r = math.hypot(x, z)
        if r < 40.5 or y < 13:
            continue
        if ((x > 0 and abs(z) <= 16) or (x < 0 and abs(z) <= 17)
                or (z < 0 and abs(x) <= 17) or (z > 0 and abs(x) <= 14)):
            put(x, y, z, "air")


def south_portal():
    # Three successive elliptical portal frames step up towards the outside of the stadium.
    for z, span, rise in [(40, 10, 11), (45, 12, 15), (50, 14, 18)]:
        for x in range(-span, span+1):
            crown = 12 + round(rise*math.sqrt(max(0, 1-(x/span)**2)))
            next_x = min(span, abs(x)+1)
            neighbouring = 12 + round(rise*math.sqrt(max(0, 1-(next_x/span)**2)))
            bottom = min(crown-1, neighbouring)
            for depth in (z, z+1):
                pillar(x, depth, bottom, crown, WHITE if depth==z else TIMBER)
            if abs(x)==span:
                pillar(x, z, 6, 12, WHITE)
                pillar(x, z+1, 6, 12, TIMBER)
            if x%5==0:
                put(x, bottom, z, LIGHT)
        for sign in (-1, 1):
            for offset in range(1, 4):
                put(sign*(span-offset), 11, z, BLUSH)
    # Side glazing connects the arches while the centre remains open to the sky.
    for sign in (-1, 1):
        for z in range(40, 52):
            x = sign*(10+(z-40)*4//10)
            pillar(x, z, 12, 15, FROST if z%4 else TIMBER)


def north_gallery():
    # A glazed observation gallery runs behind the front row of columns. Its folded roof
    # rises at each end and overhangs a central oval window facing the combat floor.
    for x in range(-17, 18):
        for z in range(-52, -39):
            if math.hypot(x, z)>55:
                continue
            top = 23 + abs(x)//2 + (-z-40)//5
            put(x, top, z, WHITE if (z in (-52, -40) or abs(x)%8==0) else BLUSH)
            put(x, top-1, z, TIMBER if z in (-52, -40) else SHELL)
            if abs(x)>14 and z in (-49, -43):
                pillar(x, z, 12, top-2, WHITE)
            if abs(x)<=15 and -50<=z<=-43:
                put(x, 16, z, "cherry_planks")
                if z in (-49, -43) and abs(x)>=6:
                    pillar(x, z, 17, 21, WHITE if x%5==0 else FROST)
            if abs(x) in (6, 14) and z in (-49, -43):
                pillar(x, z, 12, top-2, TIMBER)
    for x in range(-6, 7):
        for y in range(14, 25):
            ellipse = (x/6.2)**2+((y-19)/5.1)**2
            if .57<=ellipse<=1.1:
                put(x, y, -41, WHITE)
                put(x, y, -42, LIGHT if y<18 else TIMBER)
    # Climb the west end of the gallery from the concourse without interrupting its through route.
    for step in range(10):
        for z in (-48, -47):
            x = -17+step
            top = 12+step//2
            pillar(x, z, 12, top-1, BLUSH)
            put(x, top, z, SLAB if step%2==0 else WHITE)
            for y in range(top+1, top+4):
                put(x, y, z, "air")


def east_orchard():
    # Individual planted islands leave the inner promenade open and reveal tree crowns from the floor.
    for x, z, height in [(49, -11, 8), (51, 0, 10), (48, 11, 7)]:
        for dx in range(-2, 3):
            for dz in range(-2, 3):
                if abs(dx)+abs(dz)>3:
                    continue
                put(x+dx, 12, z+dz, SLAB if max(abs(dx), abs(dz))==2 else "moss_block")
        pillar(x, z, 13, 12+height, "cherry_log[axis=y]")
        for dx, dz, dy, rad in [(-2, -1, -1, 2), (2, 1, 0, 2), (0, 0, 2, 3)]:
            branch = 12+height+dy
            for i in range(min(0, dx), max(0, dx)+1):
                put(x+i, branch-1, z, "cherry_log[axis=x]")
            for j in range(min(0, dz), max(0, dz)+1):
                put(x+dx, branch-1, z+j, "cherry_log[axis=z]")
            for a in range(-rad, rad+1):
                for b in range(-rad, rad+1):
                    for c in (-1, 0, 1):
                        if a*a+b*b+2*c*c<=rad*rad+1:
                            put(x+dx+a, branch+c, z+dz+b, LEAVES)
    # Layer the edge with shrubs and sheltered slatted benches.
    for z in (-15, -7, 6, 15):
        for dz in (-1, 0, 1):
            put(46, 12, z+dz, "cherry_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]")
            put(52, 12, z+dz, GREEN)
    for x, z, r, _ in polar_cells(41, 53.5):
        if x<=0 or abs(z)>16:
            continue
        if abs(z) in (7, 8, 15, 16):
            put(x, 19+round((r-41)/6), z, TIMBER if abs(z)%2 else CHERRY_SLAB)
            if r>=51:
                pillar(x, z, 12, 18+round((r-41)/6), WHITE)


def west_rooflights():
    # Three sawtooth clerestories catch the light from one direction. Each has an
    # opaque timber slope and a vertical glazed return above the promenade.
    for x, z, r, _ in polar_cells(41, 54.7):
        if x>=0 or abs(z)>17:
            continue
        bay = (z+17)//12
        along = (z+17)%12
        high = 18+along//2+round((r-41)/5)+(1 if bay==1 else 0)
        if along>=10:
            pillar(x, z, high-4, high-1, FROST if 42<r<53 else WHITE)
        finish = WHITE if along in (0, 11) or r<42 else (TIMBER if along%4<=1 else BLUSH)
        put(x, high, z, finish)
        if along==11 or r<42:
            put(x, high+1, z, SLAB)
        if along in (0, 10) and r>=51:
            pillar(x, z, 12, high-1, WHITE)
        if along==9 and 42<r<46:
            put(x, high-1, z, LIGHT)


if __name__ == "__main__":
    stage = sys.argv[1]
    baseline = former_stadium()
    if stage == "muted_materials":
        material_pass(baseline)
        title = "Replace saturated panels with blush ceramic, timber and frosted glass"
    elif stage == "four_galleries":
        clear_old_canopy(baseline)
        south_portal()
        north_gallery()
        east_orchard()
        west_rooflights()
        title = "Frame the entrances and give the side galleries distinct rooflines"
    else:
        raise SystemExit("Expected muted_materials or four_galleries")
    save(stage, {"mode": "local_repair", "name": title,
                "scope": {"from": [0, 4, 0], "to": [112, 37, 112]}, "operations": operations()})
