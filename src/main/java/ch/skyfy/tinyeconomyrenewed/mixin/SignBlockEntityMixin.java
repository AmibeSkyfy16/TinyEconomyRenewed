package ch.skyfy.tinyeconomyrenewed.mixin;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin {

//    @Inject(at = @At("TAIL"), method = "onActivate")
//    public void onActivate(ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir){
//        System.out.println("sign activated");
//    }

//        @Inject(at = @At("TAIL"), method = "toUpdatePacket()Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;")
//    public void toUpdatePacket(CallbackInfoReturnable<BlockEntityUpdateS2CPacket> cir){
//        System.out.println("toUpdatePacket");
//    }

}
