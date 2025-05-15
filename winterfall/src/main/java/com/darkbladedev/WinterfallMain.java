package com.darkbladedev;

import com.darkbladedev.commands.WinterfallCommand;
import com.darkbladedev.events.PlayerEvents;
import com.darkbladedev.mechanics.SnowfallSystem;
import com.darkbladedev.mechanics.BleedingSystem;
import com.darkbladedev.mechanics.RadiationSystem;
import com.darkbladedev.items.ItemManager;
import com.darkbladedev.mobs.MobManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private ItemManager itemManager;
    private MobManager mobManager;
    
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
        
        // Mensaje de inicio
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Winterfall] El plugin ha sido activado! La nevada mortal comienza...");
    }

    @Override
    public void onDisable() {
        // Desactivar sistemas
        if (snowfallSystem != null) {
            snowfallSystem.shutdown();
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
        
        // Inicializar gestores
        itemManager = new ItemManager(this);
        mobManager = new MobManager(this);
        
        // Activar sistemas
        snowfallSystem.initialize();
        bleedingSystem.initialize();
        radiationSystem.initialize();
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
        getCommand("winterfall").setExecutor(new WinterfallCommand(this));
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
}