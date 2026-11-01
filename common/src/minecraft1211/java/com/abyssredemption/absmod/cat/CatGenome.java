package com.abyssredemption.absmod.cat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.RandomSource;
import java.util.Optional;
import java.util.UUID;

/** A deliberately limited Mendelian dilution model, not a complete feline genome. */
public record CatGenome(int dilution,String pattern,Optional<UUID> firstParent,Optional<UUID> secondParent,int generation) {
    public static final Codec<CatGenome> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.intRange(0,3).fieldOf("dilution").forGetter(CatGenome::dilution),
        net.minecraft.resources.ResourceLocation.CODEC.xmap(Object::toString,net.minecraft.resources.ResourceLocation::parse).fieldOf("pattern").forGetter(CatGenome::pattern),
        UUIDUtil.CODEC.optionalFieldOf("first_parent").forGetter(CatGenome::firstParent),
        UUIDUtil.CODEC.optionalFieldOf("second_parent").forGetter(CatGenome::secondParent),
        Codec.intRange(0,1000000).fieldOf("generation").forGetter(CatGenome::generation)).apply(i,CatGenome::new));
    public CatGenome {
        if(dilution<0 || dilution>3 || generation<0 || generation>1000000)throw new IllegalArgumentException("Invalid cat genome");
    }
    public boolean dilute(){return dilution==3;}
    public String notation(){return dilution==0?"DD":dilution==3?"dd":"Dd";}
    public int recessiveCopies(){return Integer.bitCount(dilution);}
    public int diluteChancePercent(CatGenome partner){return recessiveCopies()*partner.recessiveCopies()*25;}
    private int gamete(RandomSource random){return dilution>>(random.nextBoolean()?1:0)&1;}
    public CatGenome breed(CatGenome partner,UUID self,UUID other,RandomSource random) {
        return new CatGenome(gamete(random)|(partner.gamete(random)<<1),random.nextBoolean()?pattern:partner.pattern,
            Optional.of(self),Optional.of(other),Math.min(1000000,Math.max(generation,partner.generation)+1));
    }
}
