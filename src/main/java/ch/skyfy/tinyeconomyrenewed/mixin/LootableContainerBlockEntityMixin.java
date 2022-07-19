package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.callbacks.CanPlayerUseInventoryCallback;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootableContainerBlockEntity.class)
public class LootableContainerBlockEntityMixin {

//    @Inject(
//            method = "onOpen",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/block/entity/ViewerCountManager;openContainer(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
//                    shift = At.Shift.BEFORE
//            )
//    )
//    public void onOpen(PlayerEntity playerEntity, CallbackInfo callbackInfo) {
//        var barrelBlockEntity = (BarrelBlockEntity) (Object) this;
//        MixinConstants.OPENED_INVENTORIES.compute(barrelBlockEntity, (inventory, playerEntities) -> {
//            if (playerEntities == null) return Arrays.asList(playerEntity);
//            if(!playerEntities.contains(playerEntity))playerEntities.add(playerEntity);
//            return playerEntities;
//        });
//    }
//
//    @Inject(
//            method = "onClose",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/block/entity/ViewerCountManager;closeContainer(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
//                    shift = At.Shift.BEFORE
//            )
//    )
//    public void onClose(PlayerEntity playerEntity, CallbackInfo callbackInfo) {
//        var barrelBlockEntity = (BarrelBlockEntity) (Object) this;
//        var list = MixinConstants.OPENED_INVENTORIES.getOrDefault(barrelBlockEntity, Collections.emptyList());
//        list.remove(playerEntity);
//        if(list.isEmpty()) MixinConstants.OPENED_INVENTORIES.remove(barrelBlockEntity);
//    }

//    @Inject(method = "canPlayerUse", at = @At("HEAD"), cancellable = true)
//    public void canPlayerUse(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> cir){
//        var inventory = (LootableContainerBlockEntity)(Object)this;
//        var result = CanPlayerUseInventoryCallback.EVENT.invoker().onInsertItems(playerEntity, inventory);
//        if(!result){
//            System.out.println("cancelled canPlayerUse");
//            cir.setReturnValue(false);
//            cir.cancel();
//        }
//    }

}
