package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.server.callbacks.CreateExplosionCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {

    @Inject(
            method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;Z)Lnet/minecraft/world/explosion/Explosion;",
            at = @At(
                    value = "TAIL"
//                    target = "Lnet/minecraft/world/explosion/Explosion;collectBlocksAndDamageEntities()V"
//                    ,shift = At.Shift.AFTER
            )
            )
    public void createExplosion(Entity entity, DamageSource damageSource, ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType, boolean particles, CallbackInfoReturnable<Explosion> cir) {
//        explosion.collectBlocksAndDamageEntities();
        CreateExplosionCallback.EVENT.invoker().createExplosion(cir.getReturnValue(), (ServerWorld) (Object)this, entity, damageSource, behavior,x,y,z,power,createFire, explosionSourceType);
//        return explosion;
//        return value;
//        return null;
    }

}
