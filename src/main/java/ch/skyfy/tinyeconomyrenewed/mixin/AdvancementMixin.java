package ch.skyfy.tinyeconomyrenewed.mixin;

import ch.skyfy.tinyeconomyrenewed.callbacks.AdvancementCallback;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

@Mixin(Advancement.class)
public class AdvancementMixin {

//    @Inject(at = @At("HEAD"), method = "<init>(Lnet/minecraft/util/Identifier;)")
//    public void initp(Identifier id, @Nullable Advancement parent, @Nullable AdvancementDisplay display, AdvancementRewards rewards, Map<String, AdvancementCriterion> criteria, String[][] requirements, CallbackInfo callbackInfo){
//
//        System.out.println("init advancement");
//
//    }


    @Shadow @Final private Identifier id;
    @Shadow @Final private Text text;

    @Shadow @Final @Nullable private AdvancementDisplay display;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void test(Identifier id, @Nullable Advancement parent, @Nullable AdvancementDisplay display, AdvancementRewards rewards, Map<String, AdvancementCriterion> criteria, String[][] requirements,CallbackInfo callbackInfo){

        var advancementId = id.toString();
        var advancementFrame = "";
        var advancementTitle = "";
        var advancementDescription = "";

        if(id.toString().contains("recipes"))return;

//        System.out.println("id: " + id.toString());
//        System.out.println("text: " + text.toString());
//        System.out.println();


        if(display!= null) {
//            System.out.println("display.getDescription().toString(): " + display.getDescription().toString());
//            System.out.println("display.toString(): " + display.toString());
//            System.out.println("display.getFrame().toString() " + display.getFrame().toString());
//            System.out.println("display.getDescription().toString() " + display.getDescription().toString());
//            System.out.println("display.getTitle().toString() " + display.getTitle().toString());

            advancementFrame = display.getFrame().toString();
            advancementTitle = StringUtils.substringBetween(display.getTitle().toString(), "'", "'");
            advancementDescription = StringUtils.substringBetween(display.getDescription().toString(), "'", "'");

            AdvancementCallback.EVENT.invoker().init(id, display);
        }

//        System.out.println("advancementId: " + advancementId);
//        System.out.println("advancementFrame: " + advancementFrame);
//        System.out.println("advancementTitle: " + advancementTitle);
//        System.out.println("advancementDescription: " + advancementDescription);


        System.out.println("\n\n");

    }

}
