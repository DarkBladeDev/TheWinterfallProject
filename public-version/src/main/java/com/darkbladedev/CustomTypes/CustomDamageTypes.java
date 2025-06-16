package com.darkbladedev.CustomTypes;

import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.darkbladedev.SavageFrontierMain;

import io.papermc.paper.registry.RegistryAccess;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Clase que proporciona métodos para crear fuentes de daño personalizadas
 * utilizando la API de Paper para tipos de daño.
 */
public class CustomDamageTypes {
    
    private static SavageFrontierMain plugin;

    // Claves para los tipos de daño personalizados
    private static final Key BLEEDING_KEY = Key.key("savage-frontier:bleeding");
    private static final Key DEHYDRATION_KEY = Key.key("savage-frontier:dehydration");
    private static final Key FREEZING_KEY = Key.key("savage-frontier:freezing");
    
    /**
     * Crea una fuente de daño de sangrado
     * @param source La entidad que causa el daño (puede ser null)
     * @param target La entidad que recibe el daño
     * @return La fuente de daño personalizada
     */
    @SuppressWarnings("removal")
    public static DamageSource createBleedingDamageSource(Entity source, Entity target) {
        try {
            DamageType damageType = RegistryAccess.registryAccess().getRegistry(DamageType.class).get(BLEEDING_KEY);
            if (damageType == null) {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <dark_red>Error: <red>No se pudo encontrar el tipo de daño 'bleeding'. Usando daño genérico."));
                return DamageSource.builder(DamageType.GENERIC).build();
            }

            if (source == null) {
                source = target;
            }
            
            return DamageSource.builder(damageType)
                    .withCausingEntity(source)
                    .withDirectEntity(target)
                    .build();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al crear DamageSource de sangrado: " + e.getMessage()));
            return DamageSource.builder(DamageType.GENERIC).build();
        }
    }
    
    /**
     * Crea una fuente de daño de deshidratación
     * @param player El jugador que sufre la deshidratación
     * @return La fuente de daño personalizada
     */
    @SuppressWarnings("removal")
    public static DamageSource createDehydrationDamageSource(Player player) {
        try {
            DamageType damageType = RegistryAccess.registryAccess().getRegistry(DamageType.class).get(DEHYDRATION_KEY);
            if (damageType == null) {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + "[Winterfall] <dark_red>Error: <red>No se pudo encontrar el tipo de daño 'dehydration'. Usando daño genérico."));
                return DamageSource.builder(DamageType.GENERIC).build();
            }
            
            return DamageSource.builder(damageType)
                    .withDirectEntity(player)
                    .build();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al crear DamageSource de deshidratación: " + e.getMessage()));
            return DamageSource.builder(DamageType.GENERIC).build();
        }
    }
    
    /**
     * Crea una fuente de daño de congelación
     * @param attacker La entidad que causa el daño por congelación
     * @param target La entidad que recibe el daño
     * @return La fuente de daño personalizada
     */
    @SuppressWarnings("removal")
    public static DamageSource createFreezingDamageSource(Entity attacker, Entity target) {
        try {
            DamageType damageType = RegistryAccess.registryAccess().getRegistry(DamageType.class).get(FREEZING_KEY);
            if (damageType == null) {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <dark_red>Error: <red>No se pudo encontrar el tipo de daño 'freezing'. Usando daño genérico."));
                return DamageSource.builder(DamageType.GENERIC).build();
            }
            
            return DamageSource.builder(damageType)
                    .withCausingEntity(attacker)
                    .withDirectEntity(target)
                    .build();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al crear DamageSource de congelación: " + e.getMessage()));
            return DamageSource.builder(DamageType.GENERIC).build();
        }
    }
}