"""Build curved spirit surfaces from the preserved first rig; never modifies texture pixels."""
from pathlib import Path
import json, math
ROOT=Path(__file__).resolve().parents[2]
m=json.loads((ROOT/"art/blade_spirit/box_model_v1.json").read_text())
def tex(tile,u,v):
 return [(tile%4+.014+.972*u)/4,(tile//4+.014+.972*v)/4]
def rings(bone,profile,front,back=None,sides=16,uv_range=(0,1),flat=None):
 # profile entries are y, radius X, radius Z, center X, center Z.
 quads=[];points=[]
 for y,rx,rz,cx,cz in profile:
  points.append([[cx+rx*math.sin(i*math.tau/sides),y,cz-rz*math.cos(i*math.tau/sides)] for i in range(sides)])
 def uv(p,i):
  v=(p[1]-profile[0][0])/(profile[-1][0]-profile[0][0]);u=(math.sin((i+.0)*math.tau/sides)+1)/2
  tile=front if math.cos(i*math.tau/sides)>=-1e-6 else (back if back is not None else front)
  if tile!=front:u=1-u
  if flat:return flat
  return tex(tile,uv_range[0]+u*(uv_range[1]-uv_range[0]),v)
 for j in range(len(points)-1):
  for i in range(sides):
   k=(i+1)%sides;ps=[points[j][i],points[j+1][i],points[j+1][k],points[j][k]]
   # Use the face's side consistently to prevent UV discontinuity at the silhouette.
   tile=front if math.cos((i+.5)*math.tau/sides)>=0 else (back if back is not None else front)
   coords=[]
   for p,index in zip(ps,[i,i,k,k]):
    v=(p[1]-profile[0][0])/(profile[-1][0]-profile[0][0]);u=(math.sin(index*math.tau/sides)+1)/2
    if tile!=front:u=1-u
    coords.append(flat or tex(tile,uv_range[0]+u*(uv_range[1]-uv_range[0]),v))
   quads.append(dict(vertices=ps,uv=coords))
 for j,reverse in [(0,False),(-1,True)]:
  center=[profile[j][3],profile[j][0],profile[j][4]]
  for i in range(sides):
   a,b=points[j][i],points[j][(i+1)%sides]
   if reverse:a,b=b,a
   quads.append(dict(vertices=[center,a,b,b],uv=[flat or tex(back if back is not None else front,.5,.05)]*4))
 bone['cubes']=[];bone['quads']=quads

def profile(rows):return [(y,rx,rz,0,0) for y,rx,rz in rows]
head=m['bones'][0];body=m['bones'][1]
rings(head,profile([(-8,.9,.8),(-7.7,2.5,1.9),(-6.8,3.65,2.6),(-5,3.95,2.8),(-3,3.8,2.65),(-1.3,3.1,2.2),(-.25,1.9,1.65),(0,1.2,1.3)]),0,2,24)
for b in head['children']:
 name=b['name']
 if name.startswith('back_hair_'):
  i=int(name[-1]);b['origin']=[(-2.6,0,2.6)[i],-6.7,2.0];b['rotation']=[.01,0,0]
  rings(b,[(0,1.3,.8,0,0),(3,1.6,1,0,.45),(8,1.6,.8,(-.4,0,.4)[i],.75),(13,1.2,.65,(-.9,0,.9)[i],.75),(16.2,.12,.12,(-.65,0,.65)[i],.4)],2,2,10,(i/3,(i+1)/3))
 if name in ('left_hair','right_hair'):
  sign=1 if name=='left_hair' else -1;b['origin']=[sign*3.3,-5.7,-1.4];b['rotation']=[0,0,0]
  rings(b,[(0,.8,.65,0,0),(3,1,.65,sign*.35,0),(7,.9,.5,sign*.8,.1),(11,.55,.4,sign*.45,.2),(12.2,.05,.05,sign*.05,.1)],2,2,10,(.15,.43))
 if name.endswith('_flower'):
  for petal in b['children']:
   if petal['name'].startswith('petal'):
    rings(petal,profile([(-1.5,.13,.1),(-1.2,.43,.2),(-.65,.45,.22),(-.2,.15,.1)]),4,sides=8,flat=tex(4,.80,.47))
  b['origin'][0]*=.93
rings(body,profile([(0,2.65,1.65),(2,3,1.9),(5,2.7,1.65),(7.8,2.15,1.5),(9.4,2.75,1.8)]),4,5,16)
for b in body['children']:
 if b['name']=='skirt':
  rings(b,profile([(0,2.7,1.9),(1.5,3.3,2.25),(4.5,4.8,3.2),(6.8,5.35,3.65),(7.5,5.1,3.5)]),8,9,24)
 if b['name']=='tail':
  part=b['children'][0]
  for i in range(5):
   length=[3,3,3,2.8,2.3][i];w=[.7,.85,.9,.95,.9][i]
   rings(part,profile([(0,w*.92,w*.92),(length*.45,w,w),(length,w*.78,w*.78)]),3,sides=10,flat=tex(4,.8,.47) if i==4 else tex(3,.83,.09))
   if part['children']:part=part['children'][0]
for b in m['bones'][2:]:
 if b['name'].endswith('_arm'):
  sign=1 if b['name'].startswith('left') else -1;b['origin']=[sign*3.9,1.6,0];b['rotation']=[0,0,-sign*.07]
  rings(b,profile([(-1.5,.8,1),(0,1,1.1),(4,1.05,1.1),(8,1.45,1.4),(10,1.55,1.45)]),6,7,12)
 else:
  b['origin'][0]*=.85
  rings(b,profile([(0,1.2,1.2),(4,1.15,1.15),(8,.95,.95),(10,1.15,1.4),(12,1.2,1.5)]),10,11,12,(.03,.47))
# A continuous swept tube avoids visible elbow gaps while retaining the animated root.
tail=next(b for b in body['children'] if b['name']=='tail')
tail['children']=[];tail['quads']=[];sections=[]
for j in range(25):
 t=j/24*2.8;radius=(.88 if j<20 else max(.08,.88*(24-j)/4))
 cx=4.3*(1-math.cos(t));cy=4.3*math.sin(t)
 sections.append([[cx+radius*math.cos(a*math.tau/12)*math.cos(t),cy-radius*math.cos(a*math.tau/12)*math.sin(t),radius*math.sin(a*math.tau/12)] for a in range(12)])
for j in range(24):
 for a in range(12):
  b=(a+1)%12;ps=[sections[j][a],sections[j+1][a],sections[j+1][b],sections[j][b]]
  color=tex(4,.8,.47) if j>=20 else tex(3,.83,.09)
  tail['quads'].append(dict(vertices=ps,uv=[color]*4))
# Detach shared ring corners before applying per-face authoring transforms.
m=json.loads(json.dumps(m))
head=m["bones"][0];body=m["bones"][1]
# Proportions measured from three installed Touhou Little Maid base rigs.
# Model feet remain at Y=24; the unadorned crown is Y=-2 (26 units tall).
head['origin']=[0,6,0]
body['origin']=[0,6,0]
for q in body['quads']:
 for vertex in q['vertices']:vertex[1]*=9/9.4
for child in body['children']:
 if child['name']=='skirt':
  child['origin']=[0,5,0]
  for q in child['quads']:
   for vertex in q['vertices']:
    vertex[1]*=6/7.5
    vertex[0]*=5/5.35
    vertex[2]*=4/3.65
 if child['name']=='tail':child['origin']=[0,7.5,2.1]
for b in m['bones'][2:]:
 if b['name'].endswith('_arm'):
  sign=1 if b['name'].startswith('left') else -1
  b['origin']=[sign*3.8,6.5,0]
  for q in b['quads']:
   for vertex in q['vertices']:
    vertex[0]*=.82;vertex[2]*=.82;vertex[1]=-.5+(vertex[1]+1.5)*8/11.5
 else:
  sign=1 if b['name'].startswith('left') else -1
  b['origin']=[sign*2,15,0]
  for q in b['quads']:
   for vertex in q['vertices']:
    vertex[0]*=1.25;vertex[2]*=1.15;vertex[1]*=.75
# Keep the long-hair style, but shorten hanging locks along with the torso.
for child in head['children']:
 if 'hair' in child['name'] and child.get('quads'):
  for q in child['quads']:
   for vertex in q['vertices']:vertex[1]*=.82
# Side-view correction: retain volume and anchor accessories on the temples.
for q in head['quads']:
 for vertex in q['vertices']:vertex[2]*=1.28
for q in body['quads']:
 for vertex in q['vertices']:vertex[2]*=1.45
for child in head['children']:
 if child['name'].endswith('_flower'):
  sign=1 if child['name'].startswith('left') else -1
  child['origin']=[sign*3.7,-3.5,-1.35]
  child['rotation']=[0,-sign*.8,0]
 if child['name'].startswith('back_hair_'):
  child['origin'][2]=3.0
# A shallow frontal face patch prevents eyes wrapping onto the side of the head.
head_rings={}
for q in head['quads']:
 for x,y,z in q['vertices']:
  rx,rz=head_rings.get(y,(0,0));head_rings[y]=(max(rx,abs(x)),max(rz,abs(z)))
for q in head['quads']:
 for vertex in q['vertices']:
  x,y,z=vertex;rx,rz=head_rings[y]
  if z<0 and rx>0:
   t=abs(x)/rx
   vertex[2]=-.78*rz if t<=.8661 else -.78*rz*(1-t)/(1-.8661)
m['revision']='side_volume_and_attachment_4'
p=ROOT/'common/src/main/resources/assets/absmod/models/entity/blade_spirit.json';p.write_text(json.dumps(m,separators=(',',':'))+'\n')
print('Curved spirit rig generated')
