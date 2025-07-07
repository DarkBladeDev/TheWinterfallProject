package com.darkbladedev.utils;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.registry.GlobalRegistry;
import dev.aurelium.auraskills.api.stat.CustomStat;
import dev.aurelium.auraskills.api.trait.CustomTrait;
import dev.aurelium.auraskills.api.user.SkillsUser;
import org.bukkit.entity.Player;

/**
 * Utilidad para consultar niveles de stats personalizados de AuraSkills de forma segura.
 */
public class AuraSkillsUtil {
    /**
     * Obtiene el nivel de un stat personalizado de AuraSkills para un jugador.
     * @param player Jugador
     * @param statKey Clave del stat personalizado (por ejemplo, "vitality", "endurance")
     * @return Nivel del stat, o 0 si no se puede obtener
     */
    public static int getCustomStatLevel(Player player, String statKey) {
        try {
            AuraSkillsApi api = AuraSkillsApi.get();
            if (api == null || player == null) return 0;
            SkillsUser user = api.getUserManager().getUser(player.getUniqueId());
            if (user == null) return 0;
            GlobalRegistry registry = api.getGlobalRegistry();
            NamespacedId id = NamespacedId.of("savage-frontier", statKey);
            CustomStat stat = (CustomStat) registry.getStat(id);
            if (stat == null) return 0;
            return (int) user.getStatLevel(stat);
        } catch (Exception e) {
            return 0;
        }
    }
    /**
     * Obtiene el nivel de un stat personalizado de AuraSkills para un jugador (usando CustomStat).
     * @param player Jugador
     * @param stat Objeto CustomStat
     * @return Nivel del stat, o 0 si no se puede obtener
     */
    public static int getCustomStatLevel(Player player, CustomStat stat) {
        try {
            if (player == null || stat == null) return 0;
            SkillsUser user = dev.aurelium.auraskills.api.AuraSkillsApi.get().getUserManager().getUser(player.getUniqueId());
            if (user == null) return 0;
            return (int) user.getStatLevel(stat);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Crea y retorna los traits y stats personalizados para el sistema de estamina.
     * @param plugin Instancia principal del plugin (puede ser null si no se usa)
     * @return Un array: [CustomTrait staminaCapacity, CustomTrait staminaRecovery, CustomStat enduranceStat]
     */
    public static Object[] createStaminaTraitsAndStat() {
        CustomTrait staminaCapacityTrait = CustomTrait.builder(NamespacedId.of("savage-frontier", "stamina_capacity"))
                .displayName("Capacidad de Estamina")
                .build();
        CustomTrait staminaRecoveryTrait = CustomTrait.builder(NamespacedId.of("savage-frontier", "stamina_recovery"))
                .displayName("Recuperación de Estamina")
                .build();
        CustomStat enduranceStat = CustomStat.builder(NamespacedId.of("savage-frontier", "endurance"))
                .displayName("Resistencia")
                .description("Mejora tu capacidad y recuperación de estamina")
                .symbol("➾")
                .color("<aqua>")
                .item(
                    dev.aurelium.auraskills.api.item.ItemContext.builder()
                        .material("iron_boots")
                        .group("lower")
                        .order(2)
                        .build()
                )
                .trait(staminaRecoveryTrait, 2.0)
                .trait(staminaCapacityTrait, 1.0)
                .build();
        return new Object[] { staminaCapacityTrait, staminaRecoveryTrait, enduranceStat };
    }

    /**
     * Registra los traits y stats personalizados en AuraSkills.
     * @param registry Registry de AuraSkills
     * @param staminaCapacityTrait Trait de capacidad de estamina
     * @param staminaRecoveryTrait Trait de recuperación de estamina
     * @param enduranceStat Stat de resistencia
     */
    public static void registerStaminaTraitsAndStat(dev.aurelium.auraskills.api.registry.NamespacedRegistry registry,
                                                   CustomTrait staminaCapacityTrait,
                                                   CustomTrait staminaRecoveryTrait,
                                                   CustomStat enduranceStat) {
        registry.registerTrait(staminaCapacityTrait);
        registry.registerTrait(staminaRecoveryTrait);
        registry.registerStat(enduranceStat);
    }
}
