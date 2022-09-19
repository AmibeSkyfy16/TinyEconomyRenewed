package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.server.callbacks.AdvancementCompletedCallback;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {

    @Shadow private ServerPlayerEntity owner;

    @Inject(
            method = "grantCriterion",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/advancement/Advancement;getRewards()Lnet/minecraft/advancement/AdvancementRewards;", shift = At.Shift.AFTER
            )
    )
    public void playerAdvancementComplete(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (advancement.getId().toString().contains("recipe")) return;
        if(advancement.getDisplay() == null)return; // Some datapacks add technical "advancement" that are trigger many times, a real advancement will always have a display object
        AdvancementCompletedCallback.EVENT.invoker().completed(owner, advancement, criterionName);
    }

}
