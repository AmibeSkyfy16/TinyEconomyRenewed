package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.callbacks.AdvancementCompletedCallback;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;

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
        AdvancementCompletedCallback.EVENT.invoker().completed(owner, advancement, criterionName);
    }

}
