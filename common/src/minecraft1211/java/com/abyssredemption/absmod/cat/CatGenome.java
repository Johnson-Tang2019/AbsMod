package com.abyssredemption.absmod.cat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.RandomSource;
import java.util.Optional;
import java.util.UUID;

/** Independent D/d, B/b and C/cs loci. Pattern inheritance remains a game approximation. */
public record CatGenome(int dilution,String pattern,Optional<UUID> firstParent,Optional<UUID> secondParent,int generation,int chocolate,int point,Optional<CatCoat> coat) {
    public static final Codec<CatGenome> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.intRange(0,3).fieldOf("dilution").forGetter(CatGenome::dilution),
        net.minecraft.resources.ResourceLocation.CODEC.xmap(Object::toString,net.minecraft.resources.ResourceLocation::parse).fieldOf("pattern").forGetter(CatGenome::pattern),
        UUIDUtil.CODEC.optionalFieldOf("first_parent").forGetter(CatGenome::firstParent),
        UUIDUtil.CODEC.optionalFieldOf("second_parent").forGetter(CatGenome::secondParent),
        Codec.intRange(0,1000000).fieldOf("generation").forGetter(CatGenome::generation),
        Codec.intRange(0,3).optionalFieldOf("chocolate",1).forGetter(CatGenome::chocolate),
        Codec.intRange(-1,3).optionalFieldOf("point",-1).forGetter(CatGenome::point),
        CatCoat.CODEC.optionalFieldOf("coat").forGetter(CatGenome::coat)).apply(i,CatGenome::new));
    public CatGenome(int dilution,String pattern,Optional<UUID> firstParent,Optional<UUID> secondParent,int generation,int chocolate,int point) {
        this(dilution,pattern,firstParent,secondParent,generation,chocolate,point,Optional.empty());
    }
    public CatGenome(int dilution,String pattern,Optional<UUID> firstParent,Optional<UUID> secondParent,int generation) {
        this(dilution,pattern,firstParent,secondParent,generation,1,-1);
    }
    public CatGenome {
        // Legacy cats acquire hidden carriers without recoloring their saved appearance.
        if(point==-1)point=pattern.equals("minecraft:siamese")?3:1;
        if(dilution<0 || dilution>3 || chocolate<0 || chocolate>3 || point<0 || point>3 || generation<0 || generation>1000000)
            throw new IllegalArgumentException("Invalid cat genome");
    }
    public static CatGenome founder(String pattern,UUID id) {
        if(pattern.startsWith("absmod:coat/")) {
            String[] parts=pattern.substring(12).split("_");
            int color=java.util.Arrays.asList(CatCoat.COLORS).indexOf(parts[0]);
            if(color>=0 && parts.length>=4) {
                var traits=new CatCoat(color>=6 && color<8?2:color>=4 && color<6?1:0,
                    java.util.Arrays.asList(CatCoat.MARKINGS).indexOf(parts[1]),java.util.Arrays.asList(CatCoat.WHITES).indexOf(parts[2]),
                    java.util.Arrays.asList(CatCoat.EYES).indexOf(parts[3]),color==8);
                return new CatGenome(color<8 && color%2==1?3:0,"minecraft:all_black",Optional.empty(),Optional.empty(),0,color==2 || color==3?3:0,parts.length==5?3:0,Optional.of(traits));
            }
        }
        int hash=id.hashCode();
        if(java.util.Set.of("absmod:chocolate","absmod:lilac","absmod:seal_point","absmod:blue_point","absmod:chocolate_point","absmod:lilac_point").contains(pattern)) {
            String coat=pattern.substring(7);
            boolean brown=coat.contains("chocolate") || coat.contains("lilac");
            boolean dilute=coat.contains("blue") || coat.contains("lilac");
            return new CatGenome(dilute?3:0,"minecraft:all_black",Optional.empty(),Optional.empty(),0,brown?3:0,coat.endsWith("_point")?3:0);
        }
        boolean blue=pattern.equals("minecraft:british_shorthair");
        return new CatGenome(blue?3:Math.floorMod(hash,3),blue?"minecraft:all_black":pattern,Optional.empty(),Optional.empty(),0,
            Math.floorMod(hash>>>4,3),pattern.equals("minecraft:siamese")?3:Math.floorMod(hash>>>8,3));
    }
    public boolean dilute(){return dilution==3;}
    public boolean brown(){return chocolate==3;}
    public boolean pointed(){return point==3;}
    private static String locus(int value,String dominant,String recessive){return value==0?dominant+dominant:value==3?recessive+recessive:dominant+recessive;}
    public String notation(){return locus(dilution,"D","d");}
    public String fullNotation(){return notation()+" / "+locus(chocolate,"B","b")+" / "+locus(point,"C","cs");}
    public int recessiveCopies(){return Integer.bitCount(dilution);}
    public int diluteChancePercent(CatGenome partner){return chance(dilution,partner.dilution);}
    public int chocolateChancePercent(CatGenome partner){return chance(chocolate,partner.chocolate);}
    public int pointChancePercent(CatGenome partner){return chance(point,partner.point);}
    public double lilacPointChancePercent(CatGenome partner){return diluteChancePercent(partner)*chocolateChancePercent(partner)*pointChancePercent(partner)/10000.0;}
    private static int chance(int a,int b){return Integer.bitCount(a)*Integer.bitCount(b)*25;}
    private static int gamete(int value,RandomSource random){return value>>(random.nextBoolean()?1:0)&1;}
    private static int inherit(int self,int other,RandomSource random){return gamete(self,random)|(gamete(other,random)<<1);}
    public boolean supportedCoat(){return coat.isPresent() || pattern.equals("minecraft:all_black") || pattern.equals("minecraft:british_shorthair") || pattern.equals("minecraft:siamese");}
    public boolean pink(){return coat.map(CatCoat::pink).orElse(false);}
    public int pinkDenominator(CatGenome other){return CatCoat.pinkDenominator(pink(),other.pink());}
    public String appearance() {
        if(coat.isPresent())return "absmod:"+coat.get().variant(this);
        if(!supportedCoat())return pattern;
        String color=brown()?(dilute()?"lilac":"chocolate"):(dilute()?"blue":"seal");
        if(pointed())return "absmod:"+color+"_point";
        if(brown())return "absmod:"+color;
        return dilute()?"minecraft:british_shorthair":"minecraft:all_black";
    }
    public CatGenome breed(CatGenome partner,UUID self,UUID other,RandomSource random) {
        return new CatGenome(inherit(dilution,partner.dilution,random),random.nextBoolean()?pattern:partner.pattern,
            Optional.of(self),Optional.of(other),Math.min(1000000,Math.max(generation,partner.generation)+1),
            inherit(chocolate,partner.chocolate,random),inherit(point,partner.point,random),
            Optional.of(coat.orElseGet(()->CatCoat.founder(pattern,self)).breed(partner.coat.orElseGet(()->CatCoat.founder(partner.pattern,other)),random)));
    }
}
