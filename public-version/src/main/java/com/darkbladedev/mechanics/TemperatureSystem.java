package com.darkbladedev.mechanics;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.CustomTypes.CustomDamageTypes;
import com.darkbladedev.CustomTypes.CustomEnchantments;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.damage.DamageSource;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.io.IOException;
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
    public final int MAX_TEMPERATURE = 40; // Temperatura máxima (40°C)
    public final int MIN_TEMPERATURE = -15;   // Temperatura mínima (-15°C)
    public static final int DEFAULT_HYPOTHERMIA_THRESHOLD = 5; // Umbral para efectos de hipotermia
    public static final int DEFAULT_SEVERE_HYPOTHERMIA_THRESHOLD = -5; // Umbral para hipotermia severa
    public static final int DEFAULT_HYPERTHERMIA_THRESHOLD = 30; // Umbral para efectos de hipertermia
    public static final int DEFAULT_SEVERE_HYPERTHERMIA_THRESHOLD = 35; // Umbral para hipertermia severa
    
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
    @SuppressWarnings("unused")
    private int coldProtectionEffectiveness; // Efectividad del encantamiento de protección contra frío
    @SuppressWarnings("unused")
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
    
    
    // Referencias
    private final SavageFrontierMain plugin;
    private String temperatureConfigFile;
    
    // Estado
    private boolean isActive;
    private BukkitTask temperatureTask;
    public final Map<UUID, Integer> temperatureLevel;
    
    // Mapa de temperaturas por bioma
    private final Map<String, Integer> biomeTemperatures;

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
        updateInterval = config.getInt("temperature.updateInterval", 60);
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
        String pluginPath = plugin.getDataFolder().getParentFile().getAbsolutePath();
        temperatureConfigFile = pluginPath + "/biome_temperatures.yml";
        File temperatureFile = new File(pluginPath, "biome_temperatures.yml");
        // Crear temperatureConfigFile si no existe
        File temperatureConfigDir = new File(temperatureConfigFile).getParentFile();
        if (!temperatureConfigDir.exists()) {
            try {
                temperatureConfigDir.mkdirs();
                if (temperatureFile.exists()) {
                    temperatureFile.createNewFile();
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error al crear el archivo de configuración de temperaturas: " + e.getMessage());
            }
        }

        // Cargar temperaturas por bioma desde la configuración
        FileConfiguration biomeConfig = YamlConfiguration.loadConfiguration(new File(temperatureConfigFile));
        ConfigurationSection biomesSection = biomeConfig.getConfigurationSection("temperature.biomes");
        if (biomesSection == null) {
            // Crear sección de biomas si no existe
            biomesSection = biomeConfig.createSection("temperature.biomes");
            
            // Configurar temperaturas por defecto para algunos biomas comunes
            // Biomas fríos
            biomesSection.set("SNOWY_PLAINS", -10);
            biomesSection.set("ICE_SPIKES", -15);
            biomesSection.set("FROZEN_OCEAN", -12);
            biomesSection.set("FROZEN_RIVER", -8);
            biomesSection.set("SNOWY_TAIGA", -5);
            
            // Biomas templados
            biomesSection.set("PLAINS", 15);
            biomesSection.set("FOREST", 12);
            biomesSection.set("BIRCH_FOREST", 13);
            biomesSection.set("DARK_FOREST", 10);
            biomesSection.set("TAIGA", 5);
            
            // Biomas cálidos
            biomesSection.set("DESERT", 38);
            biomesSection.set("SAVANNA", 32);
            biomesSection.set("BADLANDS", 35);
            biomesSection.set("JUNGLE", 30);
            
            // Biomas extremos
            biomesSection.set("NETHER_WASTES", 40);
            biomesSection.set("SOUL_SAND_VALLEY", 38);
            biomesSection.set("CRIMSON_FOREST", 40);
            biomesSection.set("WARPED_FOREST", 39);
            biomesSection.set("BASALT_DELTAS", 40);
            
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
                biomeConfig.set("temperature.biomes." + biomeName, defaultTemp);
            }
        }
        
        // Guardar cambios en la configuración
        plugin.saveConfig();
        try {
            biomeConfig.save(temperatureFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                if (!isActive) {
                    this.cancel();
                    return;
                }
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerTemperature(player);
                    // Enviar mensaje de depuración al jugador para confirmar que la temperatura se está actualizando
                    if (player.isOp()) {
                        //((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<gray>Temperatura actualizada: " + getPlayerTemperature(player) + "°C"));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, updateInterval);
        
        isActive = true;
        
        // Mensaje de depuración
        plugin.getLogger().info("Sistema de temperatura iniciado con intervalo de " + updateInterval + " ticks (" + (updateInterval/20.0) + " segundos)");
    }

    /**
     * Normaliza la temperatura del jugador hacia un valor neutro (15°C)
     * @param player Jugador a normalizar
     */
    private void normalizeTemperature(Player player) {
        UUID playerId = player.getUniqueId();
        double currentTemp = temperatureLevel.getOrDefault(playerId, 15);
        
        if (currentTemp < 15) {
            // Si está frío, aumentar temperatura
            increaseTemperature(player, temperatureIncreaseAmount / 2);
        } else if (currentTemp > 15) {
            // Si está caliente, disminuir temperatura
            decreaseTemperature(player, temperatureDecreaseAmount / 2);
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
            temperatureLevel.put(playerId, 15); // Iniciar en temperatura neutral (15°C)
            plugin.getLogger().info("Inicializando temperatura para " + player.getName() + " a 15°C");
        }
        
        // Obtener temperatura actual antes de actualizar
        double tempAntes = getPlayerTemperature(player);
        
        // Verificar el entorno del jugador
        boolean isInColdEnvironment = isInColdEnvironment(player);
        boolean isInHotEnvironment = isInHotEnvironment(player);
        
        // Obtener nivel de protección contra frío y calor
        int coldProtectionLevel = getEnchantmentLevel(player, CustomEnchantments.COLD_PROTECTION_KEY);
        int heatProtectionLevel = getEnchantmentLevel(player, CustomEnchantments.HEAT_PROTECTION_KEY);
        
        // Actualizar temperatura según el entorno y protecciones
        if (isInColdEnvironment) {
            // Calcular reducción de efecto basada en nivel de protección
            double protectionFactor = 1.0;
            if (coldProtectionLevel > 0) {
                // Reducir el efecto del frío según el nivel de protección
                // Nivel 1: 30% de reducción, Nivel 2: 60% de reducción, Nivel 3: 90% de reducción
                protectionFactor = Math.max(0.1, 1.0 - (coldProtectionLevel * 0.3));
            }
            
            // Disminuir temperatura si está en ambiente frío, con reducción si tiene protección
            decreaseTemperature(player, (int)(temperatureDecreaseAmount * protectionFactor));
        } else if (isInHotEnvironment) {
            // Calcular reducción de efecto basada en nivel de protección
            double protectionFactor = 1.0;
            if (heatProtectionLevel > 0) {
                // Reducir el efecto del calor según el nivel de protección
                // Nivel 1: 30% de reducción, Nivel 2: 60% de reducción, Nivel 3: 90% de reducción
                protectionFactor = Math.max(0.1, 1.0 - (heatProtectionLevel * 0.3));
            }
            
            // Aumentar temperatura si está en ambiente caluroso, con reducción si tiene protección
            increaseTemperature(player, (int)(temperatureIncreaseAmount * 2 * protectionFactor));
        } else {
            // En biomas templados, normalizar la temperatura
            normalizeTemperature(player);
        }
        
        // Obtener temperatura después de actualizar
        double tempDespues = getPlayerTemperature(player);
        
        // Registrar cambio de temperatura para depuración
        if (tempAntes != tempDespues) {
            plugin.getLogger().info("Temperatura de " + player.getName() + " actualizada: " + tempAntes + "°C -> " + tempDespues + "°C");
        }
        
        // Aplicar efectos según la temperatura
        if (getPlayerTemperature(player) <= HYPOTHERMIA_THRESHOLD) {
            // Aplicar efectos de hipotermia si la temperatura es baja
            applyHypothermiaEffects(player, coldProtectionLevel);
        } else if (getPlayerTemperature(player) >= HYPERTHERMIA_THRESHOLD) {
            // Aplicar efectos de hipertermia si la temperatura es alta
            applyHyperthermiaEffects(player, heatProtectionLevel);
        }
    }

    
    /**
     * Verifica si el jugador tiene protección contra el frío
     * @param player Jugador a verificar
     * @return Nivel de protección contra el frío (0-3)
     */
    @SuppressWarnings("unused")
    private int hasProtectionAgainstCold(Player player) {
        // Obtener el nivel total de encantamiento de protección contra el frío
        int protectionLevel = getEnchantmentLevel(player, CustomEnchantments.COLD_PROTECTION_KEY);
        
        // Devolver el nivel de protección (0 si no tiene encantamiento)
        return protectionLevel;
    }
    
    /**
     * Verifica si el jugador tiene protección contra el calor
     * @param player Jugador a verificar
     * @return Nivel de protección contra el calor (0-3)
     */
    @SuppressWarnings("unused")
    private int hasProtectionAgainstHeat(Player player) {
        // Obtener el nivel total de encantamiento de protección contra el calor
        int protectionLevel = getEnchantmentLevel(player, CustomEnchantments.HEAT_PROTECTION_KEY);
        
        // Devolver el nivel de protección (0 si no tiene encantamiento)
        return protectionLevel;
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
        
        // Considerar frío si la temperatura del bioma es menor a 0°C
        return biomeTemperature < 0;
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
        
        // Considerar caluroso si la temperatura del bioma es mayor a 25°C
        return biomeTemperature > 25;
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
    @SuppressWarnings("unused")
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
            DamageSource damageSource = CustomDamageTypes.DamageSourceBuilder(player, player, CustomDamageTypes.HYPOTHERMIA_KEY);
            player.damage(2.0, damageSource);

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
     * Aplica efectos de hipotermia basados en el nivel de temperatura
     * @param player Jugador al que aplicar los efectos
     * @param protectionLevel Nivel de protección contra el frío
     */
    private void applyHypothermiaEffects(Player player, int protectionLevel) {
        int temperature = getTemperatureLevel(player);
        
        // Limpiar efectos anteriores si la temperatura es normal
        if (temperature > HYPOTHERMIA_THRESHOLD) {
            return;
        }
        
        // Calcular reducción de efectos basada en nivel de protección
        int effectReduction = protectionLevel;
        
        // Aplicar efectos según el nivel de hipotermia
        if (temperature <= SEVERE_HYPOTHERMIA_THRESHOLD) {
            // Hipotermia severa - daño y efectos graves
            // Reducir la potencia de los efectos según el nivel de protección
            int slownessAmplifier = Math.max(0, 2 - effectReduction);
            int weaknessAmplifier = Math.max(0, 2 - effectReduction);
            
            // Aplicar efectos reducidos según protección
            if (slownessAmplifier > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, updateInterval * 20 + 20, slownessAmplifier));
            }
            
            if (weaknessAmplifier > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, updateInterval * 20 + 20, weaknessAmplifier));
            }
            
            // Náusea solo si no tiene protección nivel 3
            if (protectionLevel < 3) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, updateInterval * 20 + 20, 0));
            }
            
            // Daño por hipotermia severa reducido según protección
            if (protectionLevel < 3) {
                DamageSource damageSource = CustomDamageTypes.DamageSourceBuilder(player, player, CustomDamageTypes.HYPOTHERMIA_KEY);
                double damageAmount = 1.5 * Math.max(0.1, 1.0 - (protectionLevel * 0.3));
                
                player.damage(damageAmount, damageSource);
            }
            
            // Mensaje según nivel de protección
            if (protectionLevel == 0) {
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>¡Estás sufriendo hipotermia severa! Necesitas protección contra el frío urgentemente."));
            } else if (protectionLevel < 3) {
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<red>Tu protección contra el frío no es suficiente para estas temperaturas extremas."));
            } else {
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Tu protección contra el frío te está salvando de la hipotermia severa."));
            }
        } else if (temperature <= HYPOTHERMIA_THRESHOLD) {
            // Hipotermia moderada - efectos de movimiento y debilidad
            // Solo aplicar si la protección no es suficiente
            if (protectionLevel < 2) {
                int slownessAmplifier = Math.max(0, 1 - effectReduction);
                int weaknessAmplifier = Math.max(0, 1 - effectReduction);
                
                if (slownessAmplifier > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, updateInterval * 20 + 20, slownessAmplifier));
                }
                
                if (weaknessAmplifier > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, updateInterval * 20 + 20, weaknessAmplifier));
                }
                
                // Mensaje de advertencia
                if (temperature % 5 == 0) { // Mostrar mensaje cada 5 puntos de temperatura
                    if (protectionLevel == 0) {
                        ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<red>Estás comenzando a sufrir hipotermia. Necesitas protección contra el frío."));
                    } else {
                        ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Tu protección contra el frío te está ayudando, pero necesitas más abrigo."));
                    }
                }
            }
        }
    }
    
    /**
     * Aplica efectos de hipertermia basados en el nivel de temperatura
     * @param player Jugador al que aplicar los efectos
     * @param protectionLevel Nivel de protección contra el calor
     */
    private void applyHyperthermiaEffects(Player player, int protectionLevel) {
        int temperature = getTemperatureLevel(player);
        
        // Limpiar efectos anteriores si la temperatura es normal
        if (temperature < HYPERTHERMIA_THRESHOLD) {
            return;
        }
        
        // Calcular reducción de efectos basada en nivel de protección
        int effectReduction = protectionLevel;
        
        // Aplicar efectos según el nivel de hipertermia
        if (temperature >= SEVERE_HYPERTHERMIA_THRESHOLD) {
            // Hipertermia severa - daño y efectos graves
            // Reducir la potencia de los efectos según el nivel de protección
            int hungerAmplifier = Math.max(0, 2 - effectReduction);
            int weaknessAmplifier = Math.max(0, 1 - effectReduction);
            
            // Aplicar efectos reducidos según protección
            if (hungerAmplifier > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, updateInterval * 20 + 20, hungerAmplifier));
            }
            
            if (weaknessAmplifier > 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, updateInterval * 20 + 20, weaknessAmplifier));
            }
            
            // Náusea solo si no tiene protección nivel 3
            if (protectionLevel < 3) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, updateInterval * 20 + 20, 0));
            }
            
            // Daño por hipertermia severa reducido según protección
            if (protectionLevel < 3) {
                DamageSource damageSource = CustomDamageTypes.DamageSourceBuilder(player, player, CustomDamageTypes.HYPERTHERMIA_KEY);
                double damageAmount = 1.5 * Math.max(0.1, 1.0 - (protectionLevel * 0.3));

                player.damage(damageAmount, damageSource);
            }
            
            // Mensaje según nivel de protección
            if (protectionLevel == 0) {
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>¡Estás sufriendo hipertermia severa! Necesitas protección contra el calor urgentemente."));
            } else if (protectionLevel < 3) {
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<red>Tu protección contra el calor no es suficiente para estas temperaturas extremas."));
            } else {
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Tu protección contra el calor te está salvando de la hipertermia severa."));
            }
        } else if (temperature >= HYPERTHERMIA_THRESHOLD) {
            // Hipertermia moderada - efectos de hambre y debilidad
            // Solo aplicar si la protección no es suficiente
            if (protectionLevel < 2) {
                int hungerAmplifier = Math.max(0, 1 - effectReduction);
                int weaknessAmplifier = Math.max(0, 0 - effectReduction); // Nivel 0 de debilidad
                
                if (hungerAmplifier > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, updateInterval * 20 + 20, hungerAmplifier));
                }
                
                if (weaknessAmplifier > 0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, updateInterval * 20 + 20, weaknessAmplifier));
                }
                
                // Mensaje de advertencia
                if (temperature % 5 == 0) { // Mostrar mensaje cada 5 puntos de temperatura
                    if (protectionLevel == 0) {
                        ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<red>Estás comenzando a sufrir hipertermia. Necesitas protección contra el calor."));
                    } else {
                        ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Tu protección contra el calor te está ayudando, pero necesitas más protección."));
                    }
                }
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
            temperatureLevel.put(playerId, 15); // Inicializar en temperatura neutral (15°C)
            plugin.getLogger().info("Inicializando temperatura para " + player.getName() + " a 15°C en decreaseTemperature");
        }
        
        // Obtener nivel actual y reducir
        int currentLevel = temperatureLevel.get(playerId);
        int newLevel = Math.max(MIN_TEMPERATURE, currentLevel - (int)(amount * temperatureDecreaseRate));
        
        // Registrar cambio para depuración
        if (currentLevel != newLevel) {
            plugin.getLogger().info("decreaseTemperature: " + player.getName() + " " + currentLevel + "°C -> " + newLevel + "°C (amount=" + amount + ", rate=" + temperatureDecreaseRate + ")");
        }
        
        // Actualizar nivel
        temperatureLevel.put(playerId, newLevel);
    }
    
    /**
     * Obtiene la temperatura actual de un jugador
     * @param player Jugador a consultar
     * @return Temperatura actual (0-100)
     */
    public double getPlayerTemperature(Player player) {
        if (player == null) return 15; // Temperatura neutral
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar si es necesario
        if (!temperatureLevel.containsKey(playerId)) {
            temperatureLevel.put(playerId, 15); // Inicializar en temperatura neutral (15°C)
            plugin.getLogger().info("Inicializando temperatura para " + player.getName() + " a 15°C en getPlayerTemperature");
            return 15;
        }
        
        return temperatureLevel.get(playerId);
    }
    
    /**
     * Disminuye la temperatura del jugador
     * @param player Jugador a afectar
     */
    @SuppressWarnings("unused")
    private void decreasePlayerTemperature(Player player) {
        decreaseTemperature(player, temperatureDecreaseAmount);
    }
    
    /**
     * Aumenta la temperatura del jugador
     * @param player Jugador a afectar
     */
    @SuppressWarnings("unused")
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
            temperatureLevel.put(playerId, 15); // Inicializar en temperatura neutral (15°C)
            plugin.getLogger().info("Inicializando temperatura para " + player.getName() + " a 15°C en increaseTemperature");
        }
        
        // Obtener nivel actual y aumentar
        int currentLevel = temperatureLevel.get(playerId);
        int newLevel = Math.min(MAX_TEMPERATURE, currentLevel + (int)(amount * temperatureIncreaseRate));
        
        // Registrar cambio para depuración
        if (currentLevel != newLevel) {
            plugin.getLogger().info("increaseTemperature: " + player.getName() + " " + currentLevel + "°C -> " + newLevel + "°C (amount=" + amount + ", rate=" + temperatureIncreaseRate + ")");
        }
        
        // Actualizar nivel
        temperatureLevel.put(playerId, newLevel);
    }
    
    /**
     * Obtiene el nivel de temperatura de un jugador
     * @param player Jugador
     * @return Nivel de temperatura (0-100)
     */
    public int getTemperatureLevel(Player player) {
        if (player == null) return 15; // Temperatura neutral
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar si es necesario
        if (!temperatureLevel.containsKey(playerId)) {
            temperatureLevel.put(playerId, 15); // Inicializar en temperatura neutral (15°C)
            plugin.getLogger().info("Inicializando temperatura para " + player.getName() + " a 15°C en getTemperatureLevel");
        }
        
        return temperatureLevel.get(playerId);
    }
    
    /**
     * Establece el nivel de temperatura de un jugador a un valor específico
     * @param player Jugador
     * @param temperature Temperatura a establecer
     */
    public void setTemperature(Player player, int temperature) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        
        // Asegurar que la temperatura esté dentro de los límites
        int newTemperature = Math.max(MIN_TEMPERATURE, Math.min(MAX_TEMPERATURE, temperature));
        
        // Actualizar nivel
        temperatureLevel.put(playerId, newTemperature);
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
     * Obtiene un mensaje formateado con el estado de temperatura del jugador
     * @param player Jugador a verificar
     * @return Componente con el mensaje formateado
     */
    public @NotNull Component getTemperatureStatusMessage(Player player) {
        int temperature = getTemperatureLevel(player);
        String temperatureBar = MiniMessage.miniMessage().serialize(getTemperatureBar(player));
        
        MiniMessage mm = MiniMessage.miniMessage();
        StringBuilder message = new StringBuilder();
        
        // Determinar el estado de temperatura
        String temperatureStatus;
        if (temperature <= SEVERE_HYPOTHERMIA_THRESHOLD) {
            temperatureStatus = "<dark_red>Hipotermia severa</dark_red>";
        } else if (temperature <= HYPOTHERMIA_THRESHOLD) {
            temperatureStatus = "<red>Hipotermia</red>";
        } else if (temperature >= SEVERE_HYPERTHERMIA_THRESHOLD) {
            temperatureStatus = "<dark_red>Hipertermia severa</dark_red>";
        } else if (temperature >= HYPERTHERMIA_THRESHOLD) {
            temperatureStatus = "<red>Hipertermia</red>";
        } else {
            temperatureStatus = "<green>Normal</green>";
        }
        
        message.append("<yellow>Estado de temperatura:</yellow> ").append(temperatureStatus);
        message.append("\n<yellow>Temperatura actual:</yellow> ").append(temperature).append("°C");
        message.append("\n<yellow>Nivel:</yellow> ").append(temperatureBar);
        
        return mm.deserialize(message.toString());
    }
    
    /**
     * Genera una barra de progreso visual para la temperatura
     * @param player Jugador
     * @return Barra de progreso como texto
     */
    public @NotNull Component getTemperatureBar(Player player) {
        int percentage = getTemperaturePercentage(player);
        
        StringBuilder bar = new StringBuilder();
        MiniMessage mm = MiniMessage.miniMessage();
        
        // Determinar color según nivel
        String barColor;
        if (percentage > 70) {
            barColor = "<green>"; // Temperatura normal
        } else if (percentage > 30) {
            barColor = "<yellow>"; // Temperatura baja
        } else {
            barColor = "<red>"; // Hipotermia
        }
        
        // Construir barra de progreso
        int bars = (int) Math.round(percentage / 10.0);
        
        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                bar.append("■");
            } else {
                bar.append("▢");
            }
        }
        
        return mm.deserialize(barColor + bar.toString());
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
            temperatureLevel.put(playerId, 15); // Inicializar en temperatura neutral (15°C)
            plugin.getLogger().info("Inicializando temperatura para " + player.getName() + " a 15°C en onPlayerJoin");
        }
        
        // Actualizar temperatura inmediatamente
        updatePlayerTemperature(player);
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
        ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Sistema de temperatura desactivado"));
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
            int coldProtectionLevel = getEnchantmentLevel(player, CustomEnchantments.COLD_PROTECTION_KEY);
            applyHypothermiaEffects(player, coldProtectionLevel);
        }
    }

    public int getMaxTemperature() {
        return MAX_TEMPERATURE;
    }
}
