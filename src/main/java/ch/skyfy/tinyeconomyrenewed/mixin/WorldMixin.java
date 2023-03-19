package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.server.callbacks.CreateExplosionCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(World.class)
public class WorldMixin {

//    @Inject(
//            method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;Z)Lnet/minecraft/world/explosion/Explosion;",
//            at = @At(
//                    value = "TAIL"
////                    target = "Lnet/minecraft/world/explosion/Explosion;collectBlocksAndDamageEntities()V"
////                    ,shift = At.Shift.AFTER
//            )
//            )
//    public void createExplosion(Entity entity, DamageSource damageSource, ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType, boolean particles, CallbackInfoReturnable<Explosion> cir) {
////        explosion.collectBlocksAndDamageEntities();
//        CreateExplosionCallback.EVENT.invoker().createExplosion(cir.getReturnValue(), (ServerWorld) (Object)this, entity, damageSource, behavior,x,y,z,power,createFire, explosionSourceType);
////        return explosion;
////        return value;
////        return null;
//    }

//    @Inject(
//            method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;Z)Lnet/minecraft/world/explosion/Explosion;",
//            at = @At(
//                    value = "INVOKE_ASSIGN",
//                    target = "Lnet/minecraft/world/explosion/Explosion;collectBlocksAndDamageEntities()V",
//                    shift = At.Shift.AFTER
//            )
//    )
//    public void createExplosion2(Entity entity, DamageSource damageSource, ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType, boolean particles, CallbackInfoReturnable<Explosion> cir) {
//        CreateExplosionCallback.EVENT.invoker().createExplosion(cir.getReturnValue(), (World) (Object)this, entity, damageSource, behavior,x,y,z,power,createFire, explosionSourceType);
//    }

//    @Redirect(
//            method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;Z)Lnet/minecraft/world/explosion/Explosion;",
//            at = @At(
//                    value = "INVOKE",
////                    target = "Lnet/minecraft/world/explosion/Explosion;<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)V"
//                    target = "Lnet/minecraft/world/explosion/Explosion;collectBlocksAndDamageEntities()V",
//                    shift = At.Shift.AFTER
//            )
//    )
//    public void createExplosion4(Explosion instance, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType) {
////        instance.collectBlocksAndDamageEntities();
//
//        CreateExplosionCallback.EVENT.invoker().createExplosion(instance,
//                (World) (Object) this,
//                instance.getEntity(),
//                instance.getDamageSource(),
//                ((ExplosionAccessor) instance).getBehavior(),
//                ((ExplosionAccessor) instance).getX(),
//                ((ExplosionAccessor) instance).getY(),
//                ((ExplosionAccessor) instance).getZ(),
//                ((ExplosionAccessor) instance).getPower(),
//                ((ExplosionAccessor) instance).getCreateFire(),
//                explosionSourceType);
//
//    }


//    @Inject(
//            method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;Z)Lnet/minecraft/world/explosion/Explosion;",
//            at = @At(
//                    value = "INVOKE",
////                    target = "Lnet/minecraft/world/explosion/Explosion;<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)V"
//                    target = "Lnet/minecraft/world/explosion/Explosion;collectBlocksAndDamageEntities()V"
//            ),
//            locals = LocalCapture.CAPTURE_FAILSOFT
//    )
//    public void createExplosion5(Entity entity, DamageSource damageSource, ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType, boolean particles, CallbackInfoReturnable<Explosion> cir, boolean createFire2, World.ExplosionSourceType explosionSourceType2, boolean particles2, Explosion.DestructionType destructionType, Explosion explosion) {
//        CreateExplosionCallback.EVENT.invoker().createExplosion(explosion, (World) (Object) this, entity, damageSource, behavior, x, y, z, power, createFire, explosionSourceType);
//
////        return null;
//    }

    @ModifyVariable(
            method = "createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;Z)Lnet/minecraft/world/explosion/Explosion;",
            at = @At(
                    value = "INVOKE",
//                    target = "Lnet/minecraft/world/explosion/Explosion;<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)V"
                    target = "Lnet/minecraft/world/explosion/Explosion;collectBlocksAndDamageEntities()V",
                    shift = At.Shift.AFTER
            )
    )
    public Explosion createExplosion6(Explosion explosion, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType) {
        CreateExplosionCallback.EVENT.invoker().createExplosion(explosion, (World) (Object) this, entity, damageSource, behavior, x, y, z, power, createFire, explosionSourceType);

//        explosion.clearAffectedBlocks();
//        System.out.println("clearing block");

        return explosion;
//        return null;
    }


}
