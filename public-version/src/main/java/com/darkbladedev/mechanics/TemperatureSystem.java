package com.darkbladedev.mechanics;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.CustomTypes.CustomEnchantments;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sistema que gestiona la temperatura corporal de los jugadores
 * Los jugadores pueden morir de hipotermia o hipertermia si no llevan equipamiento adecuado
 * o encantamientos de protección contra frío o calor
 */
public class TemperatureSystem implements Listener {

    // Constantes del sistema
    public static final int MAX_TEMPERATURE = 100; // Temperatura máxima (normal)
    public static final int MIN_TEMPERATURE = 0;   // Temperatura mínima (hipotermia severa)
    public static final int DEFAULT_HYPOTHERMIA_THRESHOLD = 30; // Umbral para efectos de hipotermia
    public static final int DEFAULT_SEVERE_HYPOTHERMIA_THRESHOLD = 15; // Umbral para hipotermia severa
    public static final int DEFAULT_HYPERTHERMIA_THRESHOLD = 70; // Umbral para efectos de hipertermia
    public static final int DEFAULT_SEVERE_HYPERTHERMIA_THRESHOLD = 85; // Umbral para hipertermia severa
    
    // Umbrales actuales (configurables)
    private int HYPOTHERMIA_THRESHOLD = DEFAULT_HYPOTHERMIA_THRESHOLD;
    private int SEVERE_HYPOTHERMIA_THRESHOLD = DEFAULT_SEVERE_HYPOTHERMIA_THRESHOLD;
    private int HYPERTHERMIA_THRESHOLD = DEFAULT_HYPERTHERMIA_THRESHOLD;
    private int SEVERE_HYPERTHERMIA_THRESHOLD = DEFAULT_SEVERE_HYPERTHERMIA_THRESHOLD;
    
    // Configuración
    private boolean isEnabled;
    private int updateInterval; // Intervalo de actualización en ticks
    private int temperatureDecreaseAmount; // Cantidad que disminuye la temperatura
    private int temperatureIncreaseAmount; // Cantidad que aumenta la temperatura
    private double temperatureDecreaseRate; // Tasa de disminución (0.0 - 1.0)
    private double temperatureIncreaseRate; // Tasa de aumento (0.0 - 1.0)
    private int coldProtectionEffectiveness; // Efectividad del encantamiento de protección contra frío
    private int heatProtectionEffectiveness; // Efectividad del encantamiento de protección contra calor
    
    // Umbrales configurables
    @SuppressWarnings("unused")
    private int hypothermiaThreshold;
    @SuppressWarnings("unused")
    private int severeHypothermiaThreshold;
    @SuppressWarnings("unused")
    private int hyperthermiaThreshold;
    @SuppressWarnings("unused")
    private int severeHyperthermiaThreshold;
    
    // Mapa de temperaturas por bioma
    private final Map<String, Integer> biomeTemperatures;
    
    // Referencias
    private final SavageFrontierMain plugin;
    
    // Estado
    private boolean isActive;
    private BukkitTask temperatureTask;
    private final Map<UUID, Integer> temperatureLevel;
    
    /**
     * Constructor del sistema de temperatura
     * @param plugin Instancia del plugin principal
     */
    public TemperatureSystem(SavageFrontierMain plugin) {
        this.plugin = plugin;
        this.temperatureLevel = new HashMap<>();
        this.biomeTemperatures = new HashMap<>();
        this.isActive = false;
        
        // Cargar configuración
        loadConfig();
    }
    
    /**
     * Carga la configuración del sistema desde config.yml
     */
    @SuppressWarnings("removal")
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        
        // Valores por defecto si no existen en la configuración
        if (!config.contains("temperature.enabled")) {
            config.set("temperature.enabled", true);
            config.set("temperature.updateInterval", 100); // 5 segundos (20 ticks = 1 segundo)
            config.set("temperature.decreaseAmount", 1);
            config.set("temperature.increaseAmount", 2);
            config.set("temperature.decreaseRate", 0.8);
            config.set("temperature.increaseRate", 0.5);
            
            // Configuración de encantamientos
            config.set("temperature.enchantments.cold_protection_effectiveness", 10);
            config.set("temperature.enchantments.heat_protection_effectiveness", 10);
            
            // Umbrales de temperatura
            config.set("temperature.thresholds.hypothermia", DEFAULT_HYPOTHERMIA_THRESHOLD);
            config.set("temperature.thresholds.severe_hypothermia", DEFAULT_SEVERE_HYPOTHERMIA_THRESHOLD);
            config.set("temperature.thresholds.hyperthermia", DEFAULT_HYPERTHERMIA_THRESHOLD);
            config.set("temperature.thresholds.severe_hyperthermia", DEFAULT_SEVERE_HYPERTHERMIA_THRESHOLD);
            
            // Guardar configuración
            plugin.saveConfig();
        }
        
