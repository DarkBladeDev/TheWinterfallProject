package com.darkbladedev.mechanics.auraskills.skills;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import com.darkbladedev.SavageFrontierMain;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import dev.aurelium.auraskills.api.skill.CustomSkill;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class CustomSkills {
    private static final String pluginName = "savage-frontier";
    private final SavageFrontierMain plugin;

    public CustomSkills(SavageFrontierMain plugin) {
        this.plugin = plugin;
    }


    public void registerCustomSkills(AuraSkillsApi api, File contentFolder) {
        if (api == null) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error: AuraSkillsApi es null"));
            return;
        }
        
        if (contentFolder == null) {
            // Si el directorio de contenido es null, crear uno por defecto
            contentFolder = new File(plugin.getDataFolder(), "auraskills");
            if (!contentFolder.exists()) {
                contentFolder.mkdirs();
            }
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Directorio de contenido no proporcionado, usando: " + contentFolder.getAbsolutePath()));
        }
        
        try {
            // Asegurarse de que el directorio existe
            if (!contentFolder.exists()) {
                contentFolder.mkdirs();
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Creando directorio de contenido: " + contentFolder.getAbsolutePath()));
            }
            
            // Usar useRegistry en lugar de getNamespacedRegistry
            NamespacedRegistry registry = api.useRegistry(pluginName, contentFolder);
            
            if (registry == null) {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error: No se pudo obtener el registro de AuraSkills"));
                return;
            }
            
            // Registrar habilidades personalizadas
            registry.registerSkill(NUTRITION);
            registry.registerSkill(HYDRATION);
            registry.registerSkill(ENDURANCE);
            registry.registerSkill(FORTITUDE);
            registry.registerSkill(RECOVERY);
            
            // Mensaje de éxito
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Habilidades personalizadas registradas correctamente."));
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al registrar habilidades personalizadas: " + e.getMessage()));
            e.printStackTrace();
        }
    }
    
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
                    .material(Material.GLOW_BERRIES.toString())
                    .pos("5,4")
                    .build())
            .build();

}
