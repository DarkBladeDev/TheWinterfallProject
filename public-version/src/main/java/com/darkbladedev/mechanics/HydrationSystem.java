package com.darkbladedev.mechanics;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.CustomTypes.CustomDamageTypes;
import com.darkbladedev.mechanics.auraskills.skilltrees.SkillTreeManager;
import com.darkbladedev.utils.AuraSkillsUtil;
import com.darkbladedev.mechanics.auraskills.skills.hydration.EfficientDrinkerSkill;
import com.darkbladedev.mechanics.auraskills.skills.hydration.DesertWalkerSkill;
import com.darkbladedev.mechanics.auraskills.skills.hydration.RainCollectorSkill;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.stat.CustomStat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sistema que maneja la hidratación de los jugadores
 * Funciona de manera similar al sistema de hambre vanilla pero con un contador separado
 * que se puede regenerar con botellas de agua
 */
public class HydrationSystem implements Listener {

    private final SavageFrontierMain plugin;
    private BukkitTask hydrationTask;
    private final Map<UUID, Integer> hydrationLevel;
    private boolean isActive;
    
    // Constantes y configuraciones
    private int MAX_HYDRATION;
    private int WATER_BOTTLE_HYDRATION; // Cantidad de hidratación que da una botella de agua
    private int HYDRATION_DAMAGE_THRESHOLD; // Nivel por debajo del cual se empieza a recibir daño
    private int UPDATE_INTERVAL; // Intervalo de actualización en ticks
    
    // Factores de disminución (configurables)
    private double normalDecreaseRate; // Probabilidad base de disminución
    private double activityDecreaseRate; // Probabilidad de disminución durante actividad
    
    // Stat de hidratación personalizado (debe inicializarse igual que en StaminaSystemExpansion)
    private CustomStat hydrationStat;
    @SuppressWarnings("unused")
	private void ensureHydrationStatLoaded() {
        if (hydrationStat == null) {
            try {
                AuraSkillsApi api = AuraSkillsApi.get();
                var registry = api.getGlobalRegistry();
                hydrationStat = (CustomStat) registry.getStat(NamespacedId.of("savage-frontier", "hydration"));
            } catch (Exception ignored) {}
        }
    }
    
    /**
     * Constructor del sistema de hidratación
     * @param plugin Instancia del plugin principal
     */
    public HydrationSystem(SavageFrontierMain plugin) {
        this.plugin = plugin;
        this.hydrationLevel = new HashMap<>();
        this.isActive = false;
        
        // Cargar configuración
        loadConfig();
    }
    
    /**
     * Carga la configuración desde config.yml
     */
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        
        // Cargar valores desde la configuración
        MAX_HYDRATION = config.getInt("hydration.max_level", 20);
        WATER_BOTTLE_HYDRATION = config.getInt("hydration.water_bottle_hydration", 6);
        HYDRATION_DAMAGE_THRESHOLD = config.getInt("hydration.damage_threshold", 6);
        UPDATE_INTERVAL = config.getInt("hydration.update_interval", 60);
        
