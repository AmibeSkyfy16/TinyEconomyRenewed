package ch.skyfy.tinyeconomyrenewed.mixin;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public interface InventoryMixin {




    //    @Inject(at = @At("HEAD"), method = "canPlayerUse")
//    @Override
//    public void canPlayerUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
//
//    }

//    @Inject(method = "canPlayerUse",  at = @At(value = "HEAD"), cancellable = true)
//    public default void canPlayerUse(PlayerEntity player, CallbackInfoReturnable<Boolean> cir){
//
//    }

}

