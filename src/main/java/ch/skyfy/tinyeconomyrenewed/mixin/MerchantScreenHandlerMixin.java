//package ch.skyfy.tinyeconomyrenewed.mixin;
//
//import ch.skyfy.tinyeconomyrenewed.callbacks.VillagerTradeDoneCallback;
//import net.minecraft.entity.passive.VillagerEntity;
//import net.minecraft.inventory.Inventory;
//import net.minecraft.screen.MerchantScreenHandler;
//import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.util.ActionResult;
//import net.minecraft.village.Merchant;
//import net.minecraft.village.MerchantInventory;
//import org.checkerframework.checker.units.qual.A;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(MerchantScreenHandler.class)
//public class MerchantScreenHandlerMixin {
//
//    @Shadow
//    @Final
//    private Merchant merchant;
//    @Shadow
//    @Final
//    private MerchantInventory merchantInventory;
//
//    @Inject(method = "setLevelProgress", at = @At("HEAD"), cancellable = true)
//    public void setLevelProgression(int levelProgress, CallbackInfo ci) {
//        System.out.println("setLevelProgression " + levelProgress);
//        if (merchant.getCustomer() == null) {
//            System.out.println("null customer");
//            return;
//        }
//        var r = VillagerTradeDoneCallback.EVENT.invoker().trade(merchantInventory.getStack(2), (ServerPlayerEntity) merchant.getCustomer());
//        if (r == ActionResult.FAIL) ci.cancel();
//    }
//
//    @Inject(method = "setExperienceFromServer", at = @At("HEAD"), cancellable = true)
//    public void setExperienceFromServer(int levelProgress, CallbackInfo ci) {
//        System.out.println("setExperienceFromServer " + levelProgress);
//        if (merchant.getCustomer() == null) {
//            System.out.println("null customer 2");
//            return;
//        }
//        var r = VillagerTradeDoneCallback.EVENT.invoker().trade(merchantInventory.getStack(2), (ServerPlayerEntity) merchant.getCustomer());
//        if (r == ActionResult.FAIL) ci.cancel();
//    }
//
//
//    @Inject(method = "setExperienceFromServer", at = @At("HEAD"), cancellable = true)
//    public void onContentChanged(int experience, CallbackInfo ci) {
//        System.out.println("onContentChanged");
//
////        System.out.println("xp: " + experience);
////        experience = 0;
////        ci.cancel();
//
////        System.out.println("\n\n");
////        for(int i = 0; i < inventory.size(); i++){
////            System.out.println(inventory.getStack(i).getTranslationKey());
////        }
//
//
//    }
//
//}
