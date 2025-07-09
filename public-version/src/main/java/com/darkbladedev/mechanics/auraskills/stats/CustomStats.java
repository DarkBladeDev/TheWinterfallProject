package com.darkbladedev.mechanics.auraskills.stats;

import org.bukkit.Material;

import com.darkbladedev.mechanics.auraskills.traits.CustomTraits;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import dev.aurelium.auraskills.api.stat.CustomStat;
import dev.aurelium.auraskills.api.stat.Stat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CustomStats {
    
    private final Map<String, CustomStat> stats;

    public CustomStats() {
        this.stats = new HashMap<>();
    }

    public void registerStats(AuraSkillsApi api, File contentFolder) {
        NamespacedRegistry registry = api.useRegistry("savage-frontier", contentFolder);

        registry.registerStat(Endurance);
        registry.registerStat(Toughness);

        stats.put("endurance", Endurance);
        stats.put("toughness", Toughness);
    }

    public static final CustomStat Endurance = CustomStat.builder(NamespacedId.of("savage-frontier", "endurance"))
                                            .displayName("Resistencia")
                                            .description("Resistencia física")
                                            .color("<green>")
                                            .item(
                                                ItemContext.builder()
                                                .material(Material.LEATHER_BOOTS.toString())
                                                .group("lower")
                                                .order(2)
                                                .build()
                                            )
                                            .trait(CustomTraits.staminaCapacity, 0.5)
                                            .trait(CustomTraits.staminaRecovery, 0.2)
                                            .build();

    public static final CustomStat Toughness = CustomStat.builder(NamespacedId.of("savage-frontier", "toughness"))
                                            .displayName("Dureza")
                                            .description("Dureza corporal")
                                            .color("<red>")
                                            .item(
                                                ItemContext.builder()
                                                .material(Material.IRON_CHESTPLATE.toString())
                                                .group("lower")
                                                .order(6)
                                                .build()
                                            )
                                            .trait(CustomTraits.limbDamageReduction, 0.1)
                                            .trait(CustomTraits.limbRecoveryRate, 0.05)
                                            .build();

    public CustomStat getStat(String name) {
        AuraSkillsApi api = AuraSkillsApi.get();
        CustomStat stat = (CustomStat) api.getGlobalRegistry().getStat(NamespacedId.of("savage-frontier", name));
        if (stat == null) {
            throw new IllegalArgumentException("Stat not found: " + name);
        }
        return stat;
    }

    public Map<String, CustomStat> getStats() {
        AuraSkillsApi api = AuraSkillsApi.get();
        for (Stat stat : api.getGlobalRegistry().getStats()) {
            stats.put(stat.getId().getKey(), (CustomStat) stat);
        }
        return stats;
    }
}