        // Cargar tasas de disminución
        normalDecreaseRate = config.getDouble("hydration.normal_decrease_rate", 0.1);
        activityDecreaseRate = config.getDouble("hydration.activity_decrease_rate", 0.3);
    }
    
    /**
     * Inicializa el sistema de hidratación
     */
    public void initialize() {
        // Verificar si el sistema está habilitado en la configuración
        if (!plugin.getConfig().getBoolean("hydration.enabled", true)) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + "<yellow> Sistema de hidratación deshabilitado en la configuración"));
            return;
        }
        
        startHydrationSystem();
        isActive = true;
        
        // Registrar eventos
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + "<green> Sistema de hidratación inicializado"));
    }
    
    /**
     * Inicia el sistema de hidratación
     */
    private void startHydrationSystem() {
        hydrationTask = new BukkitRunnable() {
			@Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    
                    // Inicializar hidratación si es necesario
                    if (!hydrationLevel.containsKey(playerId)) {
                        hydrationLevel.put(playerId, MAX_HYDRATION);
                    }
                    
                    // Obtener nivel actual
                    int currentLevel = hydrationLevel.get(playerId);
                    
                    // Verificar si el jugador está protegido como nuevo jugador
                    if (!plugin.isPlayerProtectedFromSystem(player, "hydration")) {
                        // DesertWalkerSkill: Reduce el consumo de hidratación en biomas cálidos
                        int hydrationStatLevel = AuraSkillsUtil.getCustomStatLevel(player, "hydration");
                        boolean hasDesertWalker = SkillTreeManager.getInstance().hasSkill(player, DesertWalkerSkill.class, java.util.Map.of("hydration", hydrationStatLevel));
                        double decreaseModifier = 1.0;
                        if (hasDesertWalker) {
                            org.bukkit.block.Biome biome = player.getLocation().getBlock().getBiome();
                            // Compatibilidad con Minecraft 1.20+ (evita uso de .name() deprecado)
                            String biomeKey = biome.getKey().getKey().toUpperCase();
                            if (biomeKey.contains("DESERT") || biomeKey.contains("SAVANNA") || biomeKey.contains("BADLANDS")) {
                                decreaseModifier = 0.5; // Reduce el consumo a la mitad en biomas cálidos
                            }
                        }
                        // Reducir hidratación basado en actividad
                        if (player.isSprinting() || (player.isFlying() && player.getGameMode().equals(org.bukkit.GameMode.SURVIVAL))) {
                            // Reducir más rápido si está corriendo o saltando
                            if (Math.random() < activityDecreaseRate * decreaseModifier) {
                                decreaseHydration(player, 1);
                            }
                        } else {
                            // Reducción normal
                            if (Math.random() < normalDecreaseRate * decreaseModifier) {
                                decreaseHydration(player, 1);
                            }
                        }
                    }
                    
                    // INTEGRACIÓN AURASKILLS: Habilidades pasivas de hidratación
                    int hydrationLevel = AuraSkillsUtil.getCustomStatLevel(player, "hydration");
                    // RainCollectorSkill: Recupera hidratación bajo la lluvia
                    if (SkillTreeManager.getInstance().hasSkill(player, RainCollectorSkill.class, java.util.Map.of("hydration", hydrationLevel))) {
                        if (player.getWorld().hasStorm() && player.getLocation().getBlock().getY() + 1 == player.getWorld().getHighestBlockYAt(player.getLocation())) {
                            increaseHydration(player, 1); // Recupera 1 punto por tick bajo la lluvia
                        }
                    }
                    
                    // Aplicar efectos si la hidratación es baja
                    if (currentLevel <= HYDRATION_DAMAGE_THRESHOLD) {
                        applyDehydrationEffects(player, currentLevel);
                    }
                }
            }
        }.runTaskTimer(plugin, UPDATE_INTERVAL, UPDATE_INTERVAL); // Ejecutar según el intervalo configurado
    }
    
    /**
     * Aplica los efectos de deshidratación según el nivel
     * @param player Jugador afectado
     * @param level Nivel de hidratación
     */
    private void applyDehydrationEffects(Player player, int level) {
        // Verificar si el jugador tiene permiso para bypass o está protegido como nuevo jugador
        if (player.hasPermission("savage.bypass.dehydration") || plugin.isPlayerProtectedFromSystem(player, "hydration")) {
            return; // No aplicar efectos si tiene el permiso o está protegido
        }
        
        // Efectos según el nivel de hidratación
        if (level <= 0) {
            // Deshidratación severa: daño y efectos graves
            try {
                // Crear DamageSource personalizado para deshidratación
                DamageSource damageSource = CustomDamageTypes.DamageSourceBuilder(null, player, CustomDamageTypes.DEHYDRATION_KEY);
                player.damage(1.0, damageSource); // Medio corazón de daño
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al aplicar deshidratación con DamageType custom (Aplicando daño default): " + e.getMessage()));
                player.damage(1.0); // Daño por defecto si hay un error
            }
            plugin.getCustomDebuffEffects().applyWeakness(player);
            //player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0));
            
            // Mensaje (con probabilidad para no spamear)
            if (Math.random() < 0.3 && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>¡Estás severamente deshidratado! Necesitas agua urgentemente."));
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>¡Estás severamente deshidratado!"));
            }
        } else if (level <= 3) {
            // Deshidratación moderada: efectos moderados
            plugin.getCustomDebuffEffects().applyWeakness(player);
            //player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 0));
            
            // Mensaje (con probabilidad para no spamear)
            if (Math.random() < 0.2 && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Te sientes muy débil por la deshidratación. Necesitas beber agua."));
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>Te sientes muy débil por la deshidratación."));
            }
        } else if (level <= HYDRATION_DAMAGE_THRESHOLD) {
            // Deshidratación leve: efectos leves
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 60, 0));
            
            // Mensaje (con probabilidad para no spamear)
            if (Math.random() < 0.1 && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Tienes sed. Deberías beber agua pronto."));
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<gold>Tienes sed."));
            }
        }
    }
    
    /**
     * Disminuye el nivel de hidratación de un jugador
     * @param player Jugador
     * @param amount Cantidad a disminuir
     */
    public void decreaseHydration(Player player, int amount) {
        if (!isActive || player == null || amount <= 0) {
            return;
        }
        
        // Verificar si el jugador tiene permiso para bypass
        if (player.hasPermission("savage.bypass.dehydration")) {
            return; // No disminuir hidratación si tiene el permiso
        }
        
        UUID playerId = player.getUniqueId();
        int currentLevel = getHydrationLevel(player);
        int newLevel = Math.max(0, currentLevel - amount);
        
        hydrationLevel.put(playerId, newLevel);
        
        // Notificar al jugador si hay un cambio significativo
        if (newLevel < currentLevel && (currentLevel % 20 == 0 || newLevel == 0) && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
            if (newLevel <= 20) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>¡Tienes mucha sed! Necesitas beber agua urgentemente."));
            } else {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Empiezas a sentir sed."));
            }
        }
        
        // Aplicar efectos según el nivel de hidratación
        applyDehydrationEffects(player, newLevel);
        

    }
    
    /**
     * Aumenta el nivel de hidratación de un jugador
     * @param player Jugador
     * @param amount Cantidad a aumentar
     */
    public void increaseHydration(Player player, int amount) {
        if (!isActive || player == null || amount <= 0) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        int currentLevel = getHydrationLevel(player);
        int newLevel = Math.min(currentLevel + amount, MAX_HYDRATION);
        
        hydrationLevel.put(playerId, newLevel);
        
        // Notificar al jugador si hay un cambio significativo
        if (newLevel > currentLevel && (newLevel % 20 == 0 || newLevel == MAX_HYDRATION) && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Te sientes más hidratado."));
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<aqua>Te sientes más hidratado."));
        }
        
    }
    
    /**
     * Obtiene el nivel de hidratación de un jugador
     * @param player Jugador
     * @return Nivel de hidratación (0-20)
     */
    public int getHydrationLevel(Player player) {
        if (player == null) return 0;
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar si es necesario
        if (!hydrationLevel.containsKey(playerId)) {
            hydrationLevel.put(playerId, MAX_HYDRATION);
        }
        
        return hydrationLevel.get(playerId);
    }
    
    /**
     * Obtiene el nivel de hidratación de un jugador como porcentaje
     * @param player Jugador
     * @return Nivel de hidratación (0-100%)
     */
    public int getHydrationPercentage(Player player) {
        int level = getHydrationLevel(player);
        return (level * 100) / MAX_HYDRATION;
    }
    
    /**
     * Genera una barra de progreso visual para la hidratación
     * @param player Jugador
     * @return Barra de progreso como texto
     */
    public Component getHydrationBar(Player player) {
        @SuppressWarnings("unused")
        int level = getHydrationLevel(player);
        int percentage = getHydrationPercentage(player);
        
        MiniMessage serializer = MiniMessage.builder()
            .tags(TagResolver.builder()
              .resolver(StandardTags.color())
                .build())
            .build();
        
        // Determinar color según nivel
        String colorTag;
        if (percentage > 70) {
            colorTag = "<aqua>"; // Bien hidratado
        } else if (percentage > 30) {
            colorTag = "<yellow>"; // Hidratación media
        } else {
            colorTag = "<red>"; // Deshidratado
        }
        
        // Construir barra de progreso
        int bars = (int) Math.round(percentage / 10.0);
        StringBuilder barContent = new StringBuilder();
        
        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                barContent.append("■");
            } else {
                barContent.append("▢");
            }
        }
        
        // Crear el componente con el color y el contenido
        return serializer.deserialize(colorTag + barContent.toString());
    }
    
    /**
     * Maneja el evento de consumo de items
     * @param event Evento de consumo
     */
    @SuppressWarnings({ "removal", "deprecation" })
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        int hydrationStatLevel = AuraSkillsUtil.getCustomStatLevel(player, "hydration");
        // EfficientDrinkerSkill: Aumenta la hidratación recibida al beber agua
        double hydrationMultiplier = 1.0;
        if (SkillTreeManager.getInstance().hasSkill(player, EfficientDrinkerSkill.class, java.util.Map.of("hydration", hydrationStatLevel))) {
            hydrationMultiplier = 1.5;
        }
        // Verificar si es una botella de agua
        if (item.getType() == Material.POTION) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            if (meta != null && meta.getBasePotionData().getType() == PotionType.WATER) {
                // Aumentar hidratación con multiplicador
                int hydrationToAdd = (int) Math.round(WATER_BOTTLE_HYDRATION * hydrationMultiplier);
                increaseHydration(player, hydrationToAdd);
                // Efectos de sonido
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0f, 1.0f);
            }
        }
    }
    
    /**
     * Maneja el evento de daño a entidades
     * @param event Evento de daño
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        // Reducir hidratación en caso de daño por fuego o lava
        if (event.getEntity() instanceof Player && 
                (event.getCause() == DamageCause.FIRE || 
                 event.getCause() == DamageCause.FIRE_TICK || 
                 event.getCause() == DamageCause.LAVA)) {
            Player player = (Player) event.getEntity();
            decreaseHydration(player, 2); // Perder más hidratación por calor
        }
    }
    
    /**
     * Maneja el evento de unión de un jugador
     * @param event Evento de unión
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Inicializar hidratación si es un jugador nuevo
        if (!hydrationLevel.containsKey(playerId)) {
            hydrationLevel.put(playerId, MAX_HYDRATION);
        }
    }
    
    /**
     * Verifica si el sistema está activo
     * @return true si el sistema está activo, false en caso contrario
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Establece la tasa de disminución normal de hidratación
     * @param rate Tasa de disminución (0.0 - 1.0)
     */
    public void setNormalDecreaseRate(double rate) {
        this.normalDecreaseRate = Math.max(0.0, Math.min(1.0, rate));
    }
    
    /**
     * Obtiene la tasa de disminución normal de hidratación
     * @return Tasa de disminución normal
     */
    public double getNormalDecreaseRate() {
        return normalDecreaseRate;
    }
    
    /**
     * Establece la tasa de disminución de hidratación durante actividad física
     * @param rate Tasa de disminución (0.0 - 1.0)
     */
    public void setActivityDecreaseRate(double rate) {
        this.activityDecreaseRate = Math.max(0.0, Math.min(1.0, rate));
    }
    
    /**
     * Obtiene la tasa de disminución de hidratación durante actividad física
     * @return Tasa de disminución durante actividad
     */
    public double getActivityDecreaseRate() {
        return activityDecreaseRate;
    }
    
    /**
     * Detiene el sistema de hidratación
     */
    public void shutdown() {
        if (hydrationTask != null) {
            hydrationTask.cancel();
        }
        isActive = false;
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Sistema de hidratación desactivado"));
    }

    /**
     * Establece el nivel de hidratación de un jugador
     * @param player Jugador
     * @param level Nivel de hidratación a establecer (0-20)
     */
    public void setHydrationLevel(Player player, int level) {
        if (!isActive || player == null) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        int newLevel = Math.max(0, Math.min(MAX_HYDRATION, level));
        
        hydrationLevel.put(playerId, newLevel);
        
        // Aplicar efectos según el nivel de hidratación
        applyDehydrationEffects(player, newLevel);
    }

    public int getMaxHydration() {
        return MAX_HYDRATION;
    }
}