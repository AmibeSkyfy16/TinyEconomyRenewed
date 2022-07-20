package ch.skyfy.tinyeconomyrenewed.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireballEntity.class)
public class FireballEntityMixin {
    @Shadow
    private int explosionPower;

    @Redirect(
            method = "onCollision",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;"
            )
    )
    public Explosion damage(World instance, Entity entity, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType) {
        var obj = (FireballEntity) (Object) this;
        boolean bl = instance.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
        return instance.createExplosion(obj, obj.getX(), obj.getY(), obj.getZ(), explosionPower, bl, bl ? Explosion.DestructionType.DESTROY : Explosion.DestructionType.NONE);
    }

}
