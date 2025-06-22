package com.darkbladedev;

import com.darkbladedev.commands.SavageCommand;
import com.darkbladedev.database.DatabaseManager;
import com.darkbladedev.events.PlayerEvents;
import com.darkbladedev.integrations.PlaceholderAPIExpansion;
import com.darkbladedev.mechanics.BleedingSystem;
import com.darkbladedev.mechanics.RadiationSystem;
import com.darkbladedev.mechanics.LimbDamageSystem;
import com.darkbladedev.mechanics.HydrationSystem;
import com.darkbladedev.mechanics.NutritionSystem;
import com.darkbladedev.mechanics.StaminaSystem;
import com.darkbladedev.mechanics.TemperatureSystem;
import com.darkbladedev.mechanics.FreezingSystem;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;

import java.util.List;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Clase principal del plugin Savage Frontier"
 */
public class SavageFrontierMain extends JavaPlugin {
    
    private static SavageFrontierMain instance;
    private BleedingSystem bleedingSystem;
    private RadiationSystem radiationSystem;
    private LimbDamageSystem limbDamageSystem;
    private HydrationSystem hydrationSystem;
    private NutritionSystem nutritionSystem;
    private StaminaSystem staminaSystem;
    private TemperatureSystem temperatureSystem;
    private FreezingSystem freezingSystem;
    private DatabaseManager databaseManager;
    private PlaceholderAPIExpansion placeholders;
    private com.darkbladedev.managers.UserPreferencesManager userPreferencesManager;
    public static boolean hasExecutableItems = false;
    public static boolean hasItemsAdder = false;
    public final String PREFIX = "<gradient:#20f335:#4dd8e1>Savage Frontier</gradient>";
    
    // Configuración de protección para nuevos jugadores
    private boolean newPlayerProtectionEnabled;
    private long newPlayerProtectionDuration; // en minutos
    private java.util.Map<String, Boolean> protectedSystems;

    
    @Override
    public void onEnable() {
        // Guardar instancia para acceso estático
        instance = this;
        
        // Inicializar sistemas
        initializeSystems();
        
        // Registrar eventos
        registerEvents();
        
        // Registrar comandos
        registerCommands();
        
        // Registrar placeholders si PlaceholderAPI está presente
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholders = new PlaceholderAPIExpansion(instance);
            placeholders.register();
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(PREFIX + " <green>PlaceholderAPI detectado y placeholders registrados"));
        } else {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(PREFIX + " <yellow>PlaceholderAPI no detectado. Los placeholders no estarán disponibles."));
        }
    }



    @Override
    public void onDisable() {
        // Desactivar sistemas
        if (bleedingSystem != null) {
            bleedingSystem.shutdown();
        }
        if (radiationSystem != null) {
            radiationSystem.shutdown();
        }
        if (limbDamageSystem != null) {
            limbDamageSystem.shutdown();
        }
        if (hydrationSystem != null) {
            hydrationSystem.shutdown();
        }
        if (nutritionSystem != null) {
            nutritionSystem.shutdown();
        }
        if (staminaSystem != null) {
            staminaSystem.shutdown();
        }
        if (temperatureSystem != null) {
            temperatureSystem.shutdown();
        }
        if (freezingSystem != null) {
            freezingSystem.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        
        // Mensaje de cierre
        ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(PREFIX + " <red>El plugin ha sido desactivado!"));
        
        // Limpiar instancia
        instance = null;
    }
    
    /**
     * Inicializa todos los sistemas del plugin
     */
    private void initializeSystems() {
        // Cargar configuración de protección para nuevos jugadores
        loadNewPlayerProtectionConfig();
        
        // Inicializar sistemas de mecánicas
        bleedingSystem = new BleedingSystem(instance);
        radiationSystem = new RadiationSystem(instance);
        limbDamageSystem = new LimbDamageSystem(instance);
        hydrationSystem = new HydrationSystem(instance);
        nutritionSystem = new NutritionSystem(instance);
        staminaSystem = new StaminaSystem(instance);
        temperatureSystem = new TemperatureSystem(instance);
        freezingSystem = new FreezingSystem(instance);
        
        // Inicializar gestores
        //itemManager = new ItemManager(instance);
        databaseManager = new DatabaseManager(instance);
        userPreferencesManager = new com.darkbladedev.managers.UserPreferencesManager(instance);
        
        // Activar sistemas
        bleedingSystem.initialize();
        radiationSystem.initialize();
        limbDamageSystem.initialize();
        hydrationSystem.initialize();
        nutritionSystem.initialize();
        staminaSystem.initialize();
        temperatureSystem.initialize();
        freezingSystem.initialize();
        databaseManager.initialize();
    }
    
    /**
     * Carga la configuración de protección para nuevos jugadores
     */
    private void loadNewPlayerProtectionConfig() {
        // Cargar valores desde la configuración
        newPlayerProtectionEnabled = getConfig().getBoolean("new_player_protection.enabled", true);
        newPlayerProtectionDuration = getConfig().getLong("new_player_protection.duration", 60);
        
        // Inicializar mapa de sistemas protegidos
        protectedSystems = new java.util.HashMap<>();
        protectedSystems.put("temperature", getConfig().getBoolean("new_player_protection.protected_systems.temperature", true));
        protectedSystems.put("bleeding", getConfig().getBoolean("new_player_protection.protected_systems.bleeding", true));
        protectedSystems.put("hydration", getConfig().getBoolean("new_player_protection.protected_systems.hydration", true));
        protectedSystems.put("limb_damage", getConfig().getBoolean("new_player_protection.protected_systems.limb_damage", true));
        protectedSystems.put("radiation", getConfig().getBoolean("new_player_protection.protected_systems.radiation", true));
        protectedSystems.put("stamina", getConfig().getBoolean("new_player_protection.protected_systems.stamina", true));
        protectedSystems.put("nutrition", getConfig().getBoolean("new_player_protection.protected_systems.nutrition", true));
    }
    
    /**
     * Registra todos los eventos del plugin
     */
    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerEvents(instance), instance);
    }
    
    /**
     * Registra todos los comandos del plugin
     */
    private void registerCommands() {
        SavageCommand commandManager = new SavageCommand(instance);
        
        // Usar el método registerCommand de JavaPlugin para Paper plugins
        this.getServer().getCommandMap().register("savage", new org.bukkit.command.Command("savage") {
            {
                this.setDescription("Comando principal del plugin Savage Frontier");
                this.setUsage("/savage <subcomando>");
                this.setAliases(List.of("sf", "savage-frontier"));
            }
            
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                return commandManager.onCommand(sender, this, commandLabel, args);
            }
            
            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                return commandManager.onTabComplete(sender, this, alias, args);
            }
        });
    }
    
    /**
     * Obtiene la instancia del plugin
     * @return Instancia del plugin
     */
    public static SavageFrontierMain getInstance() {
        return instance;
    }
    
    /**
     * Obtiene el sistema de sangrado
     * @return Sistema de sangrado
     */
    public BleedingSystem getBleedingSystem() {
        return bleedingSystem;
    }
    
    /**
     * Obtiene el sistema de radiación
     * @return Sistema de radiación
     */
    public RadiationSystem getRadiationSystem() {
        return radiationSystem;
    }
    
    /**
     * Obtiene el sistema de daño por extremidades
     * @return Sistema de daño por extremidades
     */
    public LimbDamageSystem getLimbDamageSystem() {
        return limbDamageSystem;
    }
    
    /**
     * Obtiene el sistema de hidratación
     * @return Sistema de hidratación
     */
    public HydrationSystem getHydrationSystem() {
        return hydrationSystem;
    }
    
    /**
     * Obtiene el sistema de nutrición
     * @return Sistema de nutrición
     */
    public NutritionSystem getNutritionSystem() {
        return nutritionSystem;
    }
    
    /**
     * Obtiene el gestor de base de datos
     * @return Gestor de base de datos
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    /**
     * Obtiene el sistema de estamina
     * @return Sistema de estamina
     */
    public StaminaSystem getStaminaSystem() {
        return staminaSystem;
    }
    
    /**
     * Obtiene el sistema de temperatura
     * @return Sistema de temperatura
     */
    public TemperatureSystem getTemperatureSystem() {
        return temperatureSystem;
    }
    
    /**
     * Obtiene el sistema de congelación
     * @return Sistema de congelación
     */
    public FreezingSystem getFreezingSystem() {
        return freezingSystem;
    }
    
    /**
     * Obtiene el gestor de preferencias de usuario
     * @return Gestor de preferencias de usuario
     */
    public com.darkbladedev.managers.UserPreferencesManager getUserPreferencesManager() {
        return userPreferencesManager;
    }
    
    /**
     * Verifica si un jugador está protegido contra un sistema específico
     * @param player Jugador a verificar
     * @param systemName Nombre del sistema (temperature, bleeding, etc.)
     * @return true si el jugador está protegido, false en caso contrario
     */
    public boolean isPlayerProtectedFromSystem(org.bukkit.entity.Player player, String systemName) {
        // Si la protección está desactivada globalmente, no hay protección
        if (!newPlayerProtectionEnabled) {
            return false;
        }
        
        // Si el sistema específico no está en la lista de protección, no hay protección
        if (!protectedSystems.containsKey(systemName) || !protectedSystems.get(systemName)) {
            return false;
        }
        
        // Verificar si el jugador tiene la protección activada en sus preferencias
        if (!userPreferencesManager.hasNewPlayerProtection(player)) {
            return false;
        }
        
        // Verificar si el jugador es nuevo (basado en su primera vez de juego)
        long firstPlayedMs = player.getFirstPlayed();
        if (firstPlayedMs == 0) {
            return true; // Si es la primera vez que juega, está protegido
        }
        
        // Calcular si el tiempo de protección ha expirado
        long currentTimeMs = System.currentTimeMillis();
        long protectionDurationMs = newPlayerProtectionDuration * 60 * 1000; // Convertir minutos a milisegundos
        
        return (currentTimeMs - firstPlayedMs) <= protectionDurationMs;
    }
    
    /**
     * Recarga todos los sistemas del plugin
     * Este método es llamado por el comando /savage reload
     */
    public void reloadSystems() {
        // Guardar datos actuales antes de recargar
        if (databaseManager != null) {
            databaseManager.saveAllPlayerData();
        }
        
        // Recargar configuración
        reloadConfig();
        
        // Recargar configuración de protección para nuevos jugadores
        loadNewPlayerProtectionConfig();
        
        // Reinicializar sistemas
        if (bleedingSystem != null) {
            bleedingSystem.shutdown();
            bleedingSystem.initialize();
        }
        
        if (radiationSystem != null) {
            radiationSystem.shutdown();
            radiationSystem.initialize();
        }
        
        if (limbDamageSystem != null) {
            limbDamageSystem.shutdown();
            limbDamageSystem.initialize();
        }
        
        if (hydrationSystem != null) {
            hydrationSystem.shutdown();
            hydrationSystem.initialize();
        }
        
        if (nutritionSystem != null) {
            nutritionSystem.shutdown();
            nutritionSystem.initialize();
        }
        
        if (staminaSystem != null) {
            staminaSystem.shutdown();
            staminaSystem.initialize();
        }
        
        if (temperatureSystem != null) {
            temperatureSystem.shutdown();
            temperatureSystem.initialize();
        }
        
        if (freezingSystem != null) {
            freezingSystem.shutdown();
            freezingSystem.initialize();
        }
        
        // Reinicializar base de datos si es necesario
        if (databaseManager != null) {
            databaseManager.shutdown();
            databaseManager.initialize();
        }
        
        // Mensaje de recarga
        ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(PREFIX + " <green>Todos los sistemas han sido recargados!"));
    }
}