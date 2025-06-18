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
    public static boolean hasExecutableItems = false;
    public static boolean hasItemsAdder = false;
    public final String PREFIX = "<gradient:#20f335:#4dd8e1>Savage Frontier</gradient>";

    
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