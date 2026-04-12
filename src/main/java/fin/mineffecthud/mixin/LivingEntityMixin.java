package fin.mineffecthud.mixin;

import fin.mineffecthud.StatusEffectAttribute;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    // this is the way to ENSURE that for every change in status effect, the old value is removed.
    // I don't know if this is safe tho...

    @Inject(method = "forceAddEffect", at = @At("HEAD"))
    private void onSetStatusEffect(MobEffectInstance effect, Entity source, CallbackInfo ci) {
        StatusEffectAttribute statusEffectAttribute = StatusEffectAttribute.get(effect);

        if (StatusEffectAttribute.shouldUpdate(effect, statusEffectAttribute))
            StatusEffectAttribute.update(effect);
    }

    // using removeStatusEffectInternal() instead of removeStatusEffect() because the former worked and the latter didn't, I don't know why.
    // remove status effect from the player status effect list. Reason is just to delete unused effect from the map.
    @Inject(method = "removeEffectNoUpdate", at = @At("RETURN"))
    private void onStatusEffectRemoved(Holder<MobEffect> effect, CallbackInfoReturnable<MobEffectInstance> cir) {
        StatusEffectAttribute.remove(effect);
    }
}
