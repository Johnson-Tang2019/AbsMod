"""Generate editable v16 stages for the 1.21.1 Court of Falling Blossoms."""
import json
import math
import sys
from pathlib import Path

OUT = Path("build/arena-design")
HALF = 56
SIZE = {"x": 113, "y": 38, "z": 113}
BASE = "polished_deepslate"
WOOD = "dark_oak_planks"
CHERRY = "cherry_planks"
POST = "stripped_cherry_log[axis=y]"
WHITE = "smooth_quartz"
TILE = "deepslate_tiles"
LEAF = "cherry_leaves[distance=1,persistent=true,waterlogged=false]"
LAMP = "lantern[hanging=true,waterlogged=false]"
operations = []


def fill(x, y, z, u, v, w, block):
    low = [min(x, u) + HALF, min(y, v), min(z, w) + HALF]
    high = [max(x, u) + HALF, max(y, v), max(z, w) + HALF]
    if any(a < 0 or b >= SIZE[k] for a, b, k in zip(low, high, "xyz")):
        raise ValueError((low, high))
    operations.append({"op": "fill", "from": low, "to": high, "block": "minecraft:" + block})


def dot(x, y, z, block):
    fill(x, y, z, x, y, z, block)


def side(which, along, depth):
    return [(along, -depth), (depth, along), (-along, depth), (-depth, -along)][which]


def bay(which, a, y, d, b, top, end, block):
    x, z = side(which, a, d)
    u, w = side(which, b, end)
    fill(x, y, z, u, top, w, block)


def stairs(direction, wood="cherry"):
    return f"{wood}_stairs[facing={direction},half=bottom,shape=straight,waterlogged=false]"


# Entries specify the centre, half-spans, platform height and main eave height.
# The side stages have distinct silhouettes: an elongated east music hall and a west belvedere.
HALLS = [(0, -46, 14, 6, 10, 20), (0, 47, 9, 5, 5, 17),
         (47, 0, 5, 12, 10, 18), (-47, 0, 5, 7, 10, 19)]
TOWERS = [(x, z, 5, 5, 10, 18) for x in (-44, 44) for z in (-44, 44)]


def posts(x, z, a, b, deck, eave):
    for dx in {-a + 2, a - 2}:
        for dz in {-b + 2, b - 2}:
            px, pz = x + dx, z + dz
            fill(px-1, deck, pz-1, px+1, deck+1, pz+1, "quartz_bricks")
            fill(px, deck+2, pz, px, eave-2, pz, POST)
            fill(px-1, eave-2, pz, px+1, eave-2, pz, WOOD)
            fill(px, eave-2, pz-1, px, eave-2, pz+1, WOOD)
    for dz in (-b+2, b-2):
        fill(x-a, eave-1, z+dz, x+a, eave-1, z+dz, WOOD)
    for dx in (-a+2, a-2):
        fill(x+dx, eave-1, z-b, x+dx, eave-1, z+b, WOOD)


def frame():
    for y, r, material in [(0,47,BASE),(1,49,"tuff_bricks"),(2,51,BASE),
                           (3,53,"polished_tuff"),(4,54,"chiseled_tuff_bricks"),(5,54,WHITE)]:
        for z in range(-r, r+1):
            span = min(r, 2*r-10-abs(z))
            fill(-span, y, z, span, y, z, material)
    for x,z,a,b,deck,eave in HALLS + TOWERS:
        if deck > 5:
            fill(x-a,6,z-b,x+a,deck-1,z+b,"tuff_bricks")
            fill(x-a,deck,z-b,x+a,deck,z+b,CHERRY)
        posts(x,z,a,b,deck,eave)
    # A second, narrower open storey carries the high northern roof.
    posts(0,-46,9,4,23,30)
    for which in range(4):
        for sign in (-1,1):
            for a in (18,25,32,39):
                for d in (46,50):
                    bay(which, sign*a,6,d,sign*a,14,d,POST)
                bay(which,sign*a,14,45,sign*a,14,51,WOOD)
            for d in (46,50):
                bay(which,sign*16,14,d,sign*39,14,d,WOOD)


