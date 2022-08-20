//package ch.skyfy.tinyeconomyrenewed.mixin;
//
//import ch.skyfy.tinyeconomyrenewed.callbacks.VillagerTradeDoneCallback;
//import net.minecraft.entity.passive.MerchantEntity;
//import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.util.ActionResult;
//import net.minecraft.village.MerchantInventory;
//import net.minecraft.village.TradeOffer;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(MerchantEntity.class)
//public class MerchantEntityMixin {
//
//    @Inject(
//            method = "trade",
////            at = @At(
////                    value = "INVOKE",
////                    target = "Lnet/minecraft/advancement/criterion/VillagerTradeCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/passive/MerchantEntity;Lnet/minecraft/item/ItemStack;)V",
////                    shift = At.Shift.AFTER
////            ),
//            at = @At("HEAD"),
//            cancellable = true)
//    public void trade(TradeOffer offer, CallbackInfo ci) {
//        System.out.println("MerchantEntityMixin trade");
//        var merchantEntity = (MerchantEntity)(Object)this;
//        if (merchantEntity.getCustomer() == null || !(merchantEntity.getCustomer() instanceof ServerPlayerEntity)) return;
//        var result = VillagerTradeDoneCallback.EVENT.invoker().trade(offer.copySellItem(), (ServerPlayerEntity) merchantEntity.getCustomer());
//        if(result == ActionResult.FAIL){
//            System.out.println("cancelled trade");
//            ci.cancel();
//        }
//    }
//
//}
