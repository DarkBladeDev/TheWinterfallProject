package com.darkbladedev.mechanics;

import com.darkbladedev.SavageFrontierMain;

import org.bukkit.Bukkit;

import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkbladedev.CustomTypes.CustomEnchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sistema que maneja la radiación en "El Eternauta"
 * Simula los efectos de la radiación en áreas contaminadas
 */
public class RadiationSystem {

    private final SavageFrontierMain plugin;
    private BukkitTask radiationTask;
    private final Map<UUID, Integer> radiationLevel;
    private boolean isActive;
    
    /**
     * Constructor del sistema de radiación
     * @param plugin Instancia del plugin principal
     */
    public RadiationSystem(SavageFrontierMain plugin) {
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
        
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " Sistema de radiación inicializado"));
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
                        // Verificar si el jugador está protegido como nuevo jugador
                        if (plugin.isPlayerProtectedFromSystem(player, "radiation")) {
                            continue; // No aplicar radiación si el jugador está protegido
                        }
                        
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
        // Verificar si el jugador está protegido como nuevo jugador
        if (plugin.isPlayerProtectedFromSystem(player, "radiation")) {
            return; // No aplicar efectos si el jugador está protegido
        }
        
        // Verificar si el jugador tiene protección contra radiación
        int protectionLevel = getRadiationProtectionLevel(player);
        
        // Reducir el nivel de radiación según el nivel de protección
        // Cada nivel de protección reduce un 25% los efectos
        int adjustedLevel = Math.max(0, level - (int)(level * (protectionLevel * 0.25)));
        
        // Si tiene protección completa (nivel 4), mostrar mensaje y salir
        if (protectionLevel >= 4 && level > 0) {
            if (level % 10 == 0 && plugin.getUserPreferencesManager().hasStatusMessages(player)) { // Mostrar mensaje ocasionalmente
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Tu protección te está protegiendo completamente de la radiación."));
            }
            return;
        }
        
        // Nivel 1-5: Efectos leves (náusea, hambre)
        if (adjustedLevel >= 1) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 0));
            
            if (plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                if (adjustedLevel == 1 && protectionLevel == 0) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Sientes un leve mareo... parece que hay radiación en esta zona."));
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<yellow>Parece que hay radiación en esta zona."));
                } else if (adjustedLevel == 1 && protectionLevel > 0) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Tu protección contra radiación está reduciendo los efectos, pero aún sientes algo de mareo."));
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<yellow>Tu protección contra radiación está reduciendo los efectos."));

                }
            }
        }
        
        // Nivel 6-10: Efectos moderados (debilidad, más náusea)
        if (adjustedLevel >= 6) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 160, 0));
            plugin.getCustomDebuffEffects().applyWeakness(player);
            //player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
            
            if (adjustedLevel == 6 && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<gold>La radiación está afectando tu cuerpo. Deberías salir de esta zona."));
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<gold>La radiación está afectando tu cuerpo."));
            }
        }
        
        // Nivel 11-15: Efectos graves (veneno, ceguera)
        if (adjustedLevel >= 11) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
            
            if (adjustedLevel == 11 && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>¡La radiación está dañando tus órganos! ¡Debes salir inmediatamente!"));
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>¡La radiación está dañando tus órganos!"));
            }
        }
        
        // Nivel 16+: Efectos mortales (wither, daño directo)
        if (adjustedLevel >= 16) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
            player.damage(1.0); // 0.5 corazones de daño
            
            if (adjustedLevel % 5 == 0 && plugin.getUserPreferencesManager().hasStatusMessages(player)) { // Mensaje cada 5 niveles a partir del 16
                player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>¡La radiación está matándote! ¡Necesitas tratamiento médico urgente!"));
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<dark_red>¡La radiación está matándote!"));
            }
        }
    }
    
    /**
     * Obtiene el nivel de protección contra radiación del jugador
     * @param player Jugador a verificar
     * @return Nivel de protección contra radiación (0-4)
     */
    private int getRadiationProtectionLevel(Player player) {
        int protectionLevel = 0;
        
        // Verificar cada pieza de armadura
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack item : armor) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasEnchant(CustomEnchantments.getRadiationProtectionEnchantment())) {
                    protectionLevel += meta.getEnchantLevel(CustomEnchantments.getRadiationProtectionEnchantment());
                }
            }
        }
        
        // Limitar el nivel máximo de protección a 4
        return Math.min(4, protectionLevel);
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