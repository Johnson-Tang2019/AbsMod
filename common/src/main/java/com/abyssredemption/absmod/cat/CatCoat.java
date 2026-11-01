package com.abyssredemption.absmod.cat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import java.util.UUID;

/** Cosmetic inheritance is a game approximation; pink is a fantasy mutation, not a real locus. */
public record CatCoat(int family, int marking, int white, int eyes, boolean pink) {
    public static final String[] COLORS={"black","blue","chocolate","lilac","cinnamon","fawn","red","cream","pink"};
    public static final String[] MARKINGS={"solid","mackerel","classic","spotted","ticked","marbled","rosette","patched"};
    public static final String[] WHITES={"none","socks","bib","bicolor"};
    public static final String[] EYES={"amber","green","blue","odd"};
    public static final Codec<CatCoat> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.intRange(0,2).fieldOf("family").forGetter(CatCoat::family),
        Codec.intRange(0,7).fieldOf("marking").forGetter(CatCoat::marking),
        Codec.intRange(0,3).fieldOf("white").forGetter(CatCoat::white),
        Codec.intRange(0,3).fieldOf("eyes").forGetter(CatCoat::eyes),
        Codec.BOOL.fieldOf("pink").forGetter(CatCoat::pink)).apply(i,CatCoat::new));
    public CatCoat {
        if(family<0 || family>2 || marking<0 || marking>7 || white<0 || white>3 || eyes<0 || eyes>3)
            throw new IllegalArgumentException("Invalid cat coat traits");
    }
    public static CatCoat founder(String pattern,UUID id) {
        int hash=id.hashCode();
        int family=pattern.equals("minecraft:red")?2:Math.floorMod(hash,12)==0?1:0;
        int marking=pattern.equals("minecraft:all_black") || pattern.equals("minecraft:siamese")?0:Math.floorMod(hash>>>3,8);
        return new CatCoat(family,marking,Math.floorMod(hash>>>8,4),Math.floorMod(hash>>>12,4),false);
    }
    public int color(CatGenome genome) {
        if(pink)return 8;
        if(family==1)return genome.dilute()?5:4;
        if(family==2)return genome.dilute()?7:6;
        return (genome.brown()?2:0)+(genome.dilute()?1:0);
    }
    public String variant(CatGenome genome) {return variant(color(genome),marking,white,eyes,genome.pointed());}
    public static String variant(int color,int marking,int white,int eyes,boolean point) {
        return "coat/"+COLORS[color]+"_"+MARKINGS[marking]+"_"+WHITES[white]+"_"+EYES[eyes]+(point?"_point":"");
    }
    public static int pinkDenominator(boolean first,boolean second) {return first && second?128:first || second?1024:16384;}
    private static int trait(int first,int second,int count,RandomSource random) {
        // Small cosmetic variation keeps every pattern discoverable without consuming special items.
        return random.nextInt(32)==0?random.nextInt(count):random.nextBoolean()?first:second;
    }
    public CatCoat breed(CatCoat other,RandomSource random) {
        boolean childPink=random.nextInt(pinkDenominator(pink,other.pink))==0;
        return new CatCoat(trait(family,other.family,3,random),trait(marking,other.marking,8,random),
            trait(white,other.white,4,random),trait(eyes,other.eyes,4,random),childPink);
    }
}
