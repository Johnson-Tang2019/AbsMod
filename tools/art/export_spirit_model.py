"""Export the legacy borrowed spirit geometry to OBJ and a self-contained interactive preview."""
import base64
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "common/src/main/resources/assets/absmod"
OUT = ROOT / "art/blade_spirit"
MODEL = json.loads((ASSETS / "models/entity/borrowed_cat_spirit.json").read_text(encoding="utf-8"))

def transform(point, bone):
    x, y, z = point
    rx, ry, rz = bone["rotation"]
    y, z = y * math.cos(rx) - z * math.sin(rx), y * math.sin(rx) + z * math.cos(rx)
    x, z = x * math.cos(ry) + z * math.sin(ry), -x * math.sin(ry) + z * math.cos(ry)
    x, y = x * math.cos(rz) - y * math.sin(rz), x * math.sin(rz) + y * math.cos(rz)
    return [x + bone["origin"][0], y + bone["origin"][1], z + bone["origin"][2]]

faces = []
def visit(bone, ancestors):
    chain = [bone] + ancestors
    for cube in bone["cubes"]:
        x,y,z,w,h,d = cube["box"]
        corners = [[x,y,z],[x+w,y,z],[x+w,y+h,z],[x,y+h,z],
                   [x,y,z+d],[x+w,y,z+d],[x+w,y+h,z+d],[x,y+h,z+d]]
        for point in corners:
            scale = cube["taper"][0 if point[1] < y+h/2 else 1]
            point[0] = x+w/2+(point[0]-x-w/2)*scale
            point[2] = z+d/2+(point[2]-z-d/2)*scale
            for parent in chain: point[:] = transform(point, parent)
        for index, indices in enumerate([[5,4,0,1],[2,3,7,6],[0,4,7,3],[1,0,3,2],[5,1,2,6],[4,5,6,7]]):
            u0,v0,u1,v1 = cube["uv"][index]
            faces.append(dict(bone=bone["name"],vertices=[corners[i] for i in indices],uv=[[u1,v0],[u0,v0],[u0,v1],[u1,v1]]))
    for quad in bone.get("quads", []):
        points = []
        for point in quad["vertices"]:
            point = point[:]
            for parent in chain: point = transform(point,parent)
            points.append(point)
        faces.append(dict(bone=bone["name"], vertices=points, uv=quad["uv"]))
    for child in bone["children"]: visit(child,chain)
for bone in MODEL["bones"]: visit(bone,[])
OUT.mkdir(parents=True,exist_ok=True)
obj = ["# Neutral pose; units are model pixels. OBJ is an interchange mesh, not an animation rig.","mtllib blade_spirit.mtl","usemtl floral"]
for face in faces:
    obj.append("g " + face["bone"])
    for x,y,z in face["vertices"]: obj.append(f"v {x:.6f} {24-y:.6f} {-z:.6f}")
    for u,v in face["uv"]: obj.append(f"vt {u:.8f} {1-v:.8f}")
    n = len([line for line in obj if line.startswith("v ")])
    obj.append("f " + " ".join(f"{i}/{i}" for i in range(n-3,n+1)))
