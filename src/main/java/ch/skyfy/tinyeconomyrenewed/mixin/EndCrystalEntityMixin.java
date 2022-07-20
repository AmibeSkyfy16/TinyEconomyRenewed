package ch.skyfy.tinyeconomyrenewed.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EndCrystalEntity.class)
public class EndCrystalEntityMixin {

    @Redirect(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;"
            )
    )
    public Explosion damage(World instance, Entity entity, double x, double y, double z, float power, Explosion.DestructionType destructionType){
        var obj = (EndCrystalEntity)(Object)this;
        return instance.createExplosion(obj, obj.getX(), obj.getY(), obj.getZ(), 6.0F, Explosion.DestructionType.DESTROY);
    }
}
