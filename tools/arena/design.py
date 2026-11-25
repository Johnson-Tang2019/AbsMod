"""Build the editable v16 arena stages in build/arena-design."""
import json
import math
import sys
from pathlib import Path

out = Path("build/arena-design")
out.mkdir(parents=True, exist_ok=True)
stage = sys.argv[1]
ops = []
size = {"x": 49, "z": 49, "y": 17}
stone = "polished_deepslate"
pink = "cherry_planks"
floor = "smooth_quartz"
pillar = "stripped_cherry_log[axis=y]"
roof = "deepslate_tiles"


def fill(x, y, z, u, v, w, material):
    ops.append({"op": "fill", "from": [x + 24, y, z + 24], "to": [u + 24, v, w + 24], "block": "minecraft:" + material})


def point(x, y, z, material):
    fill(x, y, z, x, y, z, material)


def octagon(radius, y, material):
    for z in range(-radius, radius + 1):
        w = min(radius, radius + radius * 2 // 3 - abs(z))
        fill(-w, y, z, w, y, z, material)


def save(name, value):
    (out / (name + ".json")).write_text(json.dumps(value, indent=2), encoding="utf-8")


if stage == "frame":
    # An arena has an open core. The shell is built as an octagon around that void.
    for y, r, material in [(0, 17, stone), (1, 19, "tuff_bricks"), (2, 21, stone),
                           (3, 22, "polished_tuff"), (4, 23, "cherry_wood[axis=x]"), (5, 23, floor)]:
        octagon(r, y, material)
    for z in [-20, -16]:
        for x in [-11, -4, 4, 11]:
            fill(x, 6, z, x, 11, z, pillar)
        fill(-12, 11, z, 12, 11, z, "dark_oak_planks")
    for x in [-4, 4]:
        fill(x, 6, 19, x, 12, 19, pillar)
    fill(-6, 12, 19, 6, 12, 19, stone)
    for x in [-1, 1]:
        for z in [-11, 0, 11]:
            fill(x * 21, 6, z, x * 21, 9, z, pillar)
    brief = {"name":"Court of Falling Blossoms","source_mode":"text","dimensions":size,"front_direction":"south",
             "hard_constraints":["One floating arena","Flat unobstructed fighting circle","No surrounding land"],
             "must_preserve":[{"feature":a,"priority":5,"evidence":b} for a,b in [
                 ("Open center with separate raked stands","Measured Trydar arena central section"),
                 ("Deep eaves borne by timber columns","Sakura studies roof and post section"),
                 ("A south gate opposite a northern covered gallery","Player approach axis")]],
             "massing":{"floor_count":1,"description":"Chamfered floating stone plinth, raked sides and north pavilion",
                        "major_volumes":["court","gate","gallery"],"symmetry":"bilateral_x"},
             "roof":{"system":"upturned pavilion eaves","ridge_axis":"x","tiers":1,"pitch":"moderate","overhang":2,
                     "silhouette_notes":"side stands stay below the ridge"},
             "facade_rhythm":{"primary_bays":3,"vertical_layers":2,"repeating_modules":["cherry columns"],"notes":"open sightlines"},
             "program":{"require_interior":False,"spaces":["court","stands","gallery"],"circulation":"South landing branches to both stands"},
             "palette_roles":{"foundation":"minecraft:polished_deepslate","floor":"minecraft:smooth_quartz","structure":"minecraft:cherry_planks","roof":"minecraft:deepslate_tiles"},
             "uncertainties":["Dimensions and materials are first-release design choices."]}
    save("brief", brief)
    save("frame", {"name":brief["name"],"dimensions":size,"stage":"frame","strict_block_states":True,
                   "workflow":{"mode":"staged","refinement_passes":["envelope","roof","ornament"]},
                   "quality":{"require_interior":False,"require_roof_detail":True,"front_direction":"south"},"operations":ops})
elif stage == "envelope":
    for sign in [-1, 1]:
        for row in range(3):
            for z1, z2 in [(-13,-2),(2,13)]:
                a,b=sorted((sign*(16+row*2),sign*(17+row*2)))
                fill(a,6,z1,b,6+row,z2,pink if row!=1 else "stripped_cherry_wood[axis=z]")
        for z in [-12,-6,0,6,12]:
            fill(sign*22,6,z,sign*22,9,z,stone)
        fill(sign*22,9,-12,sign*22,9,12,pink)
    fill(-12,6,-21,12,7,-20,pink)
    for x in [-12,12]:
        fill(x,6,-20,x,8,-16,"cherry_planks")
elif stage == "roof":
    for offset in range(-4,5):
        y=15-abs(offset)//2
        fill(-13,y,-18+offset,13,y,-18+offset,roof)
        for sign in [-1,1]:
            point(sign*14,y+1,-18+offset,stone)
    fill(-15,16,-18,15,16,-18,"polished_blackstone")
    for y,w in [(12,7),(13,6)]:
        fill(-w,y,18,w,y,20,"dark_oak_planks" if y==12 else roof)
    for x in [-8,8]:
        fill(x,13,18,x,14,20,roof)
    fill(-6,10,19,6,10,19,pink)
    for x in [-11,-4,4,11]:
        fill(x-1,12,-21,x+1,12,-15,"dark_oak_planks")
elif stage == "ornament":
    # A blossom mosaic lies flush with the combat floor; stairs open both aisles.
    for z in range(-15,16):
        for x in range(-15,16):
            r=math.hypot(x,z)
            a=math.atan2(z,x)+math.pi/2
            petal=3.8+2.9*math.cos(5*a)
            if 13.65<r<14.5: point(x,5,z,"pink_terracotta")
            elif r<1.7: point(x,5,z,"yellow_terracotta")
            elif r<=petal: point(x,5,z,"pink_concrete" if r>petal-1.3 else "pink_terracotta")
    for sign in [-1,1]:
        for row in range(3):
            x=sign*(16+row*2)
            face="east" if sign==1 else "west"
            fill(x,6+row,-1,x,6+row,1,"cherry_stairs[facing="+face+",half=bottom,shape=straight,waterlogged=false]")
            fill(x+sign,6,-1,x+sign,6+row,1,pink)
    for sign in [-1,1]:
        for z in [-12,-6,0,6,12]:
            point(sign*22,10,z,"lantern[hanging=false,waterlogged=false]")
        for x in [4,11]:
            point(sign*x,11,-15,"lantern[hanging=true,waterlogged=false]")
        point(sign*5,11,18,"lantern[hanging=true,waterlogged=false]")
    # Paired clumps of pale blossom crown the clipped corners, clear of the central floor.
    for x in [-16,16]:
        for z in [-16,16]:
            fill(x,6,z,x,9,z,pillar)
            for y,r in [(9,2),(10,3),(11,2),(12,1)]:
                for dz in range(-r,r+1):
                    w=r-max(0,abs(dz)-1)
                    fill(x-w,y,z+dz,x+w,y,z+dz,"cherry_leaves[distance=1,persistent=true,waterlogged=false]")
elif stage == "gallery_eaves":
    # Two stair courses per pitch give the gallery a thinner lip when seen from the court.
    for offset in range(-4,5):
        y=15-abs(offset)//2
        fill(-13,y-1,-18+offset,13,y-1,-18+offset,"dark_oak_planks")
        if offset==0:
            continue
        material=("deepslate_tile_slab[type=bottom,waterlogged=false]" if abs(offset)%2==0 else
                  "deepslate_tile_stairs[facing="+("south" if offset<0 else "north")+",half=bottom,shape=straight,waterlogged=false]")
        fill(-13,y,-18+offset,13,y,-18+offset,material)
    save(stage, {"mode":"local_repair","name":"Articulate the gallery roof pitches","scope":{"from":[11,12,2],"to":[37,15,10]},"operations":ops})
elif stage == "court_inlay":
    # Five round, notched petals radiate from the centre; all materials remain flush.
    for z in range(-15,16):
        for x in range(-15,16):
            radius=math.hypot(x,z)
            material=floor
            if 13.2<=radius<=14.7:
                material="pink_terracotta" if min(abs(x),abs(z))>1 else "chiseled_quartz_block"
            for petal in range(5):
                angle=(petal*2*math.pi/5)-math.pi/2
                long=x*math.cos(angle)+z*math.sin(angle)
                cross=abs(-x*math.sin(angle)+z*math.cos(angle))
                ellipse=((long-5)/4.3)**2+(cross/2.8)**2
                if ellipse<=1 and not (long>7.25 and cross<.7):
                    material="pink_concrete" if ellipse>.6 else "pink_wool"
                    if cross<.6 and long<4: material="white_terracotta"
            if radius<2: material="honeycomb_block" if radius<1 else "yellow_terracotta"
            point(x,5,z,material)
    save(stage, {"mode":"local_repair","name":"Five notched petals and a continuous boundary inlay","scope":{"from":[9,5,9],"to":[39,5,39]},"operations":ops})
else:
    raise ValueError(stage)
if stage in ("envelope","roof","ornament"):
    save(stage, {"mode":"refinement_pass","name":stage,"category":stage,"intent":[
        {"roof":"Supported upturned gallery and gateway eaves","envelope":"Stepped stands preserve center sightlines",
         "ornament":"Open aisles, flush blossom mosaic, low corner blossoms and lantern spacing"}[stage]],"operations":ops})
