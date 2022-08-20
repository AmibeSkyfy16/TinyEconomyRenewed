//package ch.skyfy.tinyeconomyrenewed.mixin;
//
//import net.minecraft.village.SimpleMerchant;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(SimpleMerchant.class)
//public class SimpleMerchantMixin {
//
//    @Inject(method = "setExperienceFromServer", at = @At("HEAD"))
//    public void setExperienceFromServer(int experience, CallbackInfo ci) {
//        System.out.println("\n");
//
//        System.out.println("SimpleMerchantMixin -> setExperienceFromServer + xp: " + experience);
//
//        System.out.println("\n");
//    }
//
//}
