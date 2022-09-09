package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.server.callbacks.CreateExplosionCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @ModifyVariable(
            method = "createExplosion",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/explosion/Explosion;collectBlocksAndDamageEntities()V",
                    shift = At.Shift.AFTER
            )
    )
    public Explosion createExplosion(Explosion explosion, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType) {
        explosion.collectBlocksAndDamageEntities();
        CreateExplosionCallback.EVENT.invoker().createExplosion(explosion, (ServerWorld) (Object)this, entity, damageSource, behavior,x,y,z,power,createFire, destructionType);
        return explosion;
    }

}
