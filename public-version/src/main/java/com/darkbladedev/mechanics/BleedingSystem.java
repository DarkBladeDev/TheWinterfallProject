package com.darkbladedev.mechanics;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.CustomTypes.CustomDamageTypes;
import com.darkbladedev.mechanics.auraskills.skilltrees.SkillTreeManager;
import com.darkbladedev.utils.AuraSkillsUtil;
import com.darkbladedev.mechanics.auraskills.skills.recovery.QuickHealerSkill;
import com.darkbladedev.mechanics.auraskills.skills.vitality.ThickSkinSkill;
import com.darkbladedev.mechanics.auraskills.skills.recovery.MedicSkill;

import org.bukkit.Bukkit;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
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
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sistema que maneja el sangrado del plugin
 */
public class BleedingSystem implements Listener {

    private final SavageFrontierMain plugin;
    private final Random random;
    private BukkitTask bleedingTask;
    private final ConcurrentHashMap<UUID, Integer> bleedingSeverity;
    private final ConcurrentHashMap<UUID, Integer> bleedingDuration;
    private boolean isActive;
    
    /**
     * Constructor del sistema de sangrado
     * @param plugin Instancia del plugin principal
     */
    public BleedingSystem(SavageFrontierMain plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.bleedingSeverity = new ConcurrentHashMap<>();
        this.bleedingDuration = new ConcurrentHashMap<>();
        this.isActive = false;
    }
    
    /**
     * Inicializa el sistema de sangrado
     */
    public void initialize() {
        startBleedingSystem();
        isActive = true;
        
        ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Sistema de sangrado inicializado"));
    }
    
    /**
     * Inicia el sistema de sangrado
     */
    private void startBleedingSystem() {
        bleedingTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Procesar a todos los jugadores con sangrado activo
                // Usar una copia de las claves para evitar ConcurrentModificationException
                for (UUID entityId : new HashMap<>(bleedingSeverity).keySet()) {
                    // Verificar si la entidad aún está en el mapa (podría haber sido eliminada por otra iteración)
                    if (!bleedingSeverity.containsKey(entityId)) {
                        continue;
                    }
                    
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
                            if (plugin.getUserPreferencesManager().hasStatusMessages(player)) {

                                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Tu sangrado se ha detenido."));
                                player.sendActionBar(MiniMessage.miniMessage().deserialize("<green>Tu sangrado se ha detenido."));
                            }
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
        double bleedingDamage = severity * 0.5;
        if (entity instanceof Player) {
            Player player = (Player) entity;
            // --- INTEGRACIÓN AURASKILLS: Reducción de daño por sangrado con ThickSkinSkill ---
            int vitalityLevel = AuraSkillsUtil.getCustomStatLevel(player, "vitality");
            int recoveryLevel = AuraSkillsUtil.getCustomStatLevel(player, "recovery");
            // ThickSkinSkill: Reduce el daño por sangrado un 40%
            if (SkillTreeManager.getInstance().hasSkill(player, ThickSkinSkill.class, java.util.Map.of("vitality", vitalityLevel))) {
                bleedingDamage *= 0.6;
            }
            // MedicSkill: Reduce el daño por sangrado un 20% adicional si está presente
            if (SkillTreeManager.getInstance().hasSkill(player, MedicSkill.class, java.util.Map.of("recovery", recoveryLevel))) {
                bleedingDamage *= 0.8;
            }
        }
        // Aplicar daño
        if (random.nextInt(5) < severity) {
            try {
                DamageSource damageSource = CustomDamageTypes.DamageSourceBuilder(null, entity, CustomDamageTypes.BLEEDING_KEY);
                entity.damage(bleedingDamage, damageSource);
            } catch (Exception e) {
                ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al aplicar sangrado con DamageType custom (Aplicando daño default): " + e.getMessage()));
                entity.damage(bleedingDamage);
            }
        }
        
        // Efectos adicionales para jugadores
        if (entity instanceof Player) {
            Player player = (Player) entity;
            
            // Verificar si el jugador está protegido como nuevo jugador
            if (plugin.isPlayerProtectedFromSystem(player, "bleeding")) {
                return; // No aplicar efectos si el jugador está protegido
            }
            
            // Efectos de poción según severidad
            switch (severity) {
                case 1: // Leve
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
                    break;
                case 2: // Moderado
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0));
                    plugin.getCustomDebuffEffects().applyWeakness(player);
                    //player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
                    break;
                case 3: // Grave
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
                    plugin.getCustomDebuffEffects().applyWeakness(player);
                    //player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0));
                    break;
            }
            
            // Mensajes según severidad (con probabilidad para no spamear)
            if (random.nextInt(20) < severity) {
                switch (severity) {
                    case 1:
                        ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Tienes una herida leve que está sangrando."));
                        player.sendActionBar(MiniMessage.miniMessage().deserialize("<yellow>Tienes una herida leve que está sangrando."));
                        break;
                    case 2:
                        ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<gold>Estás perdiendo sangre de forma moderada. Necesitas tratamiento."));
                        player.sendActionBar(MiniMessage.miniMessage().deserialize("<gold>Estás perdiendo sangre de forma moderada."));
                        break;
                    case 3:
                        ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<red>¡Estás sangrando gravemente! Necesitas atención médica urgente."));
                        player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>Estás sangrando gravemente."));
                        break;
                }
            }
        }
        
        // QuickHealerSkill: Reduce la duración del sangrado en un 30%
        if (entity instanceof Player) {
            Player player = (Player) entity;
            int recoveryLevel = AuraSkillsUtil.getCustomStatLevel(player, "recovery");
            if (SkillTreeManager.getInstance().hasSkill(player, QuickHealerSkill.class, java.util.Map.of("recovery", recoveryLevel))) {
                UUID entityId = player.getUniqueId();
                if (bleedingDuration.containsKey(entityId)) {
                    int duration = bleedingDuration.get(entityId);
                    int reduced = (int) Math.ceil(duration * 0.7);
                    bleedingDuration.put(entityId, reduced);
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
        if (!isActive || entity == null) {
            return;
        }
        
        // Validar severidad
        severity = Math.max(1, Math.min(3, severity));
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
                    severityText = "<yellow>" + "leve";
                    break;
                case 2:
                    severityText = "<gold>" + "moderado";
                    break;
                case 3:
                    severityText = "<red>" + "grave";
                    break;
            }
            
            ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<red>¡Estás sangrando! " + "<white>(Nivel: " + severityText + ", Duración: " + duration + " s)"));
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
        Player player = (Player) event.getEntity();
        DamageSource source = event.getDamageSource();

        if (source == CustomDamageTypes.DamageSourceBuilder(player, player, CustomDamageTypes.HYPERTHERMIA_KEY) ||
        source == CustomDamageTypes.DamageSourceBuilder(player, player, CustomDamageTypes.HYPOTHERMIA_KEY) ||
        source == CustomDamageTypes.DamageSourceBuilder(player, player, CustomDamageTypes.FREEZING_KEY) ||
        source == CustomDamageTypes.DamageSourceBuilder(player, player, CustomDamageTypes.DEHYDRATION_KEY)) {
            return; // No aplicar daño
        }
        
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
     * @param entity Entidad a la que detener el sangrado
     */
    public void stopBleeding(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        
        UUID entityId = entity.getUniqueId();
        bleedingSeverity.remove(entityId);
        bleedingDuration.remove(entityId);
        
        // Notificar al jugador
        if (entity instanceof Player) {
            Player player = (Player) entity;
            ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<green>Tu sangrado se ha detenido."));
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<green>Tu sangrado se ha detenido."));
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
