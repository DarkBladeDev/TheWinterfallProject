package com.darkbladedev.mechanics;

import com.darkbladedev.WinterfallMain;
import com.darkbladedev.items.ItemManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sistema que gestiona la temperatura corporal de los jugadores
 * Los jugadores pueden morir de hipotermia si no llevan equipamiento adecuado
 */
public class TemperatureSystem implements Listener {

    // Constantes del sistema
    public static final int MAX_TEMPERATURE = 100; // Temperatura máxima (normal)
    public static final int MIN_TEMPERATURE = 0;   // Temperatura mínima (hipotermia severa)
    public static final int HYPOTHERMIA_THRESHOLD = 30; // Umbral para efectos de hipotermia
    public static final int SEVERE_HYPOTHERMIA_THRESHOLD = 15; // Umbral para hipotermia severa
    
    // Configuración
    private boolean isEnabled;
    private int updateInterval; // Intervalo de actualización en ticks
    private int temperatureDecreaseAmount; // Cantidad que disminuye la temperatura
    private int temperatureIncreaseAmount; // Cantidad que aumenta la temperatura
    private double temperatureDecreaseRate; // Tasa de disminución (0.0 - 1.0)
    private double temperatureIncreaseRate; // Tasa de aumento (0.0 - 1.0)
    
    // Referencias
    private final WinterfallMain plugin;
    private final ItemManager itemManager;
    
    // Estado
    private boolean isActive;
    private BukkitTask temperatureTask;
    private final Map<UUID, Integer> temperatureLevel;
    
    /**
     * Constructor del sistema de temperatura
     * @param plugin Instancia del plugin principal
     */
    public TemperatureSystem(WinterfallMain plugin) {
        this.plugin = plugin;
        this.itemManager = plugin.getItemManager();
        this.temperatureLevel = new HashMap<>();
        this.isActive = false;
        
        // Cargar configuración
        loadConfig();
    }
    
    /**
     * Carga la configuración del sistema desde config.yml
     */
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
            plugin.saveConfig();
        }
        
        // Cargar valores
        isEnabled = config.getBoolean("temperature.enabled", true);
        updateInterval = config.getInt("temperature.updateInterval", 100);
        temperatureDecreaseAmount = config.getInt("temperature.decreaseAmount", 1);
        temperatureIncreaseAmount = config.getInt("temperature.increaseAmount", 2);
        temperatureDecreaseRate = config.getDouble("temperature.decreaseRate", 0.8);
        temperatureIncreaseRate = config.getDouble("temperature.increaseRate", 0.5);
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
            
            ((Audience) (Bukkit.getConsoleSender())).sendMessage(MiniMessage.miniMessage().deserialize("<green>[Winterfall] Sistema de temperatura activado"));
        } else {
            ((Audience) (Bukkit.getConsoleSender())).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>[Winterfall] Sistema de temperatura desactivado en la configuración"));
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
        }.runTaskTimer(plugin, updateInterval, updateInterval);
        
        isActive = true;
    }
    
    /**
     * Actualiza la temperatura de un jugador basado en su entorno y equipamiento
     * @param player Jugador a actualizar
     */
    private void updatePlayerTemperature(Player player) {
        if (player == null || !player.isOnline()) return;
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar si es necesario
        if (!temperatureLevel.containsKey(playerId)) {
            temperatureLevel.put(playerId, MAX_TEMPERATURE);
        }
        
        // Verificar si el jugador tiene el traje de protección
        boolean hasProtection = checkPlayerProtection(player);
        
        // Verificar si el jugador está en un bioma frío o en un mundo con nieve
        boolean isInColdEnvironment = isInColdEnvironment(player);
        
        // Actualizar temperatura
        if (isInColdEnvironment && !hasProtection) {
            // Disminuir temperatura si está en ambiente frío sin protección
            decreaseTemperature(player, temperatureDecreaseAmount);
        } else if (hasProtection || !isInColdEnvironment) {
            // Aumentar temperatura si tiene protección o no está en ambiente frío
            increaseTemperature(player, temperatureIncreaseAmount);
        }
        
        // Aplicar efectos de hipotermia si es necesario
        applyHypothermiaEffects(player);
    }
    
    /**
     * Verifica si el jugador está en un ambiente frío
     * @param player Jugador a verificar
     * @return true si está en ambiente frío, false en caso contrario
     */
    @SuppressWarnings("removal")
    private boolean isInColdEnvironment(Player player) {
        World world = player.getWorld();
        String biomeName = player.getLocation().getBlock().getBiome().name().toLowerCase();
        
        // Verificar si el mundo tiene nieve activa (usando el sistema de nevada si está disponible)
        boolean hasSnowfall = false;
        SnowfallSystem snowfallSystem = plugin.getSnowfallSystem();
        if (snowfallSystem != null) {
            hasSnowfall = snowfallSystem.isWorldEnabled(world.getName());
        }
        
        // Verificar si el bioma es frío
        boolean isColdBiome = biomeName.contains("cold") || 
                             biomeName.contains("frozen") || 
                             biomeName.contains("ice") || 
                             biomeName.contains("snow") || 
                             biomeName.contains("taiga");
        
        return hasSnowfall || isColdBiome;
    }
    
    /**
     * Verifica si el jugador tiene equipamiento de protección contra el frío
     * @param player Jugador a verificar
     * @return true si tiene protección, false en caso contrario
     */
    private boolean checkPlayerProtection(Player player) {
        if (itemManager == null) return false;
        
        // Verificar si el jugador tiene el traje aislante completo
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack chestplate = player.getInventory().getChestplate();
        ItemStack leggings = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();
        
        return itemManager.hasFullIsolationSuit(helmet, chestplate, leggings, boots);
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
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, updateInterval + 20, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, updateInterval + 20, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, updateInterval + 20, 0));
            
            // Daño por hipotermia severa
            player.damage(1.0);
            ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>¡Estás sufriendo hipotermia severa! Necesitas protección contra el frío urgentemente."));
        } else if (temperature <= HYPOTHERMIA_THRESHOLD) {
            // Hipotermia moderada - efectos de movimiento y debilidad
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, updateInterval + 20, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, updateInterval + 20, 1));
            
            // Mensaje de advertencia
            if (temperature % 5 == 0) { // Mostrar mensaje cada 5 puntos de temperatura
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<red>Estás comenzando a sufrir hipotermia. Necesitas protección contra el frío."));
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
        ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize("<green>[Winterfall] Sistema de temperatura desactivado"));
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
