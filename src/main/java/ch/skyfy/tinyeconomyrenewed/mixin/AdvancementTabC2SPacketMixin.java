package ch.skyfy.tinyeconomyrenewed.mixin;

import net.minecraft.advancement.Advancement;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AdvancementTabC2SPacket.class)
public class AdvancementTabC2SPacketMixin {

    @Inject(at = @At("HEAD"), method = "open")
    private static void test(Advancement advancement, CallbackInfoReturnable<AdvancementTabC2SPacket> cir){
        System.out.println("my mixin OPEN");
    }

}
