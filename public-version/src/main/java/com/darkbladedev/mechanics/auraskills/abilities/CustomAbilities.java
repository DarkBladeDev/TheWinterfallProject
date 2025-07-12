package com.darkbladedev.mechanics.auraskills.abilities;

import java.io.File;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.ability.CustomAbility;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;

public class CustomAbilities {

    public static void registerAbilities(AuraSkillsApi api, File contentFolder) {
        NamespacedRegistry registry = api.useRegistry("savage-frontier", contentFolder);
        
        // LIMB SYSTEM
        registry.registerAbility(HardBones);

        // STAMINA SYSTEM
        registry.registerAbility(StaminaRegen);
        registry.registerAbility(Atlethic);

        // NUTRITION SYSTEM
        registry.registerAbility(EnergyReserve);
    }

    // LIMB DAMAGE SYSTEM ABILITIES
    public static final CustomAbility HardBones = CustomAbility.builder(NamespacedId.of("savage-frontier", "hardbones"))
                                            .displayName("Huesos duros")
                                            .description("Disminuye un {value}% el daño a extremidades")
                                            .info("-{value}% de daño a extremidades")
                                            .baseValue(1.0) // Value when at level 1
                                            .valuePerLevel(0.5) // Value added per ability level
                                            .unlock(5) // Skill level ability unlocks at
                                            .levelUp(10) // Skill level interval between ability level ups
                                            .maxLevel(0) // 0 = unlimited max level, but capper by the max skill level

                                             .build();


    // STAMINA SYSTEM ABILITIES
    public static final CustomAbility StaminaRegen = CustomAbility.builder(NamespacedId.of("savage-frontier", "stamina_regen"))
                                            .displayName("Regeneración de stamina")
                                            .description("Regenera {value} de stamina extra por segundo")
                                            .info("+{value} Regen. de stamina extra")
                                            .baseValue(1.0) // Value when at level 1
                                            .valuePerLevel(0.5) // Value added per ability level
                                            .unlock(5) // Skill level ability unlocks at
                                            .levelUp(10) // Skill level interval between ability level ups
                                            .maxLevel(0) // 0 = unlimited max level, but capper by the max skill level
                                            .build();

              
    public static final CustomAbility Atlethic = CustomAbility.builder(NamespacedId.of("savage-frontier", "atlethic"))
                                            .displayName("Atlético")
                                            .description("Recupera {value} de estamina después de correr por 8s")
                                            .info("{value} Recuperación")
                                            .baseValue(2.0) // Value when at level 1
                                            .valuePerLevel(1) // Value added per ability level
                                            .unlock(15) // Skill level ability unlocks at
                                            .levelUp(10) // Skill level interval between ability level ups
                                            .maxLevel(0) // 0 = unlimited max level, but capper by the max skill level
                                            .build();


    // NUTRITION SYSTEM ABILITIES
    public static final CustomAbility EnergyReserve = CustomAbility.builder(NamespacedId.of("savage-frontier", "energy_reserve"))
                                            .displayName("Reserva de energía")
                                            .description("Los nutrientes duran un {value}% más")
                                            .info("{value}% Duración de nutrientes")
                                            .baseValue(1.0) // Value when at level 1
                                            .valuePerLevel(0.5) // Value added per ability level
                                            .unlock(10) // Skill level ability unlocks at
                                            .levelUp(5) // Skill level interval between ability level ups
                                            .maxLevel(0) // 0 = unlimited max level, but capper by the max skill level
                                            .build();


}