def envelope():
    for which in range(4):
        facing = ("north","east","south","west")[which]
        # Five accessible tiers. Radial stair aisles also reach the continuous raised promenade.
        for tier in range(5):
            d = 35+2*tier
            for sign in (-1,1):
                for a,b in ((5,17),(21,33)):
                    bay(which,sign*a,6,d,sign*b,6+tier,d+1,
                        "stripped_cherry_wood[axis="+("x" if which%2==0 else "z")+"]" if tier%2 else CHERRY)
                bay(which,sign*18,6+tier,d,sign*20,6+tier,d,stairs(facing))
                bay(which,sign*18,6,d+1,sign*20,6+tier,d+1,CHERRY)
            # North/east/west approaches rise to the pavilion decks; south stays at portal level.
            if which != 2:
                bay(which,-4,6+tier,d,4,6+tier,d,stairs(facing))
                bay(which,-4,6,d+1,4,6+tier,d+1,WHITE)
        for sign in (-1,1):
            start = 10 if which == 2 else 5
            bay(which,sign*start,6,45,sign*44,9,51,"tuff_bricks")
            bay(which,sign*start,10,45,sign*44,10,51,"smooth_quartz")
            # Alternating masonry panels, open windows, and hedge beds behind the gallery.
            for a in (17,24,31,38):
                bay(which,sign*a,6,52,sign*(a+6),10,52,"stone_bricks")
                bay(which,sign*a,11,52,sign*(a+6),11,52,"quartz_bricks")
                bay(which,sign*a,11,52,sign*a,16,52,"polished_tuff")
                bay(which,sign*(a+1),12,52,sign*(a+5),12,52,LEAF)
                bay(which,sign*(a+2),13,52,sign*(a+4),13,52,LEAF)
                bay(which,sign*a,16,51,sign*a,16,53,BASE)
                bay(which,sign*a,2,53,sign*a,5,53,BASE)
        for a in (-32,-25,-18,18,25,32):
            bay(which,a,13,45,a,13,46,"stripped_dark_oak_wood[axis=y]")
    # Keep the southern approach walkable at floor level between the two wings.
    for sign in (-1,1):
        fill(sign*8,6,48,sign*12,8,51,"quartz_bricks")
        fill(sign*6,14,46,sign*9,15,48,CHERRY)
    fill(-11,16,47,11,16,47,"dark_oak_planks")
    fill(-9,13,47,9,13,47,CHERRY)
    for x,z,a,b,deck,eave in TOWERS:
        fill(x-3,21,z-3,x+3,21,z+3,CHERRY)
        posts(x,z,4,4,21,26)
    for dx in (-11,-6,6,11):
        fill(dx,11,-42,dx,18,-42,POST)
    fill(-9,23,-50,9,23,-42,CHERRY)
    # Gold screen and timber back wall, visible through the open columns.
    for x in range(-12,13):
        fill(x,11,-50,x,18,-50,POST if x%4==0 else "white_terracotta")
        if x%4:
            dot(x,15,-50,"pink_terracotta")
    for x in (-47,47):
        for z in (-4,4):
            fill(x,11,z,x,14,z,WOOD)
            fill(x,15,z-1,x,15,z+1,CHERRY)


def hip(x,z,a,b,lip):
    # Curled corners rise above the central eave. A contrasting soffit lies below each tile course.
    for dz in range(-b,b+1):
        run = None
        for dx in range(-a,a+2):
            state = None
            if dx <= a:
                inset = min(a-abs(dx), b-abs(dz))
                tip = max(0, 3-(a-abs(dx))-(b-abs(dz)))
                height = lip + (inset+1)//2 + (tip+1)//2
                slab = inset%2 == 0
                material = "deepslate_tile_slab[type=bottom,waterlogged=false]" if slab else TILE
                if inset == min(a,b): material = "oxidized_cut_copper"
                state = (height,material)
            if state == run: continue
            if run is not None:
                y,mat = run
                fill(x+start,y-1,z+dz,x+dx-1,y-1,z+dz,WOOD)
                fill(x+start,y,z+dz,x+dx-1,y,z+dz,mat)
            run, start = state, dx
    for dx in (-a,a):
        for dz in (-b,b):
            dot(x+dx,lip+3,z+dz,"lightning_rod[facing=up,powered=false,waterlogged=false]")


def roofs():
    for x,z,a,b,deck,eave in HALLS + TOWERS:
        hip(x,z,a+2,b+2,eave)
    hip(0,-46,11,6,30)
    for x,z,*_ in TOWERS:
        hip(x,z,5,5,26)
        dot(x,30,z,"chiseled_quartz_block")
        dot(x,31,z,"lightning_rod[facing=up,powered=false,waterlogged=false]")
    for sign in (-1,1):
        hip(sign*28,-48,13,4,15)
        hip(sign*28,48,13,4,15)
        hip(-48,sign*28,4,13,15)
        hip(48,sign*28,4,13,15)
    # Tall finials emphasize the ridge of the principal hall, not every facade.
    for x in (-5,5):
        fill(x,34,-46,x,35,-46,"waxed_cut_copper")
        dot(x,36,-46,"lightning_rod[facing=up,powered=false,waterlogged=false]")


