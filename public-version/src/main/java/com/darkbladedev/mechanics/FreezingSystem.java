package com.darkbladedev.mechanics;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.CustomTypes.CustomDamageTypes;
import com.darkbladedev.CustomTypes.CustomEnchantments;

import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Sistema que maneja el efecto de congelación en "El Eternauta"
 * Aplica efectos de ralentización y daño por congelación a los enemigos
 */
public class FreezingSystem implements Listener {

    private final SavageFrontierMain plugin;
    private final Random random;
    private BukkitTask freezingTask;
    private final Map<UUID, Integer> freezingLevel;
    private final Map<UUID, Integer> freezingDuration;
    private boolean isActive;
    
    // Constantes del sistema
    private static final int MAX_FREEZING_LEVEL = 3;
    private static final int DEFAULT_DURATION = 100; // 5 segundos (20 ticks = 1 segundo)
    private static final float FREEZING_DAMAGE = 2.0f; // 1 corazones de daño
    
    /**
     * Constructor del sistema de congelación
     * @param plugin Instancia del plugin principal
     */
    public FreezingSystem(SavageFrontierMain plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.freezingLevel = new HashMap<>();
        this.freezingDuration = new HashMap<>();
        this.isActive = false;
    }
    
    /**
     * Inicializa el sistema de congelación
     */
    public void initialize() {
        if (!isActive) {
            // Registrar eventos
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            
            // Iniciar tarea de congelación
            startFreezingTask();
            
            isActive = true;
            plugin.getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Sistema de congelación activado"));
        }
    }
    
