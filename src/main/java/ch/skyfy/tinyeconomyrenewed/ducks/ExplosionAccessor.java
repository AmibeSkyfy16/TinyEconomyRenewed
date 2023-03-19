package ch.skyfy.tinyeconomyrenewed.ducks;

import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Explosion.class)
public interface ExplosionAccessor {


    @Accessor(value = "behavior")
    ExplosionBehavior getBehavior();

    @Accessor(value = "x")
    double getX();

    @Accessor(value = "y")
    double getY();

    @Accessor(value = "z")
    double getZ();

    @Accessor(value = "power")
    float getPower();

    @Accessor(value = "createFire")
    boolean getCreateFire();

    @Accessor(value = "destructionType")
    Explosion.DestructionType getDestructionType();

}
