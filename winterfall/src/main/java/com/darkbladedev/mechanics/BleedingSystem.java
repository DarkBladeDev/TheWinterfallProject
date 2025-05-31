package com.darkbladedev.mechanics;

import com.darkbladedev.WinterfallMain;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Sistema que maneja el sangrado en "El Eternauta"
 * Simula heridas causadas por los ataques de los invasores alienígenas
 */
public class BleedingSystem implements Listener {

    private final WinterfallMain plugin;
    private final Random random;
    private BukkitTask bleedingTask;
    private final Map<UUID, Integer> bleedingSeverity;
    private final Map<UUID, Integer> bleedingDuration;
    private boolean isActive;
    
    /**
     * Constructor del sistema de sangrado
     * @param plugin Instancia del plugin principal
     */
    public BleedingSystem(WinterfallMain plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.bleedingSeverity = new HashMap<>();
        this.bleedingDuration = new HashMap<>();
        this.isActive = false;
    }
    
    /**
     * Inicializa el sistema de sangrado
     */
    public void initialize() {
        startBleedingSystem();
        isActive = true;
        
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Winterfall] Sistema de sangrado inicializado");
    }
    
    /**
     * Inicia el sistema de sangrado
     */
    private void startBleedingSystem() {
        bleedingTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Procesar a todos los jugadores con sangrado activo
                for (UUID entityId : bleedingSeverity.keySet()) {
                    // Obtener entidad
                    Entity entity = Bukkit.getEntity(entityId);
                    if (entity == null || !(entity instanceof LivingEntity)) {
                        // Eliminar entidades que ya no existen
                        bleedingSeverity.remove(entityId);
                        bleedingDuration.remove(entityId);
                        continue;
                    }
                    
                    LivingEntity livingEntity = (LivingEntity) entity;
                    
                    // Obtener severidad y duración
                    int severity = bleedingSeverity.get(entityId);
                    int duration = bleedingDuration.get(entityId);
                    
                    // Aplicar efectos de sangrado
                    applyBleedingEffects(livingEntity, severity);
                    
                    // Reducir duración
                    duration--;
                    if (duration <= 0) {
                        // El sangrado ha terminado
                        bleedingSeverity.remove(entityId);
                        bleedingDuration.remove(entityId);
                        
                        if (livingEntity instanceof Player) {
                            Player player = (Player) livingEntity;
                            player.sendMessage(ChatColor.GREEN + "Tu sangrado se ha detenido.");
                        }
                    } else {
                        // Actualizar duración
                        bleedingDuration.put(entityId, duration);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Ejecutar cada segundo
    }
    
    /**
     * Aplica los efectos de sangrado según la severidad
     * @param entity Entidad afectada
     * @param severity Severidad del sangrado (1-3)
     */
    private void applyBleedingEffects(LivingEntity entity, int severity) {
        // Efectos visuales de sangrado
        Location loc = entity.getLocation();
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1.0f);
        
        // Partículas de sangrado (más intensas según severidad)
        for (int i = 0; i < severity * 3; i++) {
            double x = loc.getX() + (random.nextDouble() * 0.5) - 0.25;
            double y = loc.getY() + 1.0 + (random.nextDouble() * 0.5);
            double z = loc.getZ() + (random.nextDouble() * 0.5) - 0.25;
            
            loc.getWorld().spawnParticle(Particle.DUST, x, y, z, 1, 0.2, 0.2, 0.2, dustOptions);
        }
        
        // Efectos de sonido (probabilidad basada en severidad)
        if (random.nextInt(10) < severity * 2) {
            loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_HURT, 0.5f, 1.0f);
        }
        
        // Daño por sangrado según severidad
        if (random.nextInt(5) < severity) {
            entity.damage(severity * 0.5); // 0.5, 1.0 o 1.5 corazones de daño
        }
        
        // Efectos adicionales para jugadores
        if (entity instanceof Player) {
            Player player = (Player) entity;
            
            // Efectos de poción según severidad
            switch (severity) {
                case 1: // Leve
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
                    break;
                case 2: // Moderado
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
                    break;
                case 3: // Grave
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0));
                    break;
            }
            
            // Mensajes según severidad (con probabilidad para no spamear)
            if (random.nextInt(20) < severity) {
                switch (severity) {
                    case 1:
                        player.sendMessage(ChatColor.YELLOW + "Tienes una herida leve que está sangrando.");
                        break;
                    case 2:
                        player.sendMessage(ChatColor.GOLD + "Estás perdiendo sangre de forma moderada. Necesitas tratamiento.");
                        break;
                    case 3:
                        player.sendMessage(ChatColor.RED + "¡Estás sangrando gravemente! Necesitas atención médica urgente.");
                        break;
                }
            }
        }
    }
    
    /**
     * Aplica sangrado a una entidad
     * @param entity Entidad a la que aplicar sangrado
     * @param severity Severidad del sangrado (1-3)
     * @param duration Duración en segundos
     */
    public void applyBleeding(LivingEntity entity, int severity, int duration) {
        // Validar parámetros
        severity = Math.max(1, Math.min(3, severity)); // Entre 1 y 3
        duration = Math.max(5, duration); // Mínimo 5 segundos
        
        UUID entityId = entity.getUniqueId();
        
        // Si ya tiene sangrado, usar la severidad más alta
        if (bleedingSeverity.containsKey(entityId)) {
            int currentSeverity = bleedingSeverity.get(entityId);
            severity = Math.max(currentSeverity, severity);
            
            // Extender duración
            int currentDuration = bleedingDuration.get(entityId);
            duration = Math.max(currentDuration, duration);
        }
        
        // Aplicar sangrado
        bleedingSeverity.put(entityId, severity);
        bleedingDuration.put(entityId, duration);
        
        // Notificar al jugador
        if (entity instanceof Player) {
            Player player = (Player) entity;
            String severityText = "";
            
            switch (severity) {
                case 1:
                    severityText = ChatColor.YELLOW + "leve";
                    break;
                case 2:
                    severityText = ChatColor.GOLD + "moderado";
                    break;
                case 3:
                    severityText = ChatColor.RED + "grave";
                    break;
            }
            
            player.sendMessage(ChatColor.RED + "¡Has comenzado a sangrar! " + 
                    "Tienes un sangrado " + severityText + ChatColor.RED + ".");
        }
    }
    
    /**
     * Procesa un evento de daño para posiblemente aplicar sangrado
     * @param event Evento de daño
     */
    public void processDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        
        LivingEntity entity = (LivingEntity) event.getEntity();
        double damage = event.getFinalDamage();
        
        // Probabilidad de sangrado basada en el daño
        // Más daño = mayor probabilidad y severidad
        if (random.nextDouble() < (damage / 10.0)) { // 10% por corazón de daño
            int severity = 1;
            
            if (damage >= 6.0) { // 3+ corazones
                severity = 3;
            } else if (damage >= 3.0) { // 1.5+ corazones
                severity = 2;
            }
            
            // Duración basada en daño (entre 10 y 30 segundos)
            int duration = 10 + (int)(damage * 2);
            duration = Math.min(30, duration);
            
            // Aplicar sangrado
            applyBleeding(entity, severity, duration);
        }
    }
    
    /**
     * Detiene el sangrado de una entidad
     * @param entity Entidad a curar
     */
    public void stopBleeding(LivingEntity entity) {
        UUID entityId = entity.getUniqueId();
        
        if (bleedingSeverity.containsKey(entityId)) {
            bleedingSeverity.remove(entityId);
            bleedingDuration.remove(entityId);
            
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.sendMessage(ChatColor.GREEN + "Tu sangrado ha sido curado.");
            }
        }
    }
    
    /**
     * Verifica si una entidad está sangrando
     * @param entity Entidad a verificar
     * @return true si está sangrando, false en caso contrario
     */
    public boolean isBleeding(LivingEntity entity) {
        return bleedingSeverity.containsKey(entity.getUniqueId());
    }
    
    /**
     * Verifica si un jugador está sangrando
     * @param player Jugador a verificar
     * @return true si está sangrando, false en caso contrario
     */
    public boolean isPlayerBleeding(Player player) {
        return isBleeding(player);
    }
    
    /**
     * Obtiene el nivel de sangrado de un jugador
     * @param player Jugador a verificar
     * @return Nivel de sangrado (0 si no está sangrando)
     */
    public int getPlayerBleedingLevel(Player player) {
        return getBleedingSeverity(player);
    }
    
    /**
     * Obtiene la severidad del sangrado de una entidad
     * @param entity Entidad a verificar
     * @return Severidad del sangrado (0 si no está sangrando)
     */
    public int getBleedingSeverity(LivingEntity entity) {
        return bleedingSeverity.getOrDefault(entity.getUniqueId(), 0);
    }
    
    /**
     * Obtiene la duración restante del sangrado de una entidad
     * @param entity Entidad a verificar
     * @return Duración restante en segundos (0 si no está sangrando)
     */
    public int getBleedingDuration(LivingEntity entity) {
        return bleedingDuration.getOrDefault(entity.getUniqueId(), 0);
    }
    
    /**
     * Detiene el sistema de sangrado
     */
    public void shutdown() {
        if (bleedingTask != null) {
            bleedingTask.cancel();
        }
        
        isActive = false;
    }
    
    /**
     * Verifica si el sistema de sangrado está activo
     * @return true si está activo, false en caso contrario
     */
    public boolean isActive() {
        return isActive;
    }
}
