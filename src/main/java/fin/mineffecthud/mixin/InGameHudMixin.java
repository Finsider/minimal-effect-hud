package fin.mineffecthud.mixin;

import fin.mineffecthud.StatusEffectAttribute;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    // Pair<step, color>
    @Unique
    private final Map<RegistryEntry<StatusEffect>, Pair<Integer, Integer>> STATUS_EFFECT_MAP = new HashMap<>();

    @Inject(
            method = "renderStatusEffectOverlay",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderStatusEffectTimer(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci, @Local StatusEffectInstance effect, @Local(ordinal = 2) int x, @Local(ordinal = 3) int y) {
        if (effect.isInfinite()) return;

        final Pair<Integer, Integer> p = STATUS_EFFECT_MAP.computeIfAbsent(
            effect.getEffectType(),
            re -> createPair(effect)
        );

        final int drawX = x + 3;
        final int drawY = y + 21;
        final int step = p.getLeft();
        final int color = p.getRight();

        context.fill(
                drawX, drawY,
                drawX + step, drawY + 1,
                color
        );
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    private void tick(CallbackInfo ci) {
        if (this.client.player == null) return;
        tickStatusEffect();
    }

    @Unique
    private void tickStatusEffect() {
        for (StatusEffectInstance instance : this.client.player.getStatusEffects()) {
            if (!instance.shouldShowIcon() || instance.isInfinite()) continue;
            STATUS_EFFECT_MAP.put(instance.getEffectType(), createPair(instance));
        }
    }

    @Unique
    private Pair<Integer, Integer> createPair(StatusEffectInstance instance) {
        final StatusEffectAttribute attribute = StatusEffectAttribute.get(instance);

        final int duration = instance.getDuration();
        final int maxDuration = attribute.maxDuration();

        final int step = getStep(duration, maxDuration, 18);
        final int color = getColor(duration, maxDuration);

        return new Pair<>(step, color);
    }

    @Unique
    private int getStep(int curr, int max, int maxStep) {
        return MathHelper.clamp(Math.round((float) (curr * maxStep) / max), 0, maxStep);
    }

    @Unique
    public int getColor(int curr, int max) {
        return MathHelper.hsvToArgb(curr / (max * 3.0F), 1.0F, 1.0F, 255);
    }
}