def orchard(x,z,flip):
    fill(x,6,z,x,12,z,"cherry_log[axis=y]")
    for dx,dz,height,r in [(-3*flip,-1,13,3),(3*flip,1,14,3),(flip,0,16,3)]:
        fill(x,11,z,x+dx,11,z,"cherry_log[axis=x]")
        fill(x+dx,11,z,x+dx,height,z+dz,"cherry_log[axis=y]")
        for dy in (-1,0,1):
            width = r - abs(dy)
            for k in range(-width,width+1):
                span = width - max(0,abs(k)-1)
                fill(x+dx-span,height+dy,z+dz+k,x+dx+span,height+dy,z+dz+k,LEAF)


def ornament():
    # Merge horizontal runs so the large, flush inlay remains cheap to regenerate.
    for z in range(-35,36):
        run = None
        for x in range(-35,37):
            r = math.hypot(x,z)
            mat = None
            if x <= 35 and r <= 35:
                if abs(r-33)<.65 or abs(r-21)<.52:
                    mat = "pink_terracotta" if min(abs(x),abs(z))>1 else "pearlescent_froglight[axis=y]"
                elif 20<r<33 and min(abs(x),abs(z))==1:
                    mat = "white_terracotta"
                for i in range(5):
                    angle = i*2*math.pi/5-math.pi/2
                    along = x*math.cos(angle)+z*math.sin(angle)
                    cross = abs(-x*math.sin(angle)+z*math.cos(angle))
                    ellipse = ((along-9.5)/8.5)**2+(cross/5.4)**2
                    if ellipse<=1 and not (along>15.1 and cross<(along-15.1)*.53):
                        mat = "pink_concrete" if ellipse>.73 else ("pink_wool" if cross>1 else "white_terracotta")
                if r<3: mat = "pearlescent_froglight[axis=y]" if r<1.5 else "honeycomb_block"
            if run == mat: continue
            if run: fill(start,5,z,x-1,5,z,run)
            run,start = mat,x
    for which in range(4):
        for sign in (-1,1):
            for a in (18,25,32,39):
                bay(which,sign*a,12,45,sign*a,12,45,LAMP)
            for a in (12,28):
                bay(which,sign*a,5,33,sign*a,5,33,"pearlescent_froglight[axis=y]")
    for x in (-35,35):
        for z in (-35,35):
            orchard(x,z,1 if x*z>0 else -1)
    for x,z,a,b,deck,eave in HALLS + TOWERS:
        for dx in (-a+2,a-2):
            for dz in (-b+2,b-2):
                outward = 1 if dz>0 else -1
                dot(x+dx,eave-3,z+dz+outward,LAMP)
    # Drum pair on the east music platform and a lit alcove on the west.
    for z in (-6,6):
        fill(48,11,z-1,48,11,z+1,WOOD)
        fill(48,12,z-1,49,13,z+1,"stripped_mangrove_wood[axis=x]")
        fill(47,12,z-1,47,13,z+1,"stripped_birch_log[axis=x]")
    fill(-49,11,-2,-49,12,2,"quartz_bricks")
    for z in (-2,0,2):
        dot(-49,13,z,"ochre_froglight[axis=y]")
    for x in (-5,5):
        fill(x,12,46,x,13,46,WOOD)
        dot(x,11,46,LAMP)


def save(name, data):
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / (name+".json")).write_text(json.dumps(data,indent=2),encoding="utf-8")


if __name__ == "__main__":
    stage = sys.argv[1]
    passes = {"envelope":envelope, "roof":roofs, "ornament":ornament}
    if stage == "frame":
        frame()
        brief = {"name":"Grand Court of Falling Blossoms","source_mode":"text","dimensions":SIZE,
                 "front_direction":"south","hard_constraints":["One floating arena","Open combat radius 34","Ground level 70"],
                 "must_preserve":[{"feature":f,"priority":5,"evidence":e} for f,e in [
                     ("Open court and vertically layered enclosing buildings","Cataclysm Burning Arena eight measured NBT modules"),
                     ("Alternating enclosure bays, raised end piers and hedge beds","Twilight Forest Naga Courtyard wall and hedge modules"),
                     ("South gate opposite high north hall","Existing Ab's Mod portal arrival axis")]],
                 "massing":{"floor_count":2,"description":"A chamfered plinth carries a ring of towers and stepped halls",
                            "major_volumes":["court","north hall","gate","east music hall","west belvedere","corner towers"],"symmetry":"bilateral_x"},
                 "roof":{"system":"hipped tile roofs with raised corners","ridge_axis":"x","tiers":2,"pitch":"moderate","overhang":2,"silhouette_notes":"north ridge dominates"},
                 "facade_rhythm":{"primary_bays":4,"vertical_layers":3,"repeating_modules":["open timber colonnade","hedge and stone piers"],"notes":"roofed promenade behind terraces"},
                 "program":{"require_interior":False,"spaces":["combat floor","grandstands","promenade","halls"],"circulation":"South approach and paired stair aisles rise to the promenades"},
                 "palette_roles":{"foundation":"minecraft:tuff_bricks","floor":"minecraft:smooth_quartz","structure":"minecraft:cherry_planks","roof":"minecraft:deepslate_tiles"},
                 "uncertainties":["The two references inform layout and rhythm. All block arrangements and dimensions are original."]}
        save("brief",brief)
        save(stage,{"name":brief["name"],"dimensions":SIZE,"stage":stage,"strict_block_states":True,
                    "workflow":{"mode":"staged","refinement_passes":list(passes)},
                    "quality":{"require_interior":False,"require_roof_detail":True,"front_direction":"south"},"operations":operations})
    elif stage == "gate_lintel":
        for x in (-4,4):
            fill(x,6,47,x,10,47,POST)
            dot(x,6,47,"chiseled_quartz_block")
            fill(x,11,46,x,11,48,WOOD)
        fill(-5,10,47,5,10,47,WOOD)
        fill(-6,11,47,6,11,47,CHERRY)
        for x in (-6,6):
            dot(x,12,47,"deepslate_tile_slab[type=bottom,waterlogged=false]")
            dot(x,10,47,LAMP)
        save(stage,{"mode":"local_repair","name":"Frame the seven-wide return portal beneath the gatehouse",
                    "scope":{"from":[50,6,102],"to":[62,12,104]},"operations":operations})
    elif stage == "pavilion_screens":
        # A full-height blossom crest and layered sill give the north hall a readable front.
        fill(-11,11,-49,11,11,-49,"quartz_bricks")
        for tx in (-9,-5,5,9):
            fill(tx,12,-49,tx,17,-49,POST)
            for height in (13,16):
                fill(tx-1,height,-49,tx+1,height,-49,WOOD)
        crest = ("..P.P..",".PP.PP.","PPPLPPP",".PLGLP.","..PLP..",".PP.PP.","..P.P..")
        fill(-4,11,-49,4,19,-49,"white_terracotta")
        for row,pattern in enumerate(crest):
            for col,cell in enumerate(pattern):
                if cell != ".":
                    dot(col-3,18-row,-48,{"P":"pink_concrete","L":"pearlescent_froglight[axis=y]","G":"gold_block"}[cell])
        for x in (-5,5):
            fill(x,12,-48,x,18,-48,"stripped_dark_oak_log[axis=y]")
            fill(x,18,-40,x,19,-40,"dark_oak_planks")
            dot(x,17,-40,LAMP)
        # Lit paper screens have timber mullions. The outer galleries and stairs stay open.
        for x,z,a,b,deck,eave in [(0,-46,7,2,23,30)]+[(x,z,2,2,21,26) for x,z,*_ in TOWERS]:
            for dx in range(-a,a+1):
                for dz in range(-b,b+1):
                    if abs(dx)!=a and abs(dz)!=b: continue
                    mullion = abs(dx)==a and abs(dz)==b or (a>2 and abs(dz)==b and dx%3==0)
                    fill(x+dx,deck+1,z+dz,x+dx,eave-2,z+dz, POST if mullion else "pearlescent_froglight[axis=y]")
                    dot(x+dx,eave-3,z+dz,WOOD if mullion else "white_terracotta")
        save(stage,{"mode":"local_repair","name":"Articulate lantern chambers and the north hall blossom screen",
                    "scope":{"from":[10,11,6],"to":[102,28,102]},"operations":operations})
    elif stage == "eave_caps":
        # A bottom slab leaves half a block of visible air under each vertical finial.
        roofs_to_cap = [(x,z,a+2,b+2,eave) for x,z,a,b,_,eave in HALLS + TOWERS]
        roofs_to_cap += [(0,-46,11,6,30)] + [(x,z,5,5,26) for x,z,*_ in TOWERS]
        for sign in (-1,1):
            roofs_to_cap += [(sign*28,-48,13,4,15),(sign*28,48,13,4,15),
                             (-48,sign*28,4,13,15),(48,sign*28,4,13,15)]
        for x,z,a,b,lip in roofs_to_cap:
            for dx in (-a,a):
                for dz in (-b,b):
                    dot(x+dx,lip+2,z+dz,TILE)
        save(stage,{"mode":"local_repair","name":"Seat copper finials on solid tile caps",
                    "scope":{"from":[2,17,2],"to":[110,32,110]},"operations":operations})
    else:
        passes[stage]()
        save(stage,{"mode":"refinement_pass","name":stage,"category":stage,
                    "intent":[{"envelope":"Five tiers, stair aisles, gallery piers and planted walls",
                               "roof":"Eight main pavilions and connecting eaves frame an unroofed combat floor",
                               "ornament":"Flush blossom inlay, lantern bays, corner cherry groves and independent east and west stages"}[stage]],
                    "operations":operations})
