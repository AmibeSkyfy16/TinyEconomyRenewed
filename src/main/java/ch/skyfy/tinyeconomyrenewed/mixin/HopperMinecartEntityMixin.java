package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.callbacks.HopperCallback;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperMinecartEntity.class)
public class HopperMinecartEntityMixin {

    @Inject(method = "canOperate", at = @At("HEAD"), cancellable = true)
    private void insertAndExtract(CallbackInfoReturnable<Boolean> cir) {
        var hopperMinecartEntity = (HopperMinecartEntity)(Object)this;
        var result = HopperCallback.EVENT.invoker().insertAndExtract(hopperMinecartEntity.world, hopperMinecartEntity.getBlockPos(), hopperMinecartEntity.getBlockStateAtPos(), hopperMinecartEntity);
        if (result.getResult() == ActionResult.FAIL) {
            cir.setReturnValue(result.getValue());
            cir.cancel();
        }
    }

}
