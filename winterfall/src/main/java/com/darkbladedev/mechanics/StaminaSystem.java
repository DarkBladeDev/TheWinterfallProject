package com.darkbladedev.mechanics;

import com.darkbladedev.WinterfallMain;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sistema que maneja la estamina de los jugadores
 * Reduce la estamina cuando el jugador corre y aplica efectos negativos
 * cuando la estamina está baja
 */
public class StaminaSystem implements Listener {

    private final WinterfallMain plugin;
    private BukkitTask staminaTask;
    private final Map<UUID, Integer> staminaLevel;
    private final Map<UUID, Long> lastSprintTime;
    private boolean isActive;
    
    // Constantes y configuraciones
    private int MAX_STAMINA;
    private int STAMINA_RECOVERY_AMOUNT; // Cantidad de estamina que se recupera en reposo
    private int STAMINA_DECREASE_AMOUNT; // Cantidad de estamina que se pierde al correr
    private int STAMINA_EFFECT_THRESHOLD; // Nivel por debajo del cual se empiezan a aplicar efectos
    private int UPDATE_INTERVAL; // Intervalo de actualización en ticks
    
    // Factores de recuperación y disminución (configurables)
    private double staminaRecoveryRate; // Probabilidad de recuperación en reposo
    private double staminaDecreaseRate; // Probabilidad de disminución al correr
    
    /**
     * Constructor del sistema de estamina
     * @param plugin Instancia del plugin principal
     */
    public StaminaSystem(WinterfallMain plugin) {
        this.plugin = plugin;
        this.staminaLevel = new HashMap<>();
        this.lastSprintTime = new HashMap<>();
        this.isActive = false;
        
        // Cargar configuración
        loadConfig();
    }
    
    /**
     * Carga la configuración desde config.yml
     */
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        
        // Cargar valores desde la configuración o usar valores predeterminados
        MAX_STAMINA = config.getInt("stamina.max_level", 20);
        STAMINA_RECOVERY_AMOUNT = config.getInt("stamina.recovery_amount", 1);
        STAMINA_DECREASE_AMOUNT = config.getInt("stamina.decrease_amount", 1);
        STAMINA_EFFECT_THRESHOLD = config.getInt("stamina.effect_threshold", 6);
        UPDATE_INTERVAL = config.getInt("stamina.update_interval", 20);
        
