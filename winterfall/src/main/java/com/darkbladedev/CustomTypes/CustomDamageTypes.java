package com.darkbladedev.CustomTypes;


//import org.bukkit.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class CustomDamageTypes {
    @SuppressWarnings("unchecked")
    public static final ResourceKey<DamageType> BLEEDING = (ResourceKey<DamageType>) ResourceKey.b(ResourceKey.a(MinecraftKey.a("winterfall", "bleeding")));
    @SuppressWarnings("unchecked")
    public static final ResourceKey<DamageType> DEHYDRATION = (ResourceKey<DamageType>) ResourceKey.b(ResourceKey.a(MinecraftKey.a("winterfall", "bleeding")));

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.a(BLEEDING, new DamageType("bleeding", 0.1F));
        context.a(DEHYDRATION, new DamageType("dehydration", 0.1F));
    }
}
