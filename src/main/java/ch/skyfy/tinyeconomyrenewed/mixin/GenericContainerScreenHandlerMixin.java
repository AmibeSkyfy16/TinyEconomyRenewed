package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.server.callbacks.PlayerInsertItemsCallback;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(GenericContainerScreenHandler.class)
public class GenericContainerScreenHandlerMixin {

    private final Map<GenericContainerScreenHandler, PlayerInventory> map = new HashMap<>();

    @SuppressWarnings("rawtypes")
    @Inject(
            method = "<init>(Lnet/minecraft/screen/ScreenHandlerType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/GenericContainerScreenHandler;checkSize(Lnet/minecraft/inventory/Inventory;I)V"
            )
    )
    public void init(ScreenHandlerType type, int syncId, PlayerInventory playerInventory, Inventory inventory, int rows, CallbackInfo ci){
        map.put((GenericContainerScreenHandler)(Object)this, playerInventory);
    }

    @ModifyArg(
            method = "<init>(Lnet/minecraft/screen/ScreenHandlerType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/GenericContainerScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;",
                    ordinal = 0
            )
    )
    public Slot test(Slot par1) {
        var t = (GenericContainerScreenHandler)(Object)this;
        return new Slot(par1.inventory, par1.getIndex(), par1.x, par1.y){
            @Override
            public boolean canInsert(ItemStack stack) {
                var playerInventory = map.get(t);
                if(playerInventory != null)
                    return PlayerInsertItemsCallback.EVENT.invoker().onInsertItems(playerInventory.player, t.getInventory());
                return true;
            }
        };
    }

}
