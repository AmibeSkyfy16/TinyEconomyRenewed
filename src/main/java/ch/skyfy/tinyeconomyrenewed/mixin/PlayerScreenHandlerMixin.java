package ch.skyfy.tinyeconomyrenewed.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
public class PlayerScreenHandlerMixin {

//    @Inject(
//            method = "<init>",
//            at = @At(
//                    value = "INVOKE",
//                    target = ""
//            )
//    )
//    public void init(PlayerInventory inventory, boolean onServer, final PlayerEntity owner, CallbackInfo callbackInfo){
//
//    }

}
