// Deterministic UV material baking: AI-painted base, code-defined cosmetic layers.
using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
public static class CatCoatBaker {
 static string[] colors={"black","blue","chocolate","lilac","cinnamon","fawn","red","cream","pink"};
 static string[] markings={"solid","mackerel","classic","spotted","ticked","marbled","rosette","patched"};
 static string[] whites={"none","socks","bib","bicolor"};
 static string[] eyes={"amber","green","blue","odd"};
 static int[,] palette={{61,56,60},{112,123,145},{111,73,54},{177,154,172},{178,102,56},{213,176,143},{222,134,55},{247,214,162},{255,230,225}};
 static int[,] iris={{230,172,42},{97,174,91},{65,168,235},{65,168,235}};
 static Color Mix(Color a,Color b,double t){return Color.FromArgb(255,(int)(a.R*(1-t)+b.R*t),(int)(a.G*(1-t)+b.G*t),(int)(a.B*(1-t)+b.B*t));}
 static Color Shade(Color c,double t){return Color.FromArgb(255,Math.Min(255,(int)(c.R*t)),Math.Min(255,(int)(c.G*t)),Math.Min(255,(int)(c.B*t)));}
 static bool Mark(int m,int x,int y){switch(m){case 1:return (y+x/4)%4==0;case 2:return ((x-9)*(x-9)+(y-13)*(y-13))%35<9;case 3:return x%5<2 && y%4<2;case 4:return (x*7+y*11)%9<2;case 5:return (x+y/2)%7<3;case 6:return ((x%6-2)*(x%6-2)+(y%5-2)*(y%5-2))>=2 && ((x%6-2)*(x%6-2)+(y%5-2)*(y%5-2))<5;case 7:return (x/5+y/5)%2==0;default:return false;}}
 public static void Bake(string root){
 string output=Path.Combine(root,"common/src/main/resources/assets/absmod/textures/entity/cat/coat");Directory.CreateDirectory(output);
 string data=Path.Combine(root,"common/src/main/resources/data/absmod/cat_variant/coat");Directory.CreateDirectory(data);
 using(Bitmap normal=new Bitmap(Path.Combine(root,"art/cats/genetics/source/lilac.png")))
 using(Bitmap pink=new Bitmap(Path.Combine(root,"art/cats/diversity/source/pale_pink.png"))){
 for(int c=0;c<9;c++)for(int m=0;m<8;m++)for(int w=0;w<4;w++)for(int e=0;e<4;e++)for(int point=0;point<2;point++){
 string name=colors[c]+"_"+markings[m]+"_"+whites[w]+"_"+eyes[e]+(point==1?"_point":"");
 for(int baby=0;baby<2;baby++)using(Bitmap image=new Bitmap(baby==1?32:64,32)){
 Bitmap source=c==8?pink:normal;Color baseColor=Color.FromArgb(palette[c,0],palette[c,1],palette[c,2]);
 for(int y=0;y<32;y++)for(int x=0;x<image.Width;x++){
 Color sample=source.GetPixel((int)((x+0.5)*source.Width/64),(int)((y+baby*32+0.5)*source.Height/64));
 if(sample.A<128)continue;
 bool body=baby==0?x>=20 && x<40 && y<22:x<22 && y>=8 && y<18;
 bool feet=baby==0?(x>=40 && x<48 && y>=9 && y<12)||(x>=8 && x<16 && y>=18 && y<21):(x>=12 && x<24 && y>=20 && y<26);
 bool head=baby==0?x<20 && y<10:x<18 && y<8;
 Color color=c==8?Color.FromArgb(255,sample.R,sample.G,sample.B):Shade(baseColor,0.76+(sample.R+sample.G+sample.B)/765.0*0.28);
 if(point==1){color=body?Mix(color,Color.FromArgb(255,247,236),0.78):Shade(color,c==8?0.97:0.73);}
 if(body && Mark(m,x,y))color=c==8?Mix(color,Color.FromArgb(249,195,190),0.40):Shade(color,point==1?0.83:0.55);
 bool white=w>0 && feet || w>=2 && body && (baby==0?y<9:y>14) || w==3 && body && x%10<4;
 if(white)color=Color.FromArgb(249,246,241);
 // Remove inherited eye pigment; the new eyes are assigned below at exact front-face UVs.
 if(head && sample.B>sample.R*1.1 && sample.G>sample.R*1.05)color=baseColor;
 image.SetPixel(x,y,color);
 }
 int eyeY=baby==1?5:6;int left=baby==1?4:5;int right=baby==1?8:9;
 Color eye=Color.FromArgb(iris[e,0],iris[e,1],iris[e,2]);
 image.SetPixel(left,eyeY,eye);image.SetPixel(right,eyeY,e==3?Color.FromArgb(231,179,62):eye);
 image.SetPixel(left+1,eyeY,Color.FromArgb(34,35,43));image.SetPixel(right-1,eyeY,Color.FromArgb(34,35,43));
 image.SetPixel(baby==1?6:7,eyeY+1,c==8?Color.FromArgb(250,184,181):Color.FromArgb(212,143,150));
 image.Save(Path.Combine(output,name+(baby==1?"_baby":"")+".png"),ImageFormat.Png);
 }
 File.WriteAllText(Path.Combine(data,name+".json"),"{\"asset_id\":\"absmod:entity/cat/coat/"+name+"\",\"baby_asset_id\":\"absmod:entity/cat/coat/"+name+"_baby\",\"spawn_conditions\":[]}\n");
 }
 }
 }
}