        // Cargar valores básicos
        isEnabled = config.getBoolean("temperature.enabled", true);
        updateInterval = config.getInt("temperature.updateInterval", 100);
        temperatureDecreaseAmount = config.getInt("temperature.decreaseAmount", 1);
        temperatureIncreaseAmount = config.getInt("temperature.increaseAmount", 2);
        temperatureDecreaseRate = config.getDouble("temperature.decreaseRate", 0.8);
        temperatureIncreaseRate = config.getDouble("temperature.increaseRate", 0.5);
        
        // Cargar configuración de encantamientos
        coldProtectionEffectiveness = config.getInt("temperature.enchantments.cold_protection_effectiveness", 10);
        heatProtectionEffectiveness = config.getInt("temperature.enchantments.heat_protection_effectiveness", 10);
        
        // Cargar umbrales de temperatura
        HYPOTHERMIA_THRESHOLD = config.getInt("temperature.thresholds.hypothermia", DEFAULT_HYPOTHERMIA_THRESHOLD);
        SEVERE_HYPOTHERMIA_THRESHOLD = config.getInt("temperature.thresholds.severe_hypothermia", DEFAULT_SEVERE_HYPOTHERMIA_THRESHOLD);
        HYPERTHERMIA_THRESHOLD = config.getInt("temperature.thresholds.hyperthermia", DEFAULT_HYPERTHERMIA_THRESHOLD);
        SEVERE_HYPERTHERMIA_THRESHOLD = config.getInt("temperature.thresholds.severe_hyperthermia", DEFAULT_SEVERE_HYPERTHERMIA_THRESHOLD);
        
        // Cargar temperaturas por bioma
        ConfigurationSection biomesSection = config.getConfigurationSection("temperature.biomes");
        if (biomesSection == null) {
            // Crear sección de biomas si no existe
            biomesSection = config.createSection("temperature.biomes");
            
            // Configurar temperaturas por defecto para algunos biomas comunes
            // Biomas fríos
            biomesSection.set("SNOWY_PLAINS", -5);
            biomesSection.set("ICE_SPIKES", -15);
            biomesSection.set("FROZEN_OCEAN", -10);
            biomesSection.set("FROZEN_RIVER", -8);
            biomesSection.set("SNOWY_TAIGA", -3);
            
            // Biomas templados
            biomesSection.set("PLAINS", 20);
            biomesSection.set("FOREST", 18);
            biomesSection.set("BIRCH_FOREST", 17);
            biomesSection.set("DARK_FOREST", 16);
            biomesSection.set("TAIGA", 10);
            
            // Biomas cálidos
            biomesSection.set("DESERT", 40);
            biomesSection.set("SAVANNA", 35);
            biomesSection.set("BADLANDS", 38);
            biomesSection.set("JUNGLE", 32);
            
            // Biomas extremos
            biomesSection.set("NETHER_WASTES", 70);
            biomesSection.set("SOUL_SAND_VALLEY", 60);
            biomesSection.set("CRIMSON_FOREST", 75);
            biomesSection.set("WARPED_FOREST", 65);
            biomesSection.set("BASALT_DELTAS", 80);
            
            // Guardar configuración
            plugin.saveConfig();
        }
        
        // Cargar temperaturas de biomas desde la configuración
        if (biomesSection != null) {
            for (String biomeName : biomesSection.getKeys(false)) {
                try {
                    int temperature = biomesSection.getInt(biomeName);
                    biomeTemperatures.put(biomeName, temperature);
                } catch (Exception e) {
                    plugin.getLogger().warning("Error al cargar temperatura para bioma: " + biomeName);
                }
            }
        }
        
