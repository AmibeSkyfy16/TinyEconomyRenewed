package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.server.callbacks.PlayerTakeItemsCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.village.MerchantInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Shadow @Final public Inventory inventory;

    @Shadow @Final private int index;

    @Inject(method = "canTakeItems(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void canTakeItems(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) playerEntity;

        var slot = (Slot)(Object)this;
        if(slot instanceof TradeOutputSlot slot2){
            if(slot2.inventory instanceof MerchantInventory merchantInventory){
                System.out.println(merchantInventory.getStack(2).getTranslationKey());
                if(!merchantInventory.getStack(2).isEmpty()){
//                    System.out.println("not empty");
//                    cir.setReturnValue(false);
//                    cir.cancel();
                }
            }
        }

        ActionResult result = PlayerTakeItemsCallback.EVENT.invoker().onTakeItems(player, inventory);

        if (result == ActionResult.FAIL) {
            System.out.println("FAILED");
            // Canceling the item taking
            player.networkHandler.sendPacket(
                    new ScreenHandlerSlotUpdateS2CPacket(
                            -1,
                            -1,
                            player.getInventory().selectedSlot,
                            player.getInventory().getStack(player.getInventory().selectedSlot))
            );
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

}
