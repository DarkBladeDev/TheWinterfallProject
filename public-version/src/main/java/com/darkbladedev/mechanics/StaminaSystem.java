package com.darkbladedev.mechanics;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.mechanics.auraskills.stats.StaminaSystemExpansion;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
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

    private final SavageFrontierMain plugin;
    private BukkitTask staminaTask;
    private final Map<UUID, Integer> staminaLevel;
    private final Map<UUID, Long> lastSprintTime;
    private boolean isActive;
    private StaminaSystemExpansion auraSkillsIntegration;
    
    // Constantes y configuraciones
    private int BASE_MAX_STAMINA; // Valor base de estamina máxima
    private int STAMINA_RECOVERY_AMOUNT; // Cantidad de estamina que se recupera en reposo
    private int STAMINA_DECREASE_AMOUNT; // Cantidad de estamina que se pierde al correr
    private int STAMINA_EFFECT_THRESHOLD; // Nivel por debajo del cual se empiezan a aplicar efectos
    private int UPDATE_INTERVAL; // Intervalo de actualización en ticks
    
    // Factores de recuperación y disminución (configurables)
    private double staminaRecoveryRate; // Probabilidad de recuperación en reposo
    private double staminaDecreaseRate; // Probabilidad de disminución al correr
    
    // Mapas para almacenar modificadores personalizados por jugador
    private final Map<UUID, Integer> maxStaminaModifiers;
    private final Map<UUID, Double> recoveryRateModifiers;
    
    /**
     * Constructor del sistema de estamina
     * @param plugin Instancia del plugin principal
     */
    public StaminaSystem(SavageFrontierMain plugin) {
        this.plugin = plugin;
        this.staminaLevel = new HashMap<>();
        this.lastSprintTime = new HashMap<>();
        this.maxStaminaModifiers = new HashMap<>();
        this.recoveryRateModifiers = new HashMap<>();
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
        BASE_MAX_STAMINA = config.getInt("stamina.max_level", 20);
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
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Sistema de estamina deshabilitado en la configuración"));
            return;
        }
        
        // Inicializar integración con AuraSkills
        this.auraSkillsIntegration = new StaminaSystemExpansion(plugin, this, AuraSkillsApi.get());
        
        startStaminaSystem();
        isActive = true;
        
        // Registrar eventos
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Sistema de estamina inicializado"));
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
                        staminaLevel.put(playerId, getMaxStamina(player));
                    }
                    
                    // Obtener nivel actual
                    int currentLevel = staminaLevel.get(playerId);
                    
                    // Verificar si el jugador está corriendo
                    if (player.isSprinting()) {
                        // Registrar el último tiempo de sprint
                        lastSprintTime.put(playerId, System.currentTimeMillis());
                        
                        // Reducir estamina si está corriendo y no está protegido como nuevo jugador
                        if (Math.random() < staminaDecreaseRate && !plugin.isPlayerProtectedFromSystem(player, "stamina")) {
                            decreaseStamina(player, STAMINA_DECREASE_AMOUNT);
                        }
                    } else {
                        // Recuperar estamina si no está corriendo y ha pasado tiempo suficiente
                        Long lastSprint = lastSprintTime.getOrDefault(playerId, 0L);
                        long currentTime = System.currentTimeMillis();
                        
                        // Recuperar estamina después de 2 segundos sin correr
                        if (currentTime - lastSprint > 2000 && currentLevel < getMaxStamina(player)) {
                            // Usar la tasa de recuperación personalizada del jugador
                            if (Math.random() < getPlayerStaminaRecoveryRate(player)) {
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
        // Verificar si el jugador tiene permiso para bypass o está protegido como nuevo jugador
        if (player.hasPermission("savage.bypass.stamina") || plugin.isPlayerProtectedFromSystem(player, "stamina")) {
            return; // No aplicar efectos si tiene el permiso o está protegido
        }
        
        // Efectos según el nivel de estamina
        if (level <= 0) {
            // Estamina agotada: efectos graves
            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.3);
            plugin.getCustomDebuffEffects().applyWeakness(player);
            
            // Detener el sprint del jugador
            player.setSprinting(false);
            
            // Mensaje (con probabilidad para no spamear)
            if (Math.random() < 0.03 && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>¡Estás completamente agotado! Necesitas descansar."));
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<dark_red>¡Estás completamente agotado!"));
            }
        } else if (level <= 3) {
            // Estamina muy baja: efectos moderados
            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.6);
            plugin.getCustomDebuffEffects().applyWeakness(player);
            
            
            // Mensaje (con probabilidad para no spamear)
            if (Math.random() < 0.02 && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Te sientes muy cansado. Deberías dejar de correr."));
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>Te sientes muy cansado."));

            }
        } else if (level <= STAMINA_EFFECT_THRESHOLD) {
            // Estamina baja: efectos leves
            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.8);
            
            // Mensaje (con probabilidad para no spamear)
            if (Math.random() < 0.01 && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<gold>Estás empezando a cansarte."));
                player.sendActionBar(MiniMessage.miniMessage().deserialize("<gold>Estás empezando a cansarte."));

            }
        }
    }
    
    /**
     * Disminuye el nivel de estamina de un jugador
     * @param player Jugador
     * @param amount Cantidad a disminuir
     */
    public void decreaseStamina(Player player, int amount) {
        // Verificar si el jugador tiene permiso para bypass o está protegido como nuevo jugador
        if (player.hasPermission("savage.bypass.stamina") || plugin.isPlayerProtectedFromSystem(player, "stamina")) {
            return; // No disminuir estamina si tiene el permiso o está protegido
        }
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar si es necesario
        if (!staminaLevel.containsKey(playerId)) {
            staminaLevel.put(playerId, getMaxStamina(player));
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
            staminaLevel.put(playerId, getMaxStamina(player));
        }
        
        // Obtener nivel actual y aumentar
        int currentLevel = staminaLevel.get(playerId);
        int newLevel = Math.min(getMaxStamina(player), currentLevel + amount);
        
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
            staminaLevel.put(playerId, getMaxStamina(player));
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
        return (level * 100) / getMaxStamina(player);
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
     * Maneja el evento de activación/desactivación del sprint
     * @param event Evento de toggle sprint
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Player player = event.getPlayer();
        
        // Si el jugador está intentando activar el sprint
        if (event.isSprinting()) {
            // Si el jugador tiene estamina 0 o está protegido pero intenta correr con estamina baja
            if (getStaminaLevel(player) <= 0 || 
                (plugin.isPlayerProtectedFromSystem(player, "stamina") && getStaminaLevel(player) <= STAMINA_EFFECT_THRESHOLD)) {
                // Cancelar el sprint
                event.setCancelled(true);
                
                // Informar al jugador si tiene estamina 0
                if (getStaminaLevel(player) <= 0 && Math.random() < 0.3 && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Estás demasiado cansado para correr."));
                    player.sendActionBar(MiniMessage.miniMessage().deserialize("<red>Estás demasiado cansado para correr."));

                }
            }
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
            staminaLevel.put(playerId, getMaxStamina(player));
        }
        
        // Actualizar modificadores si hay integración con AuraSkills
        if (auraSkillsIntegration != null && auraSkillsIntegration.isEnabled()) {
            auraSkillsIntegration.updatePlayerModifiers(player);
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
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Sistema de estamina desactivado"));
    }
    
    /**
     * Establece el nivel de estamina de un jugador
     * @param player Jugador
     * @param level Nivel de estamina a establecer (0-20)
     */
    public void setStaminaLevel(Player player, int level) {
        UUID playerId = player.getUniqueId();
        
        // Asegurar que el nivel esté dentro de los límites
        int newLevel = Math.max(0, Math.min(getMaxStamina(player), level));
        
        // Actualizar nivel
        staminaLevel.put(playerId, newLevel);
        
        // Aplicar efectos si el nivel es bajo
        if (newLevel <= STAMINA_EFFECT_THRESHOLD) {
            applyLowStaminaEffects(player, newLevel);
        }
    }

    /**
     * Obtiene la estamina máxima base sin modificadores
     * @return Estamina máxima base
     */
    public int getBaseMaxStamina() {
        return BASE_MAX_STAMINA;
    }
    
    /**
     * Obtiene la estamina máxima de un jugador considerando modificadores y AuraSkills
     * @param player Jugador
     * @return Estamina máxima del jugador
     */
    public int getMaxStamina(Player player) {
        if (player == null) return BASE_MAX_STAMINA;
        
        UUID playerId = player.getUniqueId();
        int maxStamina = BASE_MAX_STAMINA;
        
        // Aplicar modificador local si existe
        if (maxStaminaModifiers.containsKey(playerId)) {
            maxStamina += maxStaminaModifiers.get(playerId);
        }
        // Integración con AuraSkills: sumar modificador de capacidad si está habilitada
        if (auraSkillsIntegration != null && auraSkillsIntegration.isEnabled()) {
            maxStamina += auraSkillsIntegration.calculateStaminaCapacityModifier(player);
        }
        return Math.max(1, maxStamina); // Asegurar que siempre sea al menos 1
    }
    
    /**
     * Establece un modificador de estamina máxima para un jugador
     * @param player Jugador
     * @param modifier Valor del modificador (puede ser positivo o negativo)
     */
    public void setMaxStaminaModifier(Player player, int modifier) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        maxStaminaModifiers.put(playerId, modifier);
        
        // Ajustar el nivel actual si excede el nuevo máximo
        if (staminaLevel.containsKey(playerId)) {
            int currentLevel = staminaLevel.get(playerId);
            int maxStamina = getMaxStamina(player);
            
            if (currentLevel > maxStamina) {
                staminaLevel.put(playerId, maxStamina);
            }
        }
    }
    
    /**
     * Obtiene el modificador de estamina máxima de un jugador
     * @param player Jugador
     * @return Valor del modificador
     */
    public int getMaxStaminaModifier(Player player) {
        if (player == null) return 0;
        
        UUID playerId = player.getUniqueId();
        return maxStaminaModifiers.getOrDefault(playerId, 0);
    }
    
    /**
     * Obtiene la tasa de recuperación de estamina para un jugador específico, considerando AuraSkills
     * @param player Jugador
     * @return Tasa de recuperación personalizada
     */
    public double getPlayerStaminaRecoveryRate(Player player) {
        if (player == null) return staminaRecoveryRate;
        
        UUID playerId = player.getUniqueId();
        double baseRate = staminaRecoveryRate;
        
        // Aplicar modificador local si existe
        if (recoveryRateModifiers.containsKey(playerId)) {
            baseRate += recoveryRateModifiers.get(playerId);
        }
        // Integración con AuraSkills: sumar modificador de recuperación si está habilitada
        if (auraSkillsIntegration != null && auraSkillsIntegration.isEnabled()) {
            baseRate += auraSkillsIntegration.calculateStaminaRecoveryModifier(player);
        }
        return Math.max(0.0, Math.min(1.0, baseRate)); // Limitar entre 0 y 1
    }
    
    /**
     * Establece un modificador de tasa de recuperación para un jugador
     * @param player Jugador
     * @param modifier Valor del modificador (puede ser positivo o negativo)
     */
    public void setRecoveryRateModifier(Player player, double modifier) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        recoveryRateModifiers.put(playerId, modifier);
    }
    
    /**
     * Obtiene el modificador de tasa de recuperación de un jugador
     * @param player Jugador
     * @return Valor del modificador
     */
    public double getRecoveryRateModifier(Player player) {
        if (player == null) return 0.0;
        
        UUID playerId = player.getUniqueId();
        return recoveryRateModifiers.getOrDefault(playerId, 0.0);
    }
    
    /**
     * Elimina todos los modificadores de un jugador
     * @param player Jugador
     */
    public void clearPlayerModifiers(Player player) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        maxStaminaModifiers.remove(playerId);
        recoveryRateModifiers.remove(playerId);
    }
}
