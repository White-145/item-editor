package me.white.simpleitemeditor.mixin;

import net.minecraft.client.resource.language.TranslationStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TranslationStorage.class)
public interface TranslationStorageMixin {
    @Accessor
    Map<String, String> getTranslations();
}