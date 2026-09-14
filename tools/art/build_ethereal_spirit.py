"""Author an original curved Bedrock mesh and UV layout; never edits texture pixels."""
from pathlib import Path
import json, math, struct, base64
ROOT=Path(__file__).resolve().parents[2]
ASSETS=ROOT/'common/src/main/resources/assets/absmod'
BONES=[]

def bone(name,parent,pivot):
 b={'name':name,'pivot':pivot,'poly_mesh':{'normalized_uvs':True,'positions':[],'normals':[],'uvs':[],'polys':[]}}
 if parent:b['parent']=parent
 BONES.append(b)
 return b

def uv(tile,u,v):
 return [(tile%4+.012+.976*u)/4,(tile//4+.012+.976*v)/4]

def face(b,points,coords):
 a,c,d=points[:3];e=[c[i]-a[i] for i in range(3)];f=[d[i]-a[i] for i in range(3)]
 n=[e[1]*f[2]-e[2]*f[1],e[2]*f[0]-e[0]*f[2],e[0]*f[1]-e[1]*f[0]]
 length=sum(x*x for x in n)**.5
 if length<1e-7:return
 n=[x/length for x in n];m=b['poly_mesh'];poly=[]
 for p,t in zip(points,coords):
  i=len(m['positions']);m['positions'].append([round(x,6) for x in p]);m['normals'].append(n);m['uvs'].append(t);poly.append([i,i,i])
 m['polys'].append(poly)

def rings(b,profile,front,back=None,sides=32,face_skin=False):
 # Bottom-to-top profiles in absolute Bedrock coordinates: y, rx, rz, cx, cz.
 if face_skin:
  dense=[]
  for k in range(len(profile)-1):
   p0=profile[max(0,k-1)];p1=profile[k];p2=profile[k+1];p3=profile[min(len(profile)-1,k+2)]
   for step in range(5):
    t=step/5
    dense.append([.5*((2*p1[q])+(-p0[q]+p2[q])*t+(2*p0[q]-5*p1[q]+4*p2[q]-p3[q])*t*t+(-p0[q]+3*p1[q]-3*p2[q]+p3[q])*t*t*t) for q in range(5)])
  profile=dense+[profile[-1]]
 points=[]
 for y,rx,rz,cx,cz in profile:
  points.append([[cx+rx*math.sin(i*math.tau/sides),y,cz-rz*math.cos(i*math.tau/sides)] for i in range(sides)])
 for row in range(len(profile)-1):
  for i in range(sides):
   j=(i+1)%sides;angle=(i+.5)*math.tau/sides
   frontside=math.cos(angle)>.55 if face_skin else math.cos(angle)>=0
   tile=front if frontside else (front if back is None else back)
   pts=[points[row][i],points[row][j],points[row+1][j],points[row+1][i]]
   coords=[]
   for p,k,r in [(pts[0],i,row),(pts[1],j,row),(pts[2],j,row+1),(pts[3],i,row+1)]:
    u=(math.sin(k*math.tau/sides)+1)/2
    if face_skin and frontside:u=(p[0]/3.8+1)/2
    v=1-(profile[r][0]-profile[0][0])/(profile[-1][0]-profile[0][0])
    if face_skin and frontside:v=max(0,min(1,(24.3-p[1])/(24.3-18.3)))
    coords.append(uv(tile,max(0,min(1,u)),v))
   face(b,pts[::-1],coords[::-1])
 for row,reverse in [(0,True),(len(profile)-1,False)]:
  center=[profile[row][3],profile[row][0],profile[row][4]]
  for i in range(sides):
   pts=[center,points[row][i],points[row][(i+1)%sides]]
   if not reverse:pts=pts[::-1]
   face(b,pts,[uv(back if back is not None else front,.5,.5)]*3)

body=bone('body',None,[0,18,0])
rings(body,[(9,2.6,1.6,0,0),(11,2.1,1.4,0,0),(14,2.65,1.75,0,0),(16.7,3,1.6,0,0),(18,1.6,1.1,0,0)],4,5)
head=bone('head','body',[0,18,0])
rings(head,[(18.3,1.4,1.4,0,-.2),(19,2.6,2.3,0,0),(20.5,3.5,2.9,0,0),(22.5,3.9,3.15,0,0),(24.5,3.85,3.05,0,0),(25.8,3.1,2.5,0,0),(26.2,1.6,1.3,0,0)],0,1,32,True)
hair=bone('hair_crown','head',[0,18,0])
rings(hair,[(25.6,3.25,2.6,0,.15),(26.2,2.8,2.3,0,.15),(26.65,1.35,1.2,0,.15),(26.75,.15,.15,0,.15)],2,3)
# Separate tapered locks frame the face without carrying eye pixels around the sides.
for side in [-1,1]:
 lock=bone(('left' if side==1 else 'right')+'_hair','head',[side*3.5,24,0])
 rings(lock,[(11.8,.18,.3,side*4.3,1),(15,.85,.9,side*4.5,1),(20,1,1.4,side*4,1),(23,1.05,1.9,side*3.55,.65),(25.6,.9,1.7,side*2.9,.6)],2,3,16)
 for k in range(3):
  bang=bone(('left' if side==1 else 'right')+'_bang_'+str(k),'head',[0,18,0])
  x=side*(.65+k*1.1)
  rings(bang,[(22.8+k*.35,.08,.12,x+side*.25,-3.12),(24,.75,.45,x,-3.1),(25.6,.8,.5,x*.85,-2.7),(26.1,.45,.45,x*.65,-2)],2,2,12)
 ear=bone(('left' if side==1 else 'right')+'_ear','head',[side*2.7,25.5,0])
 pts=[[side*1.4,25.3,-.5],[side*4.4,25.3,-.5],[side*3.4,29,-.15]]
 if side>0:pts=pts[::-1]
 # Each side is an actual wedge with an inset pink inner triangle.
 face(ear,pts,[uv(12,0,1),uv(12,1,1),uv(12,.5,0)])
 rear=[[p[0],p[1],p[2]+.85] for p in pts]
 face(ear,rear[::-1],[uv(11,0,1),uv(11,1,1),uv(11,.5,0)])
 for i in range(3):face(ear,[pts[i],rear[i],rear[(i+1)%3],pts[(i+1)%3]],[uv(11,0,0),uv(11,1,0),uv(11,1,1),uv(11,0,1)])
 arm=bone(('left' if side==1 else 'right')+'_arm','body',[side*3.5,17,0])
 rings(arm,[(9,.75,.85,side*4.1,0),(10.2,1.25,1.15,side*4.1,0),(13,1.05,1,side*4,0),(16.3,.95,.95,side*3.8,0),(17.3,.65,.65,side*3.5,0)],8,8,20)
 rings(arm,[(8,.55,.65,side*4.1,-.1),(9.5,.65,.7,side*4.1,-.1)],1,1,16)
 leg=bone(('left' if side==1 else 'right')+'_leg','body',[side*1.65,9,0])
 rings(leg,[(.7,.85,1,side*1.65,0),(2,.78,.85,side*1.65,0),(5,1.05,1.1,side*1.65,0),(8.8,1.25,1.25,side*1.65,0)],9,9,20)
 rings(leg,[(0,1,1.5,side*1.65,-.45),(.6,1.05,1.6,side*1.65,-.45),(1.2,.9,1.15,side*1.65,-.2)],10,10,20)
back=bone('back_hair','head',[0,18,0])
rings(back,[(12.3,2.1,.4,0,2.7),(16,3.4,.75,0,2.65),(21,3.6,.85,0,2.7),(24.5,3.25,.7,0,2.55),(25.8,2.5,.6,0,2)],3,3,24)
skirt=bone('skirt','body',[0,11,0])
rings(skirt,[(6.5,5.3,3.7,0,0),(7.4,5.15,3.5,0,0),(9,4.4,3,0,0),(10.5,3.3,2.2,0,0),(11.5,2.3,1.6,0,0)],6,7,32)

# Separate petal skirt panels retain individual editable bones.
for side in [-1,1]:
 panel=bone(('left' if side==1 else 'right')+'_skirt_panel','skirt',[side*2,11,0])
 rings(panel,[(7.2,.15,.2,side*4.4,-1.4),(8.3,1.15,.42,side*3.9,-2.2),(9.8,1.05,.38,side*2.95,-2),(11.3,.4,.2,side*2,-1.5)],6,7,16)

def tube(b,path,tile):
 sections=[]
 for i,(x,y,z,r) in enumerate(path):
  a=path[max(0,i-1)];c=path[min(len(path)-1,i+1)];tx,ty=c[0]-a[0],c[1]-a[1];length=math.hypot(tx,ty);nx,ny=-ty/length,tx/length
  sections.append([[x+r*nx*math.cos(k*math.tau/16),y+r*ny*math.cos(k*math.tau/16),z+r*math.sin(k*math.tau/16)] for k in range(16)])
 for i in range(len(path)-1):
  for k in range(16):
   j=(k+1)%16
   face(b,[sections[i][k],sections[i][j],sections[i+1][j],sections[i+1][k]],[uv(tile,k/16,i/(len(path)-1)),uv(tile,(k+1)/16,i/(len(path)-1)),uv(tile,(k+1)/16,(i+1)/(len(path)-1)),uv(tile,k/16,(i+1)/(len(path)-1))])
tail=bone('tail','body',[0,10,2])
tube(tail,[(0,10,2,.7),(.8,9.2,3.4,.8),(2,8.3,4.7,.85),(3.5,8.5,5.3,.85),(4.4,9.7,5.4,.75)],11)
tip=bone('tail_tip','tail',[4.4,9.7,5.4])
tube(tip,[(4.4,9.7,5.4,.75),(4.7,11,5.5,.7),(4.4,12.2,5.5,.55),(3.8,12.8,5.5,.12)],11)
# Independent pendant and paw applique preserve the confirmed visual symbols.
def plaque(b,outline,cx,cy,z,tile,thickness=.18):
 front=[[cx+x,cy+y,z] for x,y in outline]
 back=[[x,y,z+thickness] for x,y,_ in front]
 center=[cx,cy,z]
 for i in range(len(front)):
  j=(i+1)%len(front)
  face(b,[center,front[i],front[j]],[uv(tile,.45,.45),uv(tile,.25,.25),uv(tile,.75,.75)])
  face(b,[front[j],front[i],back[i],back[j]],[uv(tile,.3,.3)]*4)
heart=bone('heart_crystal','body',[0,14,-1.85])
plaque(heart,[(-.6,.2),(-.45,.5),(-.15,.5),(0,.3),(.15,.5),(.45,.5),(.6,.2),(.4,-.1),(0,-.55),(-.4,-.1)],0,14,-1.9,15)
crystal=bone('pendant_crystal','heart_crystal',[0,13,-1.9])
plaque(crystal,[(0,.45),(.3,0),(0,-.7),(-.3,0)],0,12.8,-2,14,.35)
paw=bone('paw_applique','skirt',[0,8.5,-3.35])
for cx,cy,rx,ry in [(0,8.3,.38,.27),(-.48,8.8,.17,.22),(-.18,9,.16,.21),(.18,9,.16,.21),(.48,8.8,.17,.22)]:
 outline=[(rx*math.sin(i*math.tau/16),ry*math.cos(i*math.tau/16)) for i in range(16)]
 plaque(paw,outline,cx,cy,-3.38,15,.1)
texture=ASSETS/'textures/entity/blade_spirit/ethereal_atlas.png'
w,h=struct.unpack('>II',texture.read_bytes()[16:24])
model={'format_version':'1.12.0','minecraft:geometry':[{'description':{'identifier':'geometry.absmod.ethereal_spirit','texture_width':w,'texture_height':h,'visible_bounds_width':3,'visible_bounds_height':3,'visible_bounds_offset':[0,1,0]},'bones':BONES}]}
(ASSETS/'models/entity/blade_spirit_ethereal.geo.json').write_text(json.dumps(model,separators=(',',':'))+'\n',encoding='utf8')
# Preview uses precisely the same indexed positions/UVs in the neutral pose.
faces=[]
for b in BONES:
 m=b['poly_mesh']
 for poly in m['polys']:
  poly=poly[::-1]
  if len(poly)==3:poly=poly+[poly[-1]]
  faces.append({'bone':b['name'],'vertices':[[m['positions'][v[0]][0],24-m['positions'][v[0]][1],m['positions'][v[0]][2]] for v in poly],'uv':[m['uvs'][v[2]] for v in poly]})
html=(ROOT/'tools/art/spirit_preview.template.html').read_text(encoding='utf8')
html=html.replace('gl.enable(gl.DEPTH_TEST);','gl.enable(gl.DEPTH_TEST);gl.enable(gl.CULL_FACE);gl.frontFace(gl.CW);')
html=html.replace('临时使用车万女仆「橙」模型','粉白曲面模型').replace('FACES',json.dumps(faces,separators=(',',':'))).replace('ATLAS_DATA',base64.b64encode(texture.read_bytes()).decode())
(ROOT/'art/blade_spirit/preview.html').write_text(html,encoding='utf8')
print(f'Authored {len(BONES)} bones, {len(faces)} polygons; atlas {w}x{h}')
