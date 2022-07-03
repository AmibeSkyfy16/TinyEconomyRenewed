package ch.skyfy.tinyeconomyrenewed.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Inject(
            method = "tryBreakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true
    )
    private void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState state, BlockEntity entity, Block block) {
//        boolean result = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(this.world, this.player, pos, state, entity);

//        if (true) {
////            PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(this.world, this.player, pos, state, entity);
//
//            cir.setReturnValue(false);
//            cir.cancel();
//        }
    }

}