        // Registrar biomas que no estén en la configuración
        for (Biome biome : Biome.values()) {
            String biomeName = biome.name();
            if (!biomeTemperatures.containsKey(biomeName)) {
                // Asignar temperatura por defecto según el tipo de bioma
                int defaultTemp = getDefaultBiomeTemperature(biome);
                biomeTemperatures.put(biomeName, defaultTemp);
                config.set("temperature.biomes." + biomeName, defaultTemp);
            }
        }
        
        // Guardar cambios en la configuración
        plugin.saveConfig();
    }
    

    /**
     * Inicializa el sistema de temperatura
     */
    public void initialize() {
        if (isEnabled) {
            // Registrar eventos
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            
            // Iniciar sistema
            startTemperatureSystem();
            
            ((Audience) (Bukkit.getConsoleSender())).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Sistema de temperatura activado"));
        } else {
            ((Audience) (Bukkit.getConsoleSender())).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Sistema de temperatura desactivado en la configuración"));
        }
    }
    
    /**
     * Inicia el sistema de temperatura con una tarea periódica
     */
    private void startTemperatureSystem() {
        if (temperatureTask != null) {
            temperatureTask.cancel();
        }
        
        temperatureTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerTemperature(player);
                }
            }
        }.runTaskTimer(plugin, 0L, updateInterval);
        
        isActive = true;
    }
    
    /**
     * Normaliza la temperatura del jugador hacia un valor neutro (50%)
     * @param player Jugador a normalizar
     */
    @SuppressWarnings("unused")
    private void normalizePlayerTemperature(Player player) {
        double currentTemp = getPlayerTemperature(player);
        
        if (currentTemp < 50) {
            // Si está frío, aumentar temperatura
            increasePlayerTemperature(player);
        } else if (currentTemp > 50) {
            // Si está caliente, disminuir temperatura
            decreasePlayerTemperature(player);
        }
    }
    
    /**
     * Actualiza la temperatura de un jugador basado en su entorno y equipamiento
     * @param player Jugador a actualizar
     */
    private void updatePlayerTemperature(Player player) {
        if (player == null || !player.isOnline() || 
            player.getGameMode() == GameMode.CREATIVE || 
            player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar si es necesario
        if (!temperatureLevel.containsKey(playerId)) {
            temperatureLevel.put(playerId, MAX_TEMPERATURE / 2); // Iniciar en temperatura neutral (50%)
        }
        
        // Verificar el entorno del jugador
        boolean isInColdEnvironment = isInColdEnvironment(player);
        boolean isInHotEnvironment = isInHotEnvironment(player);
        
        // Actualizar temperatura según el entorno y protecciones
        if (isInColdEnvironment) {
            // Verificar si el jugador tiene protección contra el frío
            if (!hasProtectionAgainstCold(player)) {
                // Disminuir temperatura si está en ambiente frío sin protección
                decreaseTemperature(player, temperatureDecreaseAmount);
            }
        } else if (isInHotEnvironment) {
            // Verificar si el jugador tiene protección contra el calor
            if (!hasProtectionAgainstHeat(player)) {
                // Aumentar temperatura si está en ambiente caluroso sin protección
                increaseTemperature(player, temperatureIncreaseAmount * 2); // Más rápido en ambientes calurosos
            }
        } else {
            // En biomas templados, normalizar la temperatura
            normalizeTemperature(player);
        }
        
        // Aplicar efectos según la temperatura
        if (getPlayerTemperature(player) <= HYPOTHERMIA_THRESHOLD) {
            // Aplicar efectos de hipotermia si la temperatura es baja
            applyHypothermiaEffects(player);
        } else if (getPlayerTemperature(player) >= HYPERTHERMIA_THRESHOLD) {
            // Aplicar efectos de hipertermia si la temperatura es alta
            applyHyperthermiaEffects(player);
        }
    }
    
    /**
     * Normaliza la temperatura del jugador hacia un valor neutro (50%)
     * @param player Jugador a normalizar
     */
    private void normalizeTemperature(Player player) {
        UUID playerId = player.getUniqueId();
        double currentTemp = temperatureLevel.getOrDefault(playerId, MAX_TEMPERATURE / 2);
        
        if (currentTemp < MAX_TEMPERATURE / 2) {
            // Si está frío, aumentar temperatura
            increaseTemperature(player, temperatureIncreaseAmount / 2);
        } else if (currentTemp > MAX_TEMPERATURE / 2) {
            // Si está caliente, disminuir temperatura
            decreaseTemperature(player, temperatureDecreaseAmount / 2);
        }
    }
    
    /**
     * Verifica si el jugador tiene protección contra el frío
     * @param player Jugador a verificar
     * @return true si tiene protección, false en caso contrario
     */
    private boolean hasProtectionAgainstCold(Player player) {
        
        // Verificar si tiene encantamiento de protección contra el frío
        int protectionLevel = getEnchantmentLevel(player, CustomEnchantments.COLD_PROTECTION_KEY);
        if (protectionLevel > 0) {
            // Calcular probabilidad de protección basada en el nivel del encantamiento y la efectividad configurada
            double protectionChance = protectionLevel * coldProtectionEffectiveness;
            return Math.random() * 100 <= protectionChance;
        }
        
        return false;
    }
    
    /**
     * Verifica si el jugador tiene protección contra el calor
     * @param player Jugador a verificar
     * @return true si tiene protección, false en caso contrario
     */
    private boolean hasProtectionAgainstHeat(Player player) {
        // Verificar si tiene encantamiento de protección contra el calor
        int protectionLevel = getEnchantmentLevel(player, CustomEnchantments.HEAT_PROTECTION_KEY);
        if (protectionLevel > 0) {
            // Calcular probabilidad de protección basada en el nivel del encantamiento y la efectividad configurada
            double protectionChance = protectionLevel * heatProtectionEffectiveness;
            return Math.random() * 100 <= protectionChance;
        }
        
        return false;
    }
    
    /**
     * Obtiene el nivel total de un encantamiento específico en todo el equipamiento del jugador
     * @param player Jugador a verificar
     * @param enchantmentKey Clave del encantamiento a buscar
     * @return Nivel total del encantamiento
     */
    @SuppressWarnings("deprecation")
    private int getEnchantmentLevel(Player player, Key enchantmentKey) {
        int totalLevel = 0;
        
        // Verificar armadura
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasEnchant(Enchantment.getByKey(NamespacedKey.fromString(enchantmentKey.asString())))) {
                    totalLevel += meta.getEnchantLevel(Enchantment.getByKey(NamespacedKey.fromString(enchantmentKey.asString())));
                }
            }
        }
        
        return totalLevel;
    }
    
    /**
     * Verifica si el jugador está en un ambiente frío
     * @param player Jugador a verificar
     * @return true si está en ambiente frío, false en caso contrario
     */
    /**
     * Verifica si el jugador está en un ambiente frío basado en la temperatura del bioma
     * @param player Jugador a verificar
     * @return true si está en ambiente frío, false en caso contrario
     */
    @SuppressWarnings("removal")
    private boolean isInColdEnvironment(Player player) {
        Biome biome = player.getLocation().getBlock().getBiome();
        String biomeName = biome.name();
        
        // Obtener temperatura del bioma desde la configuración
        int biomeTemperature = getBiomeTemperature(biomeName);
        
        // Considerar frío si la temperatura del bioma es menor a 5°C
        return biomeTemperature < 5;
    }
    
    /**
     * Verifica si el jugador está en un ambiente caluroso basado en la temperatura del bioma
     * @param player Jugador a verificar
     * @return true si está en ambiente caluroso, false en caso contrario
     */
    @SuppressWarnings("removal")
    private boolean isInHotEnvironment(Player player) {
        Biome biome = player.getLocation().getBlock().getBiome();
        String biomeName = biome.name();
        
        // Obtener temperatura del bioma desde la configuración
        int biomeTemperature = getBiomeTemperature(biomeName);
        
        // Considerar caluroso si la temperatura del bioma es mayor a 30°C
        return biomeTemperature > 30;
    }
    
    /**
     * Obtiene la temperatura configurada para un bioma específico
     * @param biomeName Nombre del bioma
     * @return Temperatura en grados Celsius
     */
    private int getBiomeTemperature(String biomeName) {
        // Buscar en el mapa de temperaturas
        if (biomeTemperatures.containsKey(biomeName)) {
            return biomeTemperatures.get(biomeName);
        }
        
        // Si no está en el mapa, asignar temperatura por defecto
        try {
            @SuppressWarnings("removal")
            Biome biome = Biome.valueOf(biomeName);
            int defaultTemp = getDefaultBiomeTemperature(biome);
            biomeTemperatures.put(biomeName, defaultTemp);
            return defaultTemp;
        } catch (IllegalArgumentException e) {
            // Si el bioma no existe, devolver temperatura templada
            return 20;
        }
    }
    
    /**
     * Obtiene la temperatura por defecto para un bioma basado en su tipo
     * @param biome Bioma a evaluar
     * @return Temperatura en grados Celsius
     */
    @SuppressWarnings("removal")
    private int getDefaultBiomeTemperature(Biome biome) {
        String biomeName = biome.name().toLowerCase();
        
        // Biomas fríos
        if (biomeName.contains("frozen") || 
            biomeName.contains("ice") || 
            biomeName.contains("snow") || 
            biomeName.contains("cold")) {
            return 0; // Muy frío
        }
        
        // Biomas de taiga
        if (biomeName.contains("taiga")) {
            return 5; // Frío
        }
        
        // Biomas de desierto
        if (biomeName.contains("desert") || 
            biomeName.contains("badlands") || 
            biomeName.contains("savanna") || 
            biomeName.contains("mesa")) {
            return 40; // Muy caluroso
        }
        
        // Biomas de jungla
        if (biomeName.contains("jungle")) {
            return 32; // Caluroso y húmedo
        }
        
        // Biomas del Nether
        if (biomeName.contains("nether") || 
            biomeName.contains("basalt") || 
            biomeName.contains("crimson") || 
            biomeName.contains("warped") || 
            biomeName.contains("soul")) {
            return 70; // Extremadamente caluroso
        }
        
        // Biomas del End
        if (biomeName.contains("end")) {
            return -10; // Extremadamente frío
        }
        
        // Biomas acuáticos
        if (biomeName.contains("ocean") || 
            biomeName.contains("river") || 
            biomeName.contains("beach")) {
            return 15; // Fresco
        }
        
        // Biomas de montaña
        if (biomeName.contains("mountain") || 
            biomeName.contains("hill") || 
            biomeName.contains("peak")) {
            return 10; // Fresco
        }
        
        // Biomas templados por defecto
        return 20; // Temperatura moderada
    }
    
    /**
     * Verifica si el jugador tiene equipamiento de protección contra el frío
     * @param player Jugador a verificar
     * @return true si tiene protección, false en caso contrario
     */
    @SuppressWarnings("unused")
    private boolean checkPlayerProtection(Player player) {
        
        // Verificar si el jugador tiene el traje aislante completo
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        if (helmet != null && chestplate != null && leggings != null && boots != null) {
            return true;
        }
        return false;
    }
    
    /**
     * Aplica efectos de hipotermia basados en el nivel de temperatura
     * @param player Jugador al que aplicar los efectos
     */
    private void applyHypothermiaEffects(Player player) {
        int temperature = getTemperatureLevel(player);
        
        // Limpiar efectos anteriores si la temperatura es normal
        if (temperature > HYPOTHERMIA_THRESHOLD) {
            return;
        }
        
        // Aplicar efectos según el nivel de hipotermia
        if (temperature <= SEVERE_HYPOTHERMIA_THRESHOLD) {
            // Hipotermia severa - daño y efectos graves
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, updateInterval * 20 + 20, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, updateInterval * 20 + 20, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, updateInterval * 20 + 20, 0));
            
            // Daño por hipotermia severa
            player.damage(1.0);
            ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>¡Estás sufriendo hipotermia severa! Necesitas protección contra el frío urgentemente."));
        } else if (temperature <= HYPOTHERMIA_THRESHOLD) {
            // Hipotermia moderada - efectos de movimiento y debilidad
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, updateInterval * 20 + 20, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, updateInterval * 20 + 20, 1));
            
            // Mensaje de advertencia
            if (temperature % 5 == 0) { // Mostrar mensaje cada 5 puntos de temperatura
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<red>Estás comenzando a sufrir hipotermia. Necesitas protección contra el frío."));
            }
        }
    }
    
    /**
     * Aplica efectos de hipertermia basados en el nivel de temperatura
     * @param player Jugador al que aplicar los efectos
     */
    private void applyHyperthermiaEffects(Player player) {
        int temperature = getTemperatureLevel(player);
        
        // Limpiar efectos anteriores si la temperatura es normal
        if (temperature < HYPERTHERMIA_THRESHOLD) {
            return;
        }
        
        // Aplicar efectos según el nivel de hipertermia
        if (temperature >= SEVERE_HYPERTHERMIA_THRESHOLD) {
            // Hipertermia severa - daño y efectos graves
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, updateInterval * 20 + 20, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, updateInterval * 20 + 20, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, updateInterval * 20 + 20, 1));
            
            // Daño por hipertermia severa
            player.damage(1.0);
            ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>¡Estás sufriendo hipertermia severa! Necesitas protección contra el calor urgentemente."));
        } else if (temperature >= HYPERTHERMIA_THRESHOLD) {
            // Hipertermia moderada - efectos de hambre y debilidad
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, updateInterval * 20 + 20, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, updateInterval * 20 + 20, 0));
            
            // Mensaje de advertencia
            if (temperature % 5 == 0) { // Mostrar mensaje cada 5 puntos de temperatura
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<red>Estás comenzando a sufrir hipertermia. Necesitas protección contra el calor."));
            }
        }
    }
    
    /**
     * Disminuye el nivel de temperatura de un jugador
     * @param player Jugador
     * @param amount Cantidad a disminuir
     */
    public void decreaseTemperature(Player player, int amount) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar si es necesario
        if (!temperatureLevel.containsKey(playerId)) {
            temperatureLevel.put(playerId, MAX_TEMPERATURE);
        }
        
        // Obtener nivel actual y reducir
        int currentLevel = temperatureLevel.get(playerId);
        int newLevel = Math.max(MIN_TEMPERATURE, currentLevel - (int)(amount * temperatureDecreaseRate));
        
        // Actualizar nivel
        temperatureLevel.put(playerId, newLevel);
    }
    
    /**
     * Obtiene la temperatura actual de un jugador
     * @param player Jugador a consultar
     * @return Temperatura actual (0-100)
     */
    public double getPlayerTemperature(Player player) {
        if (player == null) return MAX_TEMPERATURE / 2;
        return temperatureLevel.getOrDefault(player.getUniqueId(), MAX_TEMPERATURE / 2);
    }
    
    /**
     * Disminuye la temperatura del jugador
     * @param player Jugador a afectar
     */
    private void decreasePlayerTemperature(Player player) {
        decreaseTemperature(player, temperatureDecreaseAmount);
    }
    
    /**
     * Aumenta la temperatura del jugador
     * @param player Jugador a afectar
     */
    private void increasePlayerTemperature(Player player) {
        increaseTemperature(player, temperatureIncreaseAmount);
    }
    
    /**
     * Aumenta la temperatura del jugador con un factor multiplicador
     * @param player Jugador a afectar
     * @param factor Factor multiplicador para el aumento de temperatura
     */
    @SuppressWarnings("unused")
    private void increasePlayerTemperature(Player player, double factor) {
        increaseTemperature(player, (int)(temperatureIncreaseAmount * factor));
    }
    
    /**
     * Aumenta el nivel de temperatura de un jugador
     * @param player Jugador
     * @param amount Cantidad a aumentar
     */
    public void increaseTemperature(Player player, int amount) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar si es necesario
        if (!temperatureLevel.containsKey(playerId)) {
            temperatureLevel.put(playerId, MAX_TEMPERATURE);
        }
        
        // Obtener nivel actual y aumentar
        int currentLevel = temperatureLevel.get(playerId);
        int newLevel = Math.min(MAX_TEMPERATURE, currentLevel + (int)(amount * temperatureIncreaseRate));
        
        // Actualizar nivel
        temperatureLevel.put(playerId, newLevel);
    }
    
    /**
     * Obtiene el nivel de temperatura de un jugador
     * @param player Jugador
     * @return Nivel de temperatura (0-100)
     */
    public int getTemperatureLevel(Player player) {
        if (player == null) return 0;
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar si es necesario
        if (!temperatureLevel.containsKey(playerId)) {
            temperatureLevel.put(playerId, MAX_TEMPERATURE);
        }
        
        return temperatureLevel.get(playerId);
    }
    
    /**
     * Obtiene el nivel de temperatura de un jugador como porcentaje
     * @param player Jugador
     * @return Nivel de temperatura (0-100%)
     */
    public int getTemperaturePercentage(Player player) {
        return getTemperatureLevel(player);
    }
    
    /**
     * Genera una barra de progreso visual para la temperatura
     * @param player Jugador
     * @return Barra de progreso como texto
     */
    public String getTemperatureBar(Player player) {
        int percentage = getTemperaturePercentage(player);
        
        StringBuilder bar = new StringBuilder();
        MiniMessage mm = MiniMessage.miniMessage();
        
        // Determinar color según nivel
        Component barColor;
        if (percentage > 70) {
            barColor = mm.deserialize("<green>"); // Temperatura normal
        } else if (percentage > 30) {
            barColor = mm.deserialize("<yellow>"); // Temperatura baja
        } else {
            barColor = mm.deserialize("<red>"); // Hipotermia
        }
        
        // Construir barra de progreso
        int bars = (int) Math.round(percentage / 10.0);
        bar.append(barColor);
        
        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                bar.append("■");
            } else {
                bar.append("▢");
            }
        }
        
        return bar.toString();
    }
    
    /**
     * Maneja el evento de unión de un jugador
     * @param event Evento de unión
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Inicializar temperatura si es un jugador nuevo
        if (!temperatureLevel.containsKey(playerId)) {
            temperatureLevel.put(playerId, MAX_TEMPERATURE);
        }
    }
    
    /**
     * Maneja el evento de cambio de mundo de un jugador
     * @param event Evento de cambio de mundo
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        // Actualizar temperatura inmediatamente al cambiar de mundo
        updatePlayerTemperature(player);
    }
    
    /**
     * Verifica si el sistema está activo
     * @return true si el sistema está activo, false en caso contrario
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Establece la tasa de recuperación de temperatura
     * @param rate Tasa de recuperación (0.0 - 1.0)
     */
    public void setTemperatureIncreaseRate(double rate) {
        this.temperatureIncreaseRate = Math.max(0.0, Math.min(1.0, rate));
    }
    
    /**
     * Obtiene la tasa de recuperación de temperatura
     * @return Tasa de recuperación
     */
    public double getTemperatureIncreaseRate() {
        return temperatureIncreaseRate;
    }
    
    /**
     * Establece la tasa de disminución de temperatura
     * @param rate Tasa de disminución (0.0 - 1.0)
     */
    public void setTemperatureDecreaseRate(double rate) {
        this.temperatureDecreaseRate = Math.max(0.0, Math.min(1.0, rate));
    }
    
    /**
     * Obtiene la tasa de disminución de temperatura
     * @return Tasa de disminución
     */
    public double getTemperatureDecreaseRate() {
        return temperatureDecreaseRate;
    }
    
    /**
     * Detiene el sistema de temperatura
     */
    public void shutdown() {
        if (temperatureTask != null) {
            temperatureTask.cancel();
        }
        isActive = false;
        ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Sistema de temperatura desactivado"));
    }
    
    /**
     * Establece el nivel de temperatura de un jugador
     * @param player Jugador
     * @param level Nivel de temperatura a establecer (0-100)
     */
    public void setTemperatureLevel(Player player, int level) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        
        // Asegurar que el nivel esté dentro de los límites
        int newLevel = Math.max(MIN_TEMPERATURE, Math.min(MAX_TEMPERATURE, level));
        
        // Actualizar nivel
        temperatureLevel.put(playerId, newLevel);
        
        // Aplicar efectos si el nivel es bajo
        if (newLevel <= HYPOTHERMIA_THRESHOLD) {
            applyHypothermiaEffects(player);
        }
    }

    public int getMaxTemperature() {
        return MAX_TEMPERATURE;
    }
}
