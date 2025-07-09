package com.darkbladedev.utils;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import dev.aurelium.auraskills.api.registry.GlobalRegistry;
import dev.aurelium.auraskills.api.stat.CustomStat;
import dev.aurelium.auraskills.api.trait.CustomTrait;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.user.SkillsUser;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.mechanics.auraskills.stats.CustomStats;

import com.darkbladedev.mechanics.*;

/**
 * Utilidad para consultar valores custom de AuraSkills de forma segura.
 */
public class AuraSkillsUtil {
    private static SavageFrontierMain plugin = SavageFrontierMain.getInstance();
    private static AuraSkillsApi api = plugin.getAuraSkillsApi();
    private static StaminaSystem staminaSystem = plugin.getStaminaSystem();
    
    /**
     * Obtiene el nivel de un stat personalizado de AuraSkills para un jugador.
     * @param player Jugador
     * @param statKey Clave del stat personalizado (por ejemplo, "vitality", "endurance")
     * @return Nivel del stat, o 0 si no se puede obtener
     */
    public static int getCustomStatLevel(Player player, String statKey) {
        try {
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


    public boolean verifyPlayerTraits(Player player, CustomStat stat) {
        if (!plugin.getStaminaAuraSkillsIntegration().isEnabled() || api == null || player == null) {
            return false;
        }
        
        try {
            SkillsUser user = api.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.PREFIX + " <red>No se pudo obtener el usuario de AuraSkills para " + player.getName()
                ));
                return false;
            }
            
            // Obtener los modificadores de estamina directamente desde AuraSkills
            double capacityModifier = user.getTraitModifier("savage-frontier:stamina_capacity").value();
            double recoveryModifier = user.getTraitModifier("savage-frontier:stamina_recovery").value();
            
            // Obtener el nivel de resistencia personalizado
            int enduranceLevel = (int) user.getStatLevel(CustomStats.Endurance);
            
            // Verificar si los modificadores están aplicados en el sistema de estamina
            double appliedCapacity = staminaSystem.getMaxStaminaModifier(player, "auraskills");
            double appliedRecovery = staminaSystem.getRecoveryRateModifier(player, "auraskills");
            
            StringBuilder statusMessage = new StringBuilder();
            statusMessage.append(plugin.PREFIX).append(" <yellow>Resumen de ").append(player.getName()).append(":\n");
            statusMessage.append("  <gray>- <green>Stat: ").append(stat.name()).append("\n");
            for (Trait trait : stat.getTraits()) {
                statusMessage.append("    <gray>- <light_purple>Atributo</light_purple> <gray>(Trait): ").append(trait.name()).append("<gray>(Lvl. ").append(user.getEffectiveTraitLevel(trait)).append(")").append("\n");
            }
            statusMessage.append("<gray>---------------------------------------------------------------------");

            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(statusMessage.toString()));
            
            // Verificar si hay discrepancias significativas
            boolean capacityMatch = (Math.abs(capacityModifier - appliedCapacity) < 0.1) || 
                                   (enduranceLevel > 0 && appliedCapacity > 0);
            boolean recoveryMatch = (Math.abs(recoveryModifier - appliedRecovery) < 0.01) || 
                                   (enduranceLevel > 0 && appliedRecovery > 0);
            
            return capacityMatch && recoveryMatch;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.PREFIX + " <red>Error al verificar traits para " + player.getName() + ": " + e.getMessage()
            ));
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
            return false;
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

    public static void registerStatsAndTraits(NamespacedRegistry registry, CustomStat stat, CustomTrait... traits) {
        if (stat != null) {
            registry.registerStat(stat);
        }
        for (CustomTrait trait : traits) {
            if (trait != null) {
                registry.registerTrait(trait);
            }
        }
    }
}