        // Cargar tasas de recuperación y disminución
        staminaRecoveryRate = config.getDouble("stamina.recovery_rate", 0.3);
        staminaDecreaseRate = config.getDouble("stamina.decrease_rate", 0.5);
    }
    
    /**
     * Inicializa el sistema de estamina
     */
    public void initialize() {
        // Verificar si el sistema está habilitado en la configuración
        if (!plugin.getConfig().getBoolean("stamina.enabled", true)) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Winterfall] Sistema de estamina deshabilitado en la configuración");
            return;
        }
        
        startStaminaSystem();
        isActive = true;
        
        // Registrar eventos
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Winterfall] Sistema de estamina inicializado");
    }
    
    /**
     * Inicia el sistema de estamina
     */
    private void startStaminaSystem() {
        staminaTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    
                    // Inicializar estamina si es necesario
                    if (!staminaLevel.containsKey(playerId)) {
                        staminaLevel.put(playerId, MAX_STAMINA);
                    }
                    
                    // Obtener nivel actual
                    int currentLevel = staminaLevel.get(playerId);
                    
                    // Verificar si el jugador está corriendo
                    if (player.isSprinting()) {
                        // Registrar el último tiempo de sprint
                        lastSprintTime.put(playerId, System.currentTimeMillis());
                        
                        // Reducir estamina si está corriendo
                        if (Math.random() < staminaDecreaseRate) {
                            decreaseStamina(player, STAMINA_DECREASE_AMOUNT);
                        }
                    } else {
                        // Recuperar estamina si no está corriendo y ha pasado tiempo suficiente
                        Long lastSprint = lastSprintTime.getOrDefault(playerId, 0L);
                        long currentTime = System.currentTimeMillis();
                        
                        // Recuperar estamina después de 2 segundos sin correr
                        if (currentTime - lastSprint > 2000 && currentLevel < MAX_STAMINA) {
                            if (Math.random() < staminaRecoveryRate) {
                                increaseStamina(player, STAMINA_RECOVERY_AMOUNT);
                            }
                        }
                    }
                    
                    // Aplicar efectos si la estamina es baja
                    if (currentLevel <= STAMINA_EFFECT_THRESHOLD) {
                        applyLowStaminaEffects(player, currentLevel);
                    }
                }
            }
        }.runTaskTimer(plugin, UPDATE_INTERVAL, UPDATE_INTERVAL); // Ejecutar según el intervalo configurado
    }
    
    /**
     * Aplica los efectos de baja estamina según el nivel
     * @param player Jugador afectado
     * @param level Nivel de estamina
     */
    private void applyLowStaminaEffects(Player player, int level) {
        // Verificar si el jugador tiene permiso para bypass
        if (player.hasPermission("winterfall.bypass.stamina")) {
            return; // No aplicar efectos si tiene el permiso
        }
        
        // Efectos según el nivel de estamina
        if (level <= 0) {
            // Estamina agotada: efectos graves
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
            
            // Detener el sprint del jugador
            player.setSprinting(false);
            
            // Mensaje (con probabilidad para no spamear)
            if (Math.random() < 0.3) {
                player.sendMessage(ChatColor.DARK_RED + "¡Estás completamente agotado! Necesitas descansar.");
            }
        } else if (level <= 3) {
            // Estamina muy baja: efectos moderados
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0));
            
            // Mensaje (con probabilidad para no spamear)
            if (Math.random() < 0.2) {
                player.sendMessage(ChatColor.RED + "Te sientes muy cansado. Deberías dejar de correr.");
            }
        } else if (level <= STAMINA_EFFECT_THRESHOLD) {
            // Estamina baja: efectos leves
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0));
            
            // Mensaje (con probabilidad para no spamear)
            if (Math.random() < 0.1) {
                player.sendMessage(ChatColor.GOLD + "Estás empezando a cansarte.");
            }
        }
    }
    
    /**
     * Disminuye el nivel de estamina de un jugador
     * @param player Jugador
     * @param amount Cantidad a disminuir
     */
    public void decreaseStamina(Player player, int amount) {
        // Verificar si el jugador tiene permiso para bypass
        if (player.hasPermission("winterfall.bypass.stamina")) {
            return; // No disminuir estamina si tiene el permiso
        }
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar si es necesario
        if (!staminaLevel.containsKey(playerId)) {
            staminaLevel.put(playerId, MAX_STAMINA);
        }
        
        // Obtener nivel actual y reducir
        int currentLevel = staminaLevel.get(playerId);
        int newLevel = Math.max(0, currentLevel - amount);
        
        // Actualizar nivel
        staminaLevel.put(playerId, newLevel);
    }
    
    /**
     * Aumenta el nivel de estamina de un jugador
     * @param player Jugador
     * @param amount Cantidad a aumentar
     */
    public void increaseStamina(Player player, int amount) {
        UUID playerId = player.getUniqueId();
        
        // Inicializar si es necesario
        if (!staminaLevel.containsKey(playerId)) {
            staminaLevel.put(playerId, MAX_STAMINA);
        }
        
        // Obtener nivel actual y aumentar
        int currentLevel = staminaLevel.get(playerId);
        int newLevel = Math.min(MAX_STAMINA, currentLevel + amount);
        
        // Actualizar nivel
        staminaLevel.put(playerId, newLevel);
    }
    
    /**
     * Obtiene el nivel de estamina de un jugador
     * @param player Jugador
     * @return Nivel de estamina (0-20)
     */
    public int getStaminaLevel(Player player) {
        if (player == null) return 0;
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar si es necesario
        if (!staminaLevel.containsKey(playerId)) {
            staminaLevel.put(playerId, MAX_STAMINA);
        }
        
        return staminaLevel.get(playerId);
    }
    
    /**
     * Obtiene el nivel de estamina de un jugador como porcentaje
     * @param player Jugador
     * @return Nivel de estamina (0-100%)
     */
    public int getStaminaPercentage(Player player) {
        int level = getStaminaLevel(player);
        return (level * 100) / MAX_STAMINA;
    }
    
    /**
     * Genera una barra de progreso visual para la estamina
     * @param player Jugador
     * @return Barra de progreso como texto
     */
    public String getStaminaBar(Player player) {
        int percentage = getStaminaPercentage(player);
        
        StringBuilder bar = new StringBuilder();
        MiniMessage mm = MiniMessage.miniMessage();
        // Determinar color según nivel
        Component barColor;
        if (percentage > 70) {
            barColor = mm.deserialize("<green>"); // Buena estamina
        } else if (percentage > 30) {
            barColor = mm.deserialize("<yellow>"); // Estamina media
        } else {
            barColor = mm.deserialize("<red>"); // Estamina baja
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
     * Maneja el evento de movimiento del jugador para detectar sprint
     * @param event Evento de movimiento
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Si el jugador está corriendo y tiene estamina 0, detener el sprint
        if (player.isSprinting() && getStaminaLevel(player) <= 0) {
            player.setSprinting(false);
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
        
        // Inicializar estamina si es un jugador nuevo
        if (!staminaLevel.containsKey(playerId)) {
            staminaLevel.put(playerId, MAX_STAMINA);
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
     * Establece la tasa de recuperación de estamina
     * @param rate Tasa de recuperación (0.0 - 1.0)
     */
    public void setStaminaRecoveryRate(double rate) {
        this.staminaRecoveryRate = Math.max(0.0, Math.min(1.0, rate));
    }
    
    /**
     * Obtiene la tasa de recuperación de estamina
     * @return Tasa de recuperación
     */
    public double getStaminaRecoveryRate() {
        return staminaRecoveryRate;
    }
    
    /**
     * Establece la tasa de disminución de estamina
     * @param rate Tasa de disminución (0.0 - 1.0)
     */
    public void setStaminaDecreaseRate(double rate) {
        this.staminaDecreaseRate = Math.max(0.0, Math.min(1.0, rate));
    }
    
    /**
     * Obtiene la tasa de disminución de estamina
     * @return Tasa de disminución
     */
    public double getStaminaDecreaseRate() {
        return staminaDecreaseRate;
    }
    
    /**
     * Detiene el sistema de estamina
     */
    public void shutdown() {
        if (staminaTask != null) {
            staminaTask.cancel();
        }
        isActive = false;
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Winterfall] Sistema de estamina desactivado");
    }
    
    /**
     * Establece el nivel de estamina de un jugador
     * @param player Jugador
     * @param level Nivel de estamina a establecer (0-20)
     */
    public void setStaminaLevel(Player player, int level) {
        UUID playerId = player.getUniqueId();
        
        // Asegurar que el nivel esté dentro de los límites
        int newLevel = Math.max(0, Math.min(MAX_STAMINA, level));
        
        // Actualizar nivel
        staminaLevel.put(playerId, newLevel);
        
        // Aplicar efectos si el nivel es bajo
        if (newLevel <= STAMINA_EFFECT_THRESHOLD) {
            applyLowStaminaEffects(player, newLevel);
        }
    }
}
