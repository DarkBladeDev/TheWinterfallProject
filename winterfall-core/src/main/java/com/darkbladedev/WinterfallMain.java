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
import com.darkbladedev.mechanics.FreezingSystem;
import com.darkbladedev.items.ItemManager;
import com.ssomar.score.SCore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;

import java.util.List;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;

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
    private FreezingSystem freezingSystem;
    private DatabaseManager databaseManager;
    private ItemManager itemManager;
    private PlaceholderAPIExpansion placeholders;
    public static boolean hasExecutableItems = false;
    public static boolean hasItemsAdder = false;
    public static final String NAME = "Winterfall";
    public static final String COMMAND_NAME = "winterfall";
    public final String PREFIX = "<gradient:#ffffff:#63d0ff>Winterfall</gradient>";

    
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
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize("<green>[Winterfall] PlaceholderAPI detectado y placeholders registrados"));
        } else {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>[Winterfall] PlaceholderAPI no detectado. Los placeholders no estarán disponibles."));
        }


        // Inicializar EI-API
        Plugin executableItems = Bukkit.getPluginManager().getPlugin("ExecutableItems");
        if(executableItems != null && executableItems.isEnabled()) {
            SCore.plugin.getServer().getLogger().info("["+NAME+"] ExecutableItems hooked !");
            hasExecutableItems = true;
            itemManager = new ItemManager(this);
        }
        
        // Inicializar ItemsAdder
        Plugin itemsAdder = Bukkit.getPluginManager().getPlugin("ItemsAdder");
        if(itemsAdder != null && itemsAdder.isEnabled()) {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize("<green>[Winterfall] ItemsAdder detectado y HUDs registrados"));
            hasItemsAdder = true;
        } else {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>[Winterfall] ItemsAdder no detectado. Los HUDs no estarán disponibles."));
        }
        
        // Mensaje de inicio
        ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize("<green>[Winterfall] El plugin ha sido activado!"));
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
        if (freezingSystem != null) {
            freezingSystem.shutdown();
        }
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        
        // Mensaje de cierre
        ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize("<red>[Winterfall] El plugin ha sido desactivado! La nevada ha cesado temporalmente..."));
        
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
        freezingSystem = new FreezingSystem(this);
        
        // Inicializar gestores
        //itemManager = new ItemManager(this);
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
        freezingSystem.initialize();
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
        
        // Usar el método registerCommand de JavaPlugin para Paper plugins
        this.getServer().getCommandMap().register(COMMAND_NAME, new org.bukkit.command.Command(COMMAND_NAME) {
            {
                this.setDescription("Comando principal del plugin " + NAME);
                this.setUsage("/" + COMMAND_NAME + " <subcomando>");
                this.setAliases(List.of("wf"));
            }
            
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                return winterfallCommand.onCommand(sender, this, commandLabel, args);
            }
            
            @Override
            public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                return winterfallCommand.onTabComplete(sender, this, alias, args);
            }
        });
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
}