(OUT/"blade_spirit.obj").write_text("\n".join(obj)+"\n",encoding="utf-8")
(OUT/"blade_spirit.mtl").write_text("newmtl floral\nKd 1 1 1\nmap_Kd ../../common/src/main/resources/assets/absmod/textures/entity/blade_spirit/borrowed_cat_spirit.png\n",encoding="utf-8")
texture = base64.b64encode((ASSETS/"textures/entity/blade_spirit/borrowed_cat_spirit.png").read_bytes()).decode()
html = """<!doctype html><meta charset="utf-8"><title>喵娘剑灵 · 模型预览</title>
<style>body{margin:0;background:#fff5fa;color:#754b64;font:16px system-ui}main{max-width:1120px;margin:auto;padding:24px}h1{font-size:24px;margin:0 0 10px}p{line-height:1.7}canvas{width:100%;height:720px;background:radial-gradient(ellipse,#fff,#f3dce9);border-radius:24px;touch-action:none}button{border:0;border-radius:20px;padding:10px 20px;background:#e58eb9;color:white;margin-right:8px;cursor:pointer}</style>
<main><h1>喵娘剑灵 · 临时使用车万女仆「橙」模型</h1><p>拖动旋转模型，滚轮缩放。此页面展示实际模型几何和 UV，非 Minecraft 游戏截图。</p>
<button onclick="yaw=0;draw()">正面</button><button onclick="yaw=Math.PI;draw()">背面</button><button onclick="yaw=Math.PI/2;draw()">侧面</button><button onclick="yaw=-.55;draw()">三分之四</button><p></p><canvas id="view" width="1600" height="1100"></canvas></main>
<script>
const faces=FACES; const atlas=new Image();
const canvas=document.getElementById('view'),gl=canvas.getContext('webgl',{alpha:true,antialias:true});let yaw=-.35,zoom=27;
function shader(type,source){let s=gl.createShader(type);gl.shaderSource(s,source);gl.compileShader(s);if(!gl.getShaderParameter(s,gl.COMPILE_STATUS))throw Error(gl.getShaderInfoLog(s));return s}
const program=gl.createProgram();gl.attachShader(program,shader(gl.VERTEX_SHADER,'attribute vec3 p; attribute vec2 uv; varying vec2 t; void main(){gl_Position=vec4(p,1.0);t=uv;}'));gl.attachShader(program,shader(gl.FRAGMENT_SHADER,'precision mediump float; varying vec2 t; uniform sampler2D atlas;void main(){vec4 c=texture2D(atlas,t);if(c.a<0.1)discard;gl_FragColor=c;}'));gl.linkProgram(program);gl.useProgram(program);gl.enable(gl.DEPTH_TEST);
const position=gl.createBuffer(),texcoord=gl.createBuffer();const texture=gl.createTexture();
const vertices=[],coords=[];for(const f of faces)for(const i of [0,1,2,0,2,3]){vertices.push(f.vertices[i]);coords.push(...f.uv[i])}
gl.bindBuffer(gl.ARRAY_BUFFER,texcoord);gl.bufferData(gl.ARRAY_BUFFER,new Float32Array(coords),gl.STATIC_DRAW);let uvLocation=gl.getAttribLocation(program,'uv');gl.enableVertexAttribArray(uvLocation);gl.vertexAttribPointer(uvLocation,2,gl.FLOAT,false,0,0);
function project(v){let x=-v[0]*Math.cos(yaw)+v[2]*Math.sin(yaw),z=v[2]*Math.cos(yaw)+v[0]*Math.sin(yaw);return [x*zoom/800,-((v[1]-9.5)*zoom*.97+z*zoom*.18)/550,(z*.97-(v[1]-9.5)*.18)/100]}
function draw(){gl.viewport(0,0,1600,1100);gl.clearColor(0,0,0,0);gl.clear(gl.COLOR_BUFFER_BIT|gl.DEPTH_BUFFER_BIT);gl.bindBuffer(gl.ARRAY_BUFFER,position);gl.bufferData(gl.ARRAY_BUFFER,new Float32Array(vertices.flatMap(project)),gl.DYNAMIC_DRAW);let loc=gl.getAttribLocation(program,'p');gl.enableVertexAttribArray(loc);gl.vertexAttribPointer(loc,3,gl.FLOAT,false,0,0);gl.drawArrays(gl.TRIANGLES,0,vertices.length);window.previewReady=true}
atlas.onload=()=>{gl.bindTexture(gl.TEXTURE_2D,texture);gl.texImage2D(gl.TEXTURE_2D,0,gl.RGBA,gl.RGBA,gl.UNSIGNED_BYTE,atlas);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MIN_FILTER,gl.LINEAR);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_MAG_FILTER,gl.NEAREST);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_S,gl.CLAMP_TO_EDGE);gl.texParameteri(gl.TEXTURE_2D,gl.TEXTURE_WRAP_T,gl.CLAMP_TO_EDGE);draw()};atlas.src='data:image/png;base64,ATLAS_DATA';
canvas.onpointerdown=e=>{canvas.setPointerCapture(e.pointerId);canvas.onpointermove=m=>{yaw+=m.movementX*.012;draw()}};canvas.onpointerup=()=>canvas.onpointermove=null;canvas.onwheel=e=>{e.preventDefault();zoom=Math.max(15,Math.min(35,zoom-e.deltaY*.01));draw()};
</script>""".replace("FACES",json.dumps(faces,separators=(",",":"))).replace("ATLAS_DATA",texture)
(OUT/"borrowed-preview.html").write_text(html,encoding="utf-8")
print(f"Exported {len(faces)} quads to OBJ and interactive preview")
