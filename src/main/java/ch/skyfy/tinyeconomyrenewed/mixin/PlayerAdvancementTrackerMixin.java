package ch.skyfy.tinyeconomyrenewed.mixin;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.data.server.AdvancementProvider;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.server.command.AdvancementCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {

    @Final
    @Shadow
    private Set<Advancement> visibleAdvancements;
    @Final
    @Shadow
    private Map<Advancement, AdvancementProgress> advancementToProgress;

    @Inject(method = "sendUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void injectAdvancementLoader(ServerPlayerEntity player, CallbackInfo ci, Map<Identifier, AdvancementProgress> map, Set<Advancement> set, Set<Identifier> set2) {

        var hashset = new HashSet<String>();

        for (var advancementAdvancementProgressEntry : advancementToProgress.entrySet()) {
            var idStr = advancementAdvancementProgressEntry.getKey().getId().toString();
            if (!idStr.contains("recipe"))
                hashset.add(idStr);
        }

        System.out.println("printing advancements");
        List<String> list = new ArrayList<>(hashset);
        Collections.sort(list);
        for (String s : list) {
            System.out.println(s);
        }
        System.out.println("\n\n");
    }
}
