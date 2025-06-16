package com.darkbladedev.mechanics;

import com.darkbladedev.WinterfallMain;

import org.bukkit.Bukkit;

import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Sistema que maneja la nevada tóxica de "El Eternauta"
 * La nieve es mortal para cualquiera que no esté protegido adecuadamente
 */
public class SnowfallSystem {

    private final WinterfallMain plugin;
    private final Random random;
    private BukkitTask snowfallTask;
    private BukkitTask damageTask;
    private final Map<UUID, Integer> exposureLevel;
    private final List<String> enabledWorlds;
    private boolean isActive;
    
    /**
     * Constructor del sistema de nevada
     * @param plugin Instancia del plugin principal
     */
    public SnowfallSystem(WinterfallMain plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.exposureLevel = new HashMap<>();
        this.enabledWorlds = new ArrayList<>();
        this.isActive = false;
        
        // Por defecto, la nevada está activa en el mundo normal
        enabledWorlds.add("world");
    }
    
    /**
     * Inicializa el sistema de nevada
     */
    public void initialize() {
        startSnowfallEffect();
        startDamageSystem();
        isActive = true;
        
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " Sistema de nevada mortal inicializado"));
    }
    
    /**
     * Inicia el efecto visual de la nevada
     */
    private void startSnowfallEffect() {
        snowfallTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (String worldName : enabledWorlds) {
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) continue;
                    
                    for (Player player : world.getPlayers()) {
                        // Solo mostrar partículas si el jugador está en un área expuesta
                        if (!isPlayerProtectedFromWeather(player)) {
                            Location loc = player.getLocation();
                            
                            // Crear efecto de nieve alrededor del jugador
                            for (int i = 0; i < 15; i++) {
                                double x = loc.getX() + (random.nextDouble() * 20) - 10;
                                double y = loc.getY() + 10;
                                double z = loc.getZ() + (random.nextDouble() * 20) - 10;
                                
                                world.spawnParticle(Particle.SNOWFLAKE, x, y, z, 1, 0, -0.5, 0, 0.01);
                            }
                            
                            // Mensaje de advertencia periódico
                            if (random.nextInt(100) < 1) { // 1% de probabilidad cada tick
                                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>¡La nieve tóxica cae sobre ti! Necesitas protección."));
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Ejecutar cada 0.5 segundos
    }
    
    /**
     * Inicia el sistema de daño por exposición a la nevada
     */
    private void startDamageSystem() {
        damageTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (String worldName : enabledWorlds) {
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) continue;
                    
                    for (Player player : world.getPlayers()) {
                        UUID playerId = player.getUniqueId();
                        
                        // Verificar si el jugador está protegido
                        if (!isPlayerFullyProtected(player) && !isPlayerInShelter(player)) {
                            // Aumentar nivel de exposición
                            int currentLevel = exposureLevel.getOrDefault(playerId, 0);
                            exposureLevel.put(playerId, currentLevel + 1);
                            
                            // Aplicar efectos según nivel de exposición
                            applyExposureEffects(player, currentLevel);
                        } else {
                            // Reducir nivel de exposición gradualmente si está protegido
                            int currentLevel = exposureLevel.getOrDefault(playerId, 0);
                            if (currentLevel > 0) {
                                exposureLevel.put(playerId, currentLevel - 1);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Ejecutar cada segundo
    }
    
    /**
     * Aplica los efectos de exposición a la nevada según el nivel
     * @param player Jugador afectado
     * @param level Nivel de exposición
     */
    private void applyExposureEffects(Player player, int level) {
        // Nivel 1-5: Efectos leves (lentitud, debilidad)
        if (level >= 1) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
        }
        
        // Nivel 6-10: Efectos moderados (náusea, más lentitud)
        if (level >= 6) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
        }
        
        // Nivel 11-15: Efectos graves (daño, ceguera)
        if (level >= 11) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            player.damage(10.0); // 5 corazones de daño
        }
        
        // Nivel 16+: Efectos mortales (daño severo)
        if (level >= 16) {
            player.damage(15.0); // 7.5 corazones de daño
            player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>¡La nieve tóxica está penetrando en tu piel! ¡Necesitas protección urgentemente!"));
        }
    }
    
    /**
     * Verifica si el jugador está completamente protegido contra la nevada
     * @param player Jugador a verificar
     * @return true si está protegido, false en caso contrario
     */
    public boolean isPlayerFullyProtected(Player player) {
        // Verificar si el jugador tiene el traje aislante completo
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        
        // Por ahora, verificamos si tiene armadura completa (se mejorará con items personalizados)
        return helmet != null && chestplate != null && leggings != null && boots != null;
    }
    
    /**
     * Verifica si el jugador está protegido del clima (bajo techo)
     * @param player Jugador a verificar
     * @return true si está bajo techo, false en caso contrario
     */
    public boolean isPlayerProtectedFromWeather(Player player) {
        Location loc = player.getLocation();
        return loc.getWorld().getHighestBlockYAt(loc) > loc.getY();
    }
    
    /**
     * Verifica si el jugador está en un refugio seguro
     * @param player Jugador a verificar
     * @return true si está en un refugio, false en caso contrario
     */
    public boolean isPlayerInShelter(Player player) {
        Location loc = player.getLocation();
        int playerY = loc.getBlockY();
        
        // Verificar si hay bloques sólidos sobre el jugador (techo)
        for (int y = playerY + 1; y < playerY + 4 && y < 256; y++) {
            Location checkLoc = new Location(loc.getWorld(), loc.getX(), y, loc.getZ());
            if (checkLoc.getBlock().getType().isSolid()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Activa o desactiva la nevada en un mundo específico
     * @param worldName Nombre del mundo
     * @param enabled true para activar, false para desactivar
     */
    public void setWorldEnabled(String worldName, boolean enabled) {
        if (enabled && !enabledWorlds.contains(worldName)) {
            enabledWorlds.add(worldName);
        } else if (!enabled) {
            enabledWorlds.remove(worldName);
        }
    }
    
    /**
     * Verifica si la nevada está activa en un mundo específico
     * @param worldName Nombre del mundo
     * @return true si está activa, false en caso contrario
     */
    public boolean isWorldEnabled(String worldName) {
        return enabledWorlds.contains(worldName);
    }
    
    /**
     * Detiene el sistema de nevada
     */
    public void shutdown() {
        if (snowfallTask != null) {
            snowfallTask.cancel();
        }
        
        if (damageTask != null) {
            damageTask.cancel();
        }
        
        isActive = false;
    }
    
    /**
     * Verifica si el sistema de nevada está activo
     * @return true si está activo, false en caso contrario
     */
    public boolean isActive() {
        return isActive;
    }
}
