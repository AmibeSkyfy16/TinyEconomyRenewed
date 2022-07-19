package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.callbacks.PlayerInsertItemsCallback;
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

//    @Inject(
//            method = "<init>(Lnet/minecraft/screen/ScreenHandlerType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;I)V",
//            at = @At("HEAD")
//    )
//    public void s(ScreenHandlerType type, int syncId, PlayerInventory playerInventory, Inventory inventory, int rows, CallbackInfo ci){
//
//    }


    private final Map<GenericContainerScreenHandler, PlayerInventory> map = new HashMap<>();

    @Inject(
            method = "<init>(Lnet/minecraft/screen/ScreenHandlerType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/GenericContainerScreenHandler;checkSize(Lnet/minecraft/inventory/Inventory;I)V"
            )
    )
    public void init(ScreenHandlerType type, int syncId, PlayerInventory playerInventory, Inventory inventory, int rows, CallbackInfo ci){

        var t = (GenericContainerScreenHandler)(Object)this;
        map.put(t, playerInventory);

    }

//    @Redirect(
//            method = "<init>(Lnet/minecraft/screen/ScreenHandlerType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;I)V",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/screen/GenericContainerScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;"
//            )
//    )
//    public Slot test(GenericContainerScreenHandler instance, Slot slot){
//        return new Slot(instance.getInventory(), 1,1,1);
//    }


    @ModifyArg(
            method = "<init>(Lnet/minecraft/screen/ScreenHandlerType;ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;I)V",
            at = @At(
                    value = "INVOKE",

                    target = "Lnet/minecraft/screen/GenericContainerScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;"
            )
    )
    public Slot test(Slot par1) {
//        try {
//            ScreenHandlerType<?> type = args.get(0);
//            int syncId = args.get(1);
//            PlayerInventory playerInventory = args.get(2);
//            Inventory inventory = args.get(3);
//            int rows = args.get(4);
//        }catch (Exception e){e.printStackTrace();}
//        for (int i = 0; i < args.size(); i++) {
//            var u = args.get(i);
//            System.out.println("\n");
//            System.out.println(u);
//            System.out.println(u.getClass().toString());
//            System.out.println("\n");
//        }
        var t = (GenericContainerScreenHandler)(Object)this;
        return new Slot(par1.inventory, par1.getIndex(), par1.x, par1.y){
            @Override
            public boolean canInsert(ItemStack stack) {
                var value = map.get(t);
                if(value != null){
                    var result = PlayerInsertItemsCallback.EVENT.invoker().onInsertItems(value.player, t.getInventory());
                    if(!result)return false;
//                    System.out.println("Player name: " + value.player.getName().toString());
                }
                // TODO If it's a specific player and a par1.inventory have a specific location -> return false
                return true;
            }
        };
    }

}