    /**
     * Inicia la tarea periódica que procesa los efectos de congelación
     */
    private void startFreezingTask() {
        if (freezingTask != null) {
            freezingTask.cancel();
        }
        
        freezingTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Procesar cada entidad con efecto de congelación
                for (UUID entityId : new HashMap<>(freezingDuration).keySet()) {
                    // Obtener duración actual
                    int duration = freezingDuration.get(entityId);
                    
                    // Reducir duración
                    duration -= 20; // Reducir 1 segundo (20 ticks)
                    
                    // Obtener la entidad
                    Entity entity = Bukkit.getEntity(entityId);
                    
                    if (entity == null || !entity.isValid() || duration <= 0) {
                        // Eliminar efecto si la entidad no existe o la duración terminó
                        freezingDuration.remove(entityId);
                        freezingLevel.remove(entityId);
                        
                        if (entity instanceof LivingEntity livingEntity && entity.isValid()) {
                            // Notificar al jugador que el efecto terminó
                            if (livingEntity instanceof Player player) {
                                if (plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                                    player.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>El efecto de congelación ha terminado.</aqua>"));
                                    
                                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<aqua>El efecto de congelación ha terminado.</aqua>"));
                                }
                            }
                        }
                    } else {
                        // Actualizar duración
                        freezingDuration.put(entityId, duration);
                        
                        // Aplicar efectos si es una entidad viva
                        if (entity instanceof LivingEntity livingEntity) {
                            applyFreezingEffects(livingEntity);
                            showFreezingEffects(livingEntity);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Ejecutar cada segundo
    }
    
    /**
     * Maneja el evento de daño entre entidades para aplicar el efecto de congelación
     * @param event Evento de daño entre entidades
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Verificar si el atacante es un jugador
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        
        // Verificar si la entidad dañada es una entidad viva
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        
        // Verificar si el jugador tiene un arma con el encantamiento de congelación
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon == null || !weapon.hasItemMeta()) {
            return;
        }
        
        ItemMeta meta = weapon.getItemMeta();
        if (meta.hasEnchant(CustomEnchantments.getCongelationEnchantment())) {
            // Calcular probabilidad basada en el nivel del encantamiento
            int enchantLevel = meta.getEnchantLevel(CustomEnchantments.getCongelationEnchantment());
            double chance = 0.15 * enchantLevel; // 15% por nivel
            
            if (random.nextDouble() <= chance) {
                // Aplicar efecto de congelación
                applyFreezing(victim, enchantLevel);
                
                // Notificar al atacante
                if (plugin.getUserPreferencesManager().hasStatusMessages(attacker)) {
                    attacker.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>¡Has congelado a tu objetivo!</aqua>"));
                    attacker.sendActionBar(MiniMessage.miniMessage().deserialize("<aqua>¡Has congelado a tu objetivo!</aqua>"));
                }
                
                // Notificar a la víctima si es un jugador
                if (victim instanceof Player) {
                    Player player = (Player) victim;
                    if (plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>¡Has sido congelado!</aqua>"));
                        player.sendActionBar(MiniMessage.miniMessage().deserialize("<aqua>¡Has sido congelado!</aqua>"));
                    }
                }
                
                // Efectos visuales y sonoros
                victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);
            }
        }
    }
    
    /**
     * Aplica el efecto de congelación a una entidad
     * @param entity Entidad a congelar
     * @param level Nivel de congelación (1-3)
     */
    public void applyFreezing(LivingEntity entity, int level) {
        if (entity == null || !entity.isValid()) {
            return;
        }
        
        // Verificar si es un jugador y está protegido como nuevo jugador
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (plugin.isPlayerProtectedFromSystem(player, "freezing")) {
                return; // No aplicar congelación si el jugador está protegido
            }
        }
        
        UUID entityId = entity.getUniqueId();
        
        // Limitar el nivel máximo
        int freezingLevel = Math.min(level, MAX_FREEZING_LEVEL);
        
        // Establecer nivel y duración
        this.freezingLevel.put(entityId, freezingLevel);
        this.freezingDuration.put(entityId, DEFAULT_DURATION * freezingLevel); // Duración proporcional al nivel
    }
    
    /**
     * Aplica los efectos de congelación a una entidad
     * @param entity Entidad afectada
     */
    private void applyFreezingEffects(LivingEntity entity) {
        if (entity == null || !entity.isValid()) {
            return;
        }
        
        // Verificar si es un jugador y está protegido como nuevo jugador
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (plugin.isPlayerProtectedFromSystem(player, "freezing")) {
                return; // No aplicar efectos si el jugador está protegido
            }
        }
        
        UUID entityId = entity.getUniqueId();
        
        // Obtener nivel de congelación
        int level = freezingLevel.getOrDefault(entityId, 1);
        
        // Aplicar efectos según el nivel
        switch (level) {
            case 1: // Nivel 1: Lentitud leve
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
                break;
                
            case 2: // Nivel 2: Lentitud moderada y debilidad
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
                
                // Daño leve por congelación cada 3 segundos
                if (random.nextInt(3) == 0) {
                    entity.damage(FREEZING_DAMAGE, CustomDamageTypes.DamageSourceBuilder(null, entity, CustomDamageTypes.FREEZING_KEY));
                }
                break;
                
            case 3: // Nivel 3: Lentitud severa, debilidad y daño por congelación
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
                entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 1));
                // Daño por congelación
                entity.damage(FREEZING_DAMAGE, CustomDamageTypes.DamageSourceBuilder(null, entity, CustomDamageTypes.FREEZING_KEY));
                break;
        }
    }
    
    /**
     * Muestra efectos visuales de congelación alrededor de la entidad
     * @param entity Entidad afectada
     */
    private void showFreezingEffects(LivingEntity entity) {
        if (entity == null || !entity.isValid()) {
            return;
        }
        
        Location loc = entity.getLocation();
        
        // Partículas de hielo
        DustOptions dustOptions = new DustOptions(Color.fromRGB(173, 216, 230), 1.0f); // Color azul claro
        
        for (int i = 0; i < 10; i++) {
            double x = loc.getX() + (random.nextDouble() * 2 - 1) * 0.5;
            double y = loc.getY() + random.nextDouble() * 1.5;
            double z = loc.getZ() + (random.nextDouble() * 2 - 1) * 0.5;
            
            entity.getWorld().spawnParticle(Particle.DUST, x, y, z, 1, 0, 0, 0, 0, dustOptions);
        }
        
        // Partículas adicionales para niveles más altos
        int level = freezingLevel.getOrDefault(entity.getUniqueId(), 1);
        if (level >= 2) {
            entity.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 5, 0.5, 0.5, 0.5, 0.01);
        }
    }
    
    /**
     * Detiene el sistema de congelación
     */
    public void shutdown() {
        if (freezingTask != null) {
            freezingTask.cancel();
        }
        
        isActive = false;
        plugin.getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Sistema de congelación desactivado</red>"));
    }
    
    /**
     * Verifica si el sistema de congelación está activo
     * @return true si está activo, false en caso contrario
     */
    public boolean isActive() {
        return isActive;
    }

    public boolean isFrozen(Player player) {
        return freezingLevel.containsKey(player.getUniqueId());
    }
}