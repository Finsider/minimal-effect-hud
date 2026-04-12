package fin.mineffecthud.mixin;

import fin.mineffecthud.StatusEffectAttribute;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Gui.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    // Pair<step, color>
    @Unique
    private final Map<Holder<MobEffect>, Tuple<Integer, Integer>> STATUS_EFFECT_MAP = new HashMap<>();

    @Inject(
            method = "extractEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderStatusEffectTimer(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci, @Local MobEffectInstance instance, @Local(ordinal = 2) int x, @Local(ordinal = 3) int y) {
        if (instance.isInfiniteDuration()) return;

        final Tuple<Integer, Integer> p = STATUS_EFFECT_MAP.computeIfAbsent(
            instance.getEffect(),
            re -> createPair(instance)
        );

        final int drawX = x + 3;
        final int drawY = y + 21;
        final int step = p.getA();
        final int color = p.getB();

        context.fill(
                drawX, drawY,
                drawX + step, drawY + 1,
                color
        );
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    private void tick(CallbackInfo ci) {
        if (this.minecraft.player == null) return;
        tickStatusEffect();
    }

    @Unique
    private void tickStatusEffect() {
        for (MobEffectInstance instance : this.minecraft.player.getActiveEffects()) {
            if (!instance.showIcon() || instance.isInfiniteDuration()) continue;
            STATUS_EFFECT_MAP.put(instance.getEffect(), createPair(instance));
        }
    }

    @Unique
    private Tuple<Integer, Integer> createPair(MobEffectInstance instance) {
        final StatusEffectAttribute attribute = StatusEffectAttribute.get(instance);

        final int duration = instance.getDuration();
        final int maxDuration = attribute.maxDuration();

        final int step = getStep(duration, maxDuration, 18);
        final int color = getColor(duration, maxDuration);

        return new Tuple<>(step, color);
    }

    @Unique
    private int getStep(int curr, int max, int maxStep) {
        return Mth.clamp(Math.round((float) (curr * maxStep) / max), 0, maxStep);
    }

    @Unique
    public int getColor(int curr, int max) {
        return Mth.hsvToArgb(curr / (max * 3.0F), 1.0F, 1.0F, 255);
    }
}