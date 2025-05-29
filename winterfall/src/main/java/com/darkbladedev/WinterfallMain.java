package com.darkbladedev;

import com.darkbladedev.commands.WinterfallCommand;
import com.darkbladedev.database.DatabaseManager;
import com.darkbladedev.events.PlayerEvents;
import com.darkbladedev.integrations.PlaceholderAPIExpansion;
import com.darkbladedev.mechanics.SnowfallSystem;
import com.darkbladedev.mechanics.BleedingSystem;
import com.darkbladedev.mechanics.RadiationSystem;
import com.darkbladedev.mechanics.LimbDamageSystem;
import com.darkbladedev.mechanics.HydrationSystem;
import com.darkbladedev.mechanics.NutritionSystem;
import com.darkbladedev.mechanics.StaminaSystem;
import com.darkbladedev.mechanics.TemperatureSystem;
import com.darkbladedev.items.ItemManager;
import com.darkbladedev.mobs.MobManager;
import com.ssomar.score.SCore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Clase principal del plugin Winterfall que recrea el mundo de "El Eternauta"
 * con mecánicas, items y mobs realistas.
 */
public class WinterfallMain extends JavaPlugin {
    
    private static WinterfallMain instance;
    private SnowfallSystem snowfallSystem;
    private BleedingSystem bleedingSystem;
    private RadiationSystem radiationSystem;
    private LimbDamageSystem limbDamageSystem;
    private HydrationSystem hydrationSystem;
    private NutritionSystem nutritionSystem;
    private StaminaSystem staminaSystem;
    private TemperatureSystem temperatureSystem;
    private DatabaseManager databaseManager;
    private ItemManager itemManager;
    private MobManager mobManager;
    private PlaceholderAPIExpansion placeholders;
    public static boolean hasExecutableItems = false;
    public static final String NAME = "Winterfall";
    
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
            placeholders = new PlaceholderAPIExpansion(this);
            placeholders.register();
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Winterfall] PlaceholderAPI detectado y placeholders registrados");
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[Winterfall] PlaceholderAPI no detectado. Los placeholders no estarán disponibles.");
        }


        // INicializar EI-API
        Plugin executableItems = Bukkit.getPluginManager().getPlugin("ExecutableItems");
        if(executableItems != null && executableItems.isEnabled()) {
            SCore.plugin.getServer().getLogger().info("["+NAME+"] ExecutableItems hooked !");
            hasExecutableItems = true;
            itemManager = new ItemManager(this);
        }
        
        // Mensaje de inicio
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Winterfall] El plugin ha sido activado!");
    }

    @Override
    public void onDisable() {
        // Desactivar sistemas
        if (snowfallSystem != null) {
            snowfallSystem.shutdown();
        }
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
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        
        // Mensaje de cierre
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Winterfall] El plugin ha sido desactivado! La nevada ha cesado temporalmente...");
        
        // Limpiar instancia
        instance = null;
    }
    
    /**
     * Inicializa todos los sistemas del plugin
     */
    private void initializeSystems() {
        // Inicializar sistemas de mecánicas
        snowfallSystem = new SnowfallSystem(this);
        bleedingSystem = new BleedingSystem(this);
        radiationSystem = new RadiationSystem(this);
        limbDamageSystem = new LimbDamageSystem(this);
        hydrationSystem = new HydrationSystem(this);
        nutritionSystem = new NutritionSystem(this);
        staminaSystem = new StaminaSystem(this);
        temperatureSystem = new TemperatureSystem(this);
        
        // Inicializar gestores
        //itemManager = new ItemManager(this);
        mobManager = new MobManager(this);
        databaseManager = new DatabaseManager(this);
        
        // Activar sistemas
        snowfallSystem.initialize();
        bleedingSystem.initialize();
        radiationSystem.initialize();
        limbDamageSystem.initialize();
        hydrationSystem.initialize();
        nutritionSystem.initialize();
        staminaSystem.initialize();
        temperatureSystem.initialize();
        databaseManager.initialize();
    }
    
    /**
     * Registra todos los eventos del plugin
     */
    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerEvents(this), this);
    }
    
    /**
     * Registra todos los comandos del plugin
     */
    private void registerCommands() {
        WinterfallCommand winterfallCommand = new WinterfallCommand(this);
        getCommand("winterfall").setExecutor(winterfallCommand);
        getCommand("winterfall").setTabCompleter(winterfallCommand);
    }
    
    /**
     * Obtiene la instancia del plugin
     * @return Instancia del plugin
     */
    public static WinterfallMain getInstance() {
        return instance;
    }
    
    /**
     * Obtiene el sistema de nevada
     * @return Sistema de nevada
     */
    public SnowfallSystem getSnowfallSystem() {
        return snowfallSystem;
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
     * Obtiene el gestor de items
     * @return Gestor de items
     */
    public ItemManager getItemManager() {
        return itemManager;
    }
    
    /**
     * Obtiene el gestor de mobs
     * @return Gestor de mobs
     */
    public MobManager getMobManager() {
        return mobManager;
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
}