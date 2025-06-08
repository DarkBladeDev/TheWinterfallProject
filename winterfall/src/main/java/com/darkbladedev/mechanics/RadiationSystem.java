package com.darkbladedev.mechanics;

import com.darkbladedev.WinterfallMain;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sistema que maneja la radiación en "El Eternauta"
 * Simula los efectos de la radiación en áreas contaminadas
 */
public class RadiationSystem {

    private final WinterfallMain plugin;
    private BukkitTask radiationTask;
    private final Map<UUID, Integer> radiationLevel;
    private boolean isActive;
    
    /**
     * Constructor del sistema de radiación
     * @param plugin Instancia del plugin principal
     */
    public RadiationSystem(WinterfallMain plugin) {
        this.plugin = plugin;
        this.radiationLevel = new HashMap<>();
        this.isActive = false;
    }
    
    /**
     * Inicializa el sistema de radiación
     */
    public void initialize() {
        startRadiationSystem();
        isActive = true;
        
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Winterfall] Sistema de radiación inicializado");
    }
    
    /**
     * Inicia el sistema de radiación
     */
    private void startRadiationSystem() {
        radiationTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Player player : world.getPlayers()) {
                        // Verificar si el jugador está en una zona radiactiva
                        if (isInRadiationZone(player.getLocation())) {
                            UUID playerId = player.getUniqueId();
                            int currentLevel = radiationLevel.getOrDefault(playerId, 0);
                            
                            // Aumentar nivel de radiación
                            radiationLevel.put(playerId, currentLevel + 1);
                            
                            // Aplicar efectos según nivel de radiación
                            applyRadiationEffects(player, currentLevel);
                            
                            // Efectos visuales de radiación
                            showRadiationEffects(player);
                        } else {
                            // Reducir nivel de radiación gradualmente si no está en zona radiactiva
                            UUID playerId = player.getUniqueId();
                            int currentLevel = radiationLevel.getOrDefault(playerId, 0);
                            if (currentLevel > 0) {
                                radiationLevel.put(playerId, currentLevel - 1);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Ejecutar cada segundo
    }
    
    /**
     * Verifica si una ubicación está en una zona radiactiva
     * @param location Ubicación a verificar
     * @return true si está en zona radiactiva, false en caso contrario
     */
    public boolean isInRadiationZone(Location location) {
        // Por ahora, consideramos zonas radiactivas basadas en coordenadas específicas
        // Esto se puede mejorar con un sistema de regiones personalizado
        
        // Ejemplo: considerar áreas con coordenadas X e Z negativas como radiactivas
        return location.getX() < 0 && location.getZ() < 0;
    }
    
    /**
     * Aplica los efectos de radiación según el nivel
     * @param player Jugador afectado
     * @param level Nivel de radiación
     */
    private void applyRadiationEffects(Player player, int level) {
        // Nivel 1-5: Efectos leves (náusea, hambre)
        if (level >= 1) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 0));
            
            if (level == 1) {
                player.sendMessage(ChatColor.YELLOW + "Sientes un leve mareo... parece que hay radiación en esta zona.");
            }
        }
        
        // Nivel 6-10: Efectos moderados (debilidad, más náusea)
        if (level >= 6) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 160, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
            
            if (level == 6) {
                player.sendMessage(ChatColor.GOLD + "La radiación está afectando tu cuerpo. Deberías salir de esta zona.");
            }
        }
        
        // Nivel 11-15: Efectos graves (veneno, ceguera)
        if (level >= 11) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
            
            if (level == 11) {
                player.sendMessage(ChatColor.RED + "¡La radiación está dañando tus órganos! ¡Debes salir inmediatamente!");
            }
        }
        
        // Nivel 16+: Efectos mortales (wither, daño directo)
        if (level >= 16) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
            player.damage(1.0); // 0.5 corazones de daño
            
            if (level % 5 == 0) { // Mensaje cada 5 niveles a partir del 16
                player.sendMessage(ChatColor.DARK_RED + "¡La radiación está matándote! ¡Necesitas tratamiento médico urgente!");
            }
        }
    }
    
    /**
     * Muestra efectos visuales de radiación alrededor del jugador
     * @param player Jugador afectado
     */
    private void showRadiationEffects(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        
        // Partículas verdes para simular radiación
        for (int i = 0; i < 10; i++) {
            double x = loc.getX() + (Math.random() * 2) - 1;
            double y = loc.getY() + (Math.random() * 2);
            double z = loc.getZ() + (Math.random() * 2) - 1;
            
            world.spawnParticle(Particle.ANGRY_VILLAGER, x, y, z, 0, 0.1, 0.1, 0.1, 1);
        }
    }
    
    /**
     * Obtiene el nivel de radiación de un jugador
     * @param player Jugador a verificar
     * @return Nivel de radiación
     */
    public int getRadiationLevel(Player player) {
        return radiationLevel.getOrDefault(player.getUniqueId(), 0);
    }
    
    /**
     * Obtiene el nivel de radiación de un jugador en porcentaje (0-100)
     * @param player Jugador a verificar
     * @return Nivel de radiación en porcentaje
     */
    public int getPlayerRadiationLevel(Player player) {
        int level = getRadiationLevel(player);
        // Convertir el nivel a un porcentaje (considerando 20 como el 100%)
        return Math.min(100, (level * 5));
    }
    
    /**
     * Establece el nivel de radiación de un jugador
     * @param player Jugador a modificar
     * @param level Nuevo nivel de radiación
     */
    public void setRadiationLevel(Player player, int level) {
        radiationLevel.put(player.getUniqueId(), level);
    }
    
    /**
     * Detiene el sistema de radiación
     */
    public void shutdown() {
        if (radiationTask != null) {
            radiationTask.cancel();
        }
        
        isActive = false;
    }
    
    /**
     * Verifica si el sistema de radiación está activo
     * @return true si está activo, false en caso contrario
     */
    public boolean isActive() {
        return isActive;
    }

    public int getMaxRadiationLevel() {
        return 20;
    }
}