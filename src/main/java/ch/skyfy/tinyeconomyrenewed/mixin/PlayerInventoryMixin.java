package ch.skyfy.tinyeconomyrenewed.mixin;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.UUID;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    private static UUID CUSTOM_HEALTH_MODIFIER_UUID = null;

    /**
     * Useful links
     * <a href="https://www.curseforge.com/minecraft/mc-mods/stats-keeper-fabric">...</a>
     * <a href="https://github.com/Terrails/StatsKeeper/blob/1.19.3/common/src/main/java/terrails/statskeeper/feature/health/HealthManager.java">...</a>
     */
    @Inject(
            method = "setStack",
            at = @At("HEAD")
    )
    public void onInsertStack(int slot, ItemStack stack, CallbackInfo ci) {
        if(0 == 0)return; // Disable the mixin, don't need for now

        var instance = (PlayerInventory) (Object) this;
        var player = (ServerPlayerEntity) instance.player;

        if (CUSTOM_HEALTH_MODIFIER_UUID == null) CUSTOM_HEALTH_MODIFIER_UUID = UUID.randomUUID();

        if (player.getInventory().getStack(0).getItem() == Items.DIRT) {
            var maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);

            var playerHealthWhenHavingDirtInSlot = 4f; // 2 hearts

            if (player.getHealth() >= playerHealthWhenHavingDirtInSlot) player.setHealth(playerHealthWhenHavingDirtInSlot);

            maxHealthAttr.clearModifiers();
            maxHealthAttr.removeModifier(CUSTOM_HEALTH_MODIFIER_UUID);

            var attributeModifier = new EntityAttributeModifier(CUSTOM_HEALTH_MODIFIER_UUID, "Health Modifier", playerHealthWhenHavingDirtInSlot, EntityAttributeModifier.Operation.ADDITION);

            maxHealthAttr.addPersistentModifier(attributeModifier);
            maxHealthAttr.setBaseValue(0.0);
            player.networkHandler.sendPacket(new EntityAttributesS2CPacket(player.getId(), Collections.singletonList(maxHealthAttr)));
        } else if (player.getInventory().getStack(0).getItem() == Items.STONE) {
            var maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);

            var playerHealthWhenHavingDirtInSlot = 24f; // 12 hearts

            if (player.getHealth() >= playerHealthWhenHavingDirtInSlot) player.setHealth(playerHealthWhenHavingDirtInSlot);

            maxHealthAttr.clearModifiers();
            var attributeModifier = new EntityAttributeModifier(CUSTOM_HEALTH_MODIFIER_UUID, "Health Modifier", playerHealthWhenHavingDirtInSlot, EntityAttributeModifier.Operation.ADDITION);
//
            maxHealthAttr.removeModifier(CUSTOM_HEALTH_MODIFIER_UUID);
            maxHealthAttr.addPersistentModifier(attributeModifier);
            maxHealthAttr.setBaseValue(0.0);
            player.networkHandler.sendPacket(new EntityAttributesS2CPacket(player.getId(), Collections.singletonList(maxHealthAttr)));
        }

    }

}
