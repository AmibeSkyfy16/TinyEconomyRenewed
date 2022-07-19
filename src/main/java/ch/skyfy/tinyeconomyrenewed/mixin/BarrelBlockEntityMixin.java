package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.MixinConstants;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Collections;

@Mixin(BarrelBlockEntity.class)
public class BarrelBlockEntityMixin {


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
//
//        MixinConstants.OPENED_INVENTORIES.compute(barrelBlockEntity, (inventory, playerEntities) -> {
//            if (playerEntities == null) return Arrays.asList(playerEntity);
//            if (!playerEntities.contains(playerEntity)) playerEntities.add(playerEntity);
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
//        if (list.isEmpty()) MixinConstants.OPENED_INVENTORIES.remove(barrelBlockEntity);
//    }




}
