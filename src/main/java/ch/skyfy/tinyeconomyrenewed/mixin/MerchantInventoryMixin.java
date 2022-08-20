package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.callbacks.VillagerTradeCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.TradeOffer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantInventory.class)
class MerchantInventoryMixin {

    @Shadow
    @Final
    private Merchant merchant;

    @Shadow
    @Nullable
    private TradeOffer tradeOffer;

    @Shadow private int merchantRewardedExperience;

    @Inject(
            method = "updateOffers",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/village/MerchantInventory;setStack(ILnet/minecraft/item/ItemStack;)V",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            ),
            cancellable = true)
    public void updateOffers(CallbackInfo ci) {
        if (merchant.getCustomer() == null && !(merchant.getCustomer() instanceof ServerPlayerEntity) || tradeOffer == null) return;
        var r = VillagerTradeCallback.EVENT.invoker().trade(tradeOffer.copySellItem(), (ServerPlayerEntity) merchant.getCustomer());
        if (r == ActionResult.FAIL) {
            tradeOffer = null;
            merchantRewardedExperience = 0;
            ((MerchantInventory)(Object)this).setStack(2, ItemStack.EMPTY);
            this.merchant.onSellingItem(ItemStack.EMPTY);
            ci.cancel();
        }
    }
}
