//package ch.skyfy.tinyeconomyrenewed.mixin;
//
//import ch.skyfy.tinyeconomyrenewed.callbacks.VillagerTradeDoneCallback;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.Items;
//import net.minecraft.screen.slot.TradeOutputSlot;
//import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.util.ActionResult;
//import net.minecraft.village.MerchantInventory;
//import org.checkerframework.checker.units.qual.A;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//
//@Mixin(TradeOutputSlot.class)
//public class TradeOutputSlotMixin {
//
//    @Shadow
//    @Final
//    private MerchantInventory merchantInventory;
//
//
//    @Inject(
//            method = "onTakeItem",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/village/Merchant;trade(Lnet/minecraft/village/TradeOffer;)V",
//                    shift = At.Shift.BEFORE
//            ),
//            cancellable = true)
//    public void onTakeItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
////        var result = VillagerTradeDoneCallback.EVENT.invoker().trade(stack, (ServerPlayerEntity) player);
////        if(result == ActionResult.FAIL){
////            System.out.println("cancelled");
////            ci.cancel();
////        }
////        if (!merchantInventory.getStack(2).isEmpty()) {
////            System.out.println("PlayerEntity");
////            ci.cancel();
////        }
//    }
////
////    @Inject(method = "takeStack", at = @At("HEAD"), cancellable = true)
////    public void takeStack(int amount, CallbackInfoReturnable<ItemStack> cir) {
////        if (!merchantInventory.getStack(2).isEmpty()) {
////            System.out.println("takeStack");
////            cir.setReturnValue(Items.AIR.getDefaultStack());
////            cir.cancel();
////        }
////    }
//
//}
