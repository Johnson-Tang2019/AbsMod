"""Extract a bounded SlashBlade motion preset; original assets are retained separately.
No NyMmd code is copied. Position lerp and quaternion interpolation follow its documented sampling convention.
"""
from pathlib import Path
import struct,json,math,bisect,shutil,hashlib
ROOT=Path(__file__).resolve().parents[2]
REF=next((ROOT/'build/slashblade-reference').iterdir())
ASSET=REF/'src/main/resources/assets/slashblade'
OUT=ROOT/'art/animation/slashblade_baseline/upstream'
SOURCES=['combostate/motion.vmd','model/bladeholder.pmd','model/pa/player_motion.vmd','model/pa/alex.pmd']
def text(raw):return raw.split(b'\0')[0].decode('cp932',errors='replace')
def vmd(path):
 b=path.read_bytes(); assert b[:20]==b'Vocaloid Motion Data';n=struct.unpack_from('<I',b,50)[0];tracks={}
 for i in range(n):
  o=54+i*111;name=text(b[o:o+15]);f,*v=struct.unpack_from('<I7f',b,o+15);tracks.setdefault(name,[]).append([f,v[:3],v[3:]])
 for t in tracks.values():t.sort(key=lambda k:k[0])
 return tracks
def pmd(path):
 b=path.read_bytes();o=283;n=struct.unpack_from('<I',b,o)[0];o+=4+38*n;n=struct.unpack_from('<I',b,o)[0];o+=4+2*n;n=struct.unpack_from('<I',b,o)[0];o+=4+70*n;n=struct.unpack_from('<H',b,o)[0];o+=2;bones=[]
 for i in range(n):
  name=text(b[o:o+20]);parent,tail,typ,ik,x,y,z=struct.unpack_from('<HHBH3f',b,o+20);bones.append((name,parent,(x,y,z)));o+=39
 assert struct.unpack_from('<H',b,o)[0]==0,'IK requires an explicit solver'
 return bones
def mul(a,b):
 x,y,z,w=a;X,Y,Z,W=b
 return [w*X+x*W+y*Z-z*Y,w*Y-x*Z+y*W+z*X,w*Z+x*Y-y*X+z*W,w*W-x*X-y*Y-z*Z]
def rotate(q,p):return mul(mul(q,[*p,0]),[-q[0],-q[1],-q[2],q[3]])[:3]
def sample(tracks,name,f):
 t=tracks.get(name)
 if not t:return [[0,0,0],[0,0,0,1]]
 i=bisect.bisect_right([k[0] for k in t],f);a=t[max(0,i-1)];b=t[min(i,len(t)-1)];v=0 if a[0]==b[0] else (f-a[0])/(b[0]-a[0]);q=b[2][:]
 if sum(x*y for x,y in zip(a[2],q))<0:q=[-x for x in q]
 q=[x+(y-x)*v for x,y in zip(a[2],q)];length=math.sqrt(sum(x*x for x in q));q=[x/length for x in q]
 return [[x+(y-x)*v for x,y in zip(a[1],b[1])],q]
weapon=vmd(ASSET/SOURCES[0]);rig=pmd(ASSET/SOURCES[1]);player=vmd(ASSET/SOURCES[2]);pmd(ASSET/SOURCES[3])
def frame(f):
 world=[]
 for name,parent,rest in rig:
  p,q=sample(weapon,name,f)
  if parent!=65535:
   pp,pq=world[parent];offset=[rest[j]-rig[parent][2][j]+p[j] for j in range(3)];p=[x+y for x,y in zip(pp,rotate(pq,offset))];q=mul(pq,q)
  world.append((p,q))
 hard=world[next(i for i,b in enumerate(rig) if b[0]=='hardpointA')]
 return {'blade':hard,**{n:sample(player,n,f) for n in ['body','torso','head','right arm','left arm']}}
clips=[]
for name,start,end,rec0,rec1,roll in [('combo_a1',1,10,10,41,-10),('combo_a2',100,115,115,151,170),('combo_a3_first',200,209,230,245,-61),('combo_a3_second',209,218,230,245,138),('combo_a4',519,532,576,608,45)]:
 clips.append({'id':name,'source_range':[start,end],'recovery_range':[rec0,rec1],'effect_roll':roll,'strike':[frame(f/2) for f in range(start*2,end*2+1)],'recovery':[frame(f/2) for f in range(rec0*2,rec1*2+1)]})
result={'format':'absmod_motion_v1','preset':'slashblade_baseline','sample_fps':60,'source_commit':'6e2a0a092fb794d7ea56fd83452869674f3ab1c7','clips':clips}
(ROOT/'common/src/main/resources/assets/absmod/animations/slashblade_baseline.json').write_text(json.dumps(result,separators=(',',':')),encoding='utf-8')
manifest=[]
for n in SOURCES:
 dst=OUT/n;dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(ASSET/n,dst);manifest.append({'path':n,'sha256':hashlib.sha256(dst.read_bytes()).hexdigest()})
shutil.copyfile(REF/'LICENSE',OUT/'LICENSE')
(OUT.parent/'manifest.json').write_text(json.dumps({'commit':result['source_commit'],'files':manifest},indent=2),encoding='utf-8')
for c in clips:print(c['id'],c['source_range'],c['strike'][0]['blade'],c['strike'][-1]['blade'])
