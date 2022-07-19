package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.MixinConstants;
import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerInsertItemsCallback;
import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerTakeItemsCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Shadow @Final private int index;
    @Shadow @Final public Inventory inventory;

    @Shadow public int id;

    @Inject(method = "canTakeItems(Lnet/minecraft/entity/player/PlayerEntity;)Z", at = @At(value = "HEAD"), cancellable = true)
    private void canTakeItems(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) playerEntity;

        ActionResult result = PlayerTakeItemsCallback.EVENT.invoker().onTakeItems(player, inventory);

        if (result == ActionResult.FAIL) {
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

//    @Inject(method = "canInsert", at = @At(value = "HEAD"), cancellable = true)
//    private void canInsert(ItemStack newItem, CallbackInfoReturnable<Boolean> cir) {
//
//        var list = MixinConstants.OPENED_INVENTORIES.getOrDefault(inventory, null);
//        if(list == null) return;
//
//        var result = PlayerInsertItemsCallback.EVENT.invoker().onInsertItems(list, inventory);
//        if(!result){
//            System.out.println("cancelled");
//            cir.setReturnValue(false);
//            cir.cancel();
//        }
//
//
//        // TODO Cancel
//    }

}
