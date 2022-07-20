package ch.skyfy.tinyeconomyrenewed.mixin;

import net.minecraft.entity.TntEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntEntity.class)
public class TntMixin {

    @Inject(method = "explode", at = @At("TAIL"))
    public void ex(CallbackInfo ci){
        System.out.println("Explode");
    }

}
