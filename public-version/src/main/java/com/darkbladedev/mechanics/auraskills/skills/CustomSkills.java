package com.darkbladedev.mechanics.auraskills.skills;

import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.CustomSkill;

public class CustomSkills {
    private static final String pluginName = "savage-frontier";

    /*       EJEMPLO DE CÓMO SE DEBE HACER

        public static final CustomSkill TRADING = CustomSkill
            .builder(NamespacedId.of("pluginname", "trading"))
            .displayName("Trading")
            .description("Trade with villagers to gain Trading XP")
            .item(ItemContext.builder()
                    .material("emerald")
                    .pos("4,4")
                    .build())
            .build();
    */

    
    public static final CustomSkill NUTRITION = CustomSkill
            .builder(NamespacedId.of(pluginName, "nutrition"))
            .displayName("Nutrición")
            .description("Reduce la faltiga y efectos por desnutrición.")
            .item(ItemContext.builder()
                    .material("APPLE")
                    .pos("1,2")
                    .build())
            .build();

    public static final CustomSkill HYDRATION = CustomSkill
            .builder(NamespacedId.of(pluginName, "hydration"))
            .displayName("Hidratación")
            .description("Reduce la fatiga y tasa de deshidratación.")
            .item(ItemContext.builder()
                    .material("WATER_BUCKET")
                    .pos("1,6")
                    .build())
            .build();

    public static final CustomSkill ENDURANCE = CustomSkill
            .builder(NamespacedId.of(pluginName, "endurance"))
            .displayName("Resistencia")
            .description("Reduce la fatiga por agotamiento.")
            .item(ItemContext.builder()
                    .material("LEATHER_BOOTS")
                    .pos("4,2")
                    .build())
            .build();

    public static final CustomSkill VITALITY = CustomSkill
            .builder(NamespacedId.of(pluginName, "vitality"))
            .displayName("Vitalidad")
            .description("Aumenta la vida máxima y resistencia al daño.")
            .item(ItemContext.builder()
                    .material("GOLDEN_APPLE")
                    .pos("2,4")
                    .build())
            .build();

    public static final CustomSkill FORTITUDE = CustomSkill
            .builder(NamespacedId.of(pluginName, "fortitude"))
            .displayName("Fortaleza")
            .description("Resistencia al frío, calor y radiación.")
            .item(ItemContext.builder()
                    .material("SHIELD")
                    .pos("3,6")
                    .build())
            .build();

    public static final CustomSkill RECOVERY = CustomSkill
            .builder(NamespacedId.of(pluginName, "recovery"))
            .displayName("Recuperación")
            .description("Mejora la curación y recuperación de heridas.")
            .item(ItemContext.builder()
                    .material("GLOW_BERRIES")
                    .pos("5,4")
                    .build())
            .build();

    public static final CustomSkill STAMINA = CustomSkill
            .builder(NamespacedId.of(pluginName, "stamina"))
            .displayName("Estamina")
            .description("Aumenta la estamina máxima y su recuperación.")
            .item(ItemContext.builder()
                    .material("SUGAR")
                    .pos("6,2")
                    .build())
            .build();
}
