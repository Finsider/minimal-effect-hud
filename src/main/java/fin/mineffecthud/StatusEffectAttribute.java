package fin.mineffecthud;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.HashMap;
import java.util.Map;

public record StatusEffectAttribute(int maxDuration, int amplifier, boolean isAmbient) {

    // this Implementation is inspired from @SoRadGaming Simple-HUD-Enhanced StatusEffectTracker class
    // see: https://github.com/SoRadGaming/Simple-HUD-Enhanced/blob/main/src/main/java/com/soradgaming/simplehudenhanced/utli/StatusEffectsTracker.java

    private static final Map<Holder<MobEffect>, StatusEffectAttribute> STATUS_EFFECT_ATTRIBUTE_MAP = new HashMap<>();

    // maxDuration for maxDuration, amplifier and isAmbient to help updating the map.

    public static StatusEffectAttribute create(MobEffectInstance instance) {
        return new StatusEffectAttribute(
            instance.getDuration(),
            instance.getAmplifier(),
            instance.isAmbient()
        );
    }

    public static StatusEffectAttribute get(MobEffectInstance instance) {
        return STATUS_EFFECT_ATTRIBUTE_MAP.computeIfAbsent(instance.getEffect(), key -> create(instance));
    }

    public static void update(MobEffectInstance instance) {
        STATUS_EFFECT_ATTRIBUTE_MAP.put(instance.getEffect(), create(instance));
    }

    public static void remove(Holder<MobEffect> effectRegistry) {
        STATUS_EFFECT_ATTRIBUTE_MAP.remove(effectRegistry);
    }

    public static boolean shouldUpdate(MobEffectInstance current, StatusEffectAttribute cached) {
        return  current.getAmplifier() != cached.amplifier() || // different Amplifier: update
                current.isAmbient() != cached.isAmbient() || // different Ambient: update
                current.getDuration() > cached.maxDuration(); // higher Duration: update
    }

    public static Map<Holder<MobEffect>, StatusEffectAttribute> getMap() {
        return STATUS_EFFECT_ATTRIBUTE_MAP;
    }
}