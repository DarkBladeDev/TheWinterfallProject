package com.darkbladedev.integrations;

import com.darkbladedev.SavageFrontierMain;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Clase para manejar la integración con el plugin AuraSkills
 * Coordina el uso de la actionbar entre ambos plugins
 */
public class AuraSkillsIntegration {
    private final SavageFrontierMain plugin;
    private boolean auraSkillsEnabled = false;
    private boolean auraSkillsActionBarEnabled = false;
    private AuraSkillsApi auraSkillsApi;
    private final Map<UUID, Boolean> playerAuraSkillsActionBarState = new HashMap<>();
    private boolean integrationEnabled = true;
    private boolean allowToggle = true;
    private String defaultPriority = "savage";
    
    // Mapa para rastrear jugadores que ya tienen una actionbar mostrada en este tick
    private final Map<UUID, Long> actionBarShownTimestamps = new HashMap<>();
    
    public AuraSkillsIntegration(SavageFrontierMain plugin) {
        this.plugin = plugin;
        loadConfiguration();
        checkAuraSkillsIntegration();
    }
    
    /**
     * Carga la configuración desde config.yml
     */
    private void loadConfiguration() {
        integrationEnabled = plugin.getConfig().getBoolean("actionbar.auraskills_integration.enabled", true);
        allowToggle = plugin.getConfig().getBoolean("actionbar.auraskills_integration.allow_toggle", true);
        defaultPriority = plugin.getConfig().getString("actionbar.auraskills_integration.default_priority", "savage");
    }
    
    /**
     * Verifica si AuraSkills está presente y disponible
     */
    private void checkAuraSkillsIntegration() {
        if (!integrationEnabled) {
            plugin.getLogger().info("Integración con AuraSkills deshabilitada en la configuración.");
            auraSkillsEnabled = false;
            return;
        }
        
        Plugin auraSkillsPlugin = Bukkit.getPluginManager().getPlugin("AuraSkills");
        
        if (auraSkillsPlugin != null && auraSkillsPlugin.isEnabled()) {
            try {
                auraSkillsApi = AuraSkillsApi.get();
                auraSkillsEnabled = true;
                checkAuraSkillsActionBarConfig();
                plugin.getLogger().info("Integración con AuraSkills habilitada.");
            } catch (Exception e) {
                plugin.getLogger().warning("AuraSkills detectado pero no se pudo inicializar la API: " + e.getMessage());
                auraSkillsEnabled = false;
            }
        } else {
            plugin.getLogger().info("AuraSkills no detectado. Funcionando en modo independiente.");
            auraSkillsEnabled = false;
        }
    }
    
    /**
     * Verifica la configuración de actionbar de AuraSkills
     */
    private void checkAuraSkillsActionBarConfig() {
        try {
            File auraSkillsConfigFile = new File(Bukkit.getPluginManager().getPlugin("AuraSkills").getDataFolder(), "config.yml");
            if (auraSkillsConfigFile.exists()) {
                YamlConfiguration auraConfig = 
                    YamlConfiguration.loadConfiguration(auraSkillsConfigFile);
                
                // Verificar si la actionbar de AuraSkills está habilitada
                auraSkillsActionBarEnabled = auraConfig.getBoolean("action_bar.enabled", true) && 
                                           auraConfig.getBoolean("action_bar.idle", true);
                
                if (auraSkillsActionBarEnabled) {
                    plugin.getLogger().info("AuraSkills tiene la actionbar habilitada. Se coordinará el uso.");
                } else {
                    plugin.getLogger().info("AuraSkills tiene la actionbar deshabilitada. Savage Frontier tendrá control total.");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error al leer la configuración de AuraSkills: " + e.getMessage());
            auraSkillsActionBarEnabled = false;
        }
    }
    
    /**
     * Verifica si AuraSkills está habilitado
     * @return true si AuraSkills está disponible
     */
    public boolean isAuraSkillsEnabled() {
        return auraSkillsEnabled;
    }
    
    /**
     * Verifica si la actionbar de AuraSkills está habilitada
     * @return true si AuraSkills tiene su actionbar habilitada
     */
    public boolean isAuraSkillsActionBarEnabled() {
        return auraSkillsActionBarEnabled;
    }
    
    /**
     * Determina si Savage Frontier debe mostrar su actionbar para un jugador específico
     * @param player El jugador a verificar
     * @return true si Savage Frontier debe mostrar su actionbar
     */
    public boolean shouldShowSavageActionBar(Player player) {
        if (!auraSkillsEnabled) {
            return true; // Si AuraSkills no está presente, siempre mostrar
        }
        
        if (!auraSkillsActionBarEnabled) {
            return true; // Si AuraSkills no usa actionbar, siempre mostrar
        }
        
        // Si AuraSkills está habilitado y usa actionbar, verificar preferencias del jugador
        return !playerAuraSkillsActionBarState.getOrDefault(player.getUniqueId(), true);
    }
    
    /**
     * Establece la preferencia del jugador para la actionbar de AuraSkills
     * @param player El jugador
     * @param enabled Si debe usar la actionbar de AuraSkills
     */
    public void setPlayerAuraSkillsActionBar(Player player, boolean enabled) {
        playerAuraSkillsActionBarState.put(player.getUniqueId(), enabled);
        
        // Si el jugador habilita AuraSkills actionbar, deshabilitar la de Savage Frontier
        if (enabled && auraSkillsEnabled) {
            plugin.getActionBarDisplayManager().setActionbarEnabled(player, false);
        } else {
            // Si deshabilita AuraSkills actionbar, habilitar la de Savage Frontier
            plugin.getActionBarDisplayManager().setActionbarEnabled(player, true);
        }
    }
    
    /**
     * Obtiene la API de AuraSkills si está disponible
     * @return La instancia de AuraSkillsApi o null si no está disponible
     */
    public AuraSkillsApi getAuraSkillsApi() {
        return auraSkillsApi;
    }
    
    /**
     * Verifica si un jugador tiene la actionbar de AuraSkills habilitada
     * @param player El jugador a verificar
     * @return true si el jugador prefiere usar AuraSkills actionbar
     */
    public boolean isPlayerUsingAuraSkillsActionBar(Player player) {
        boolean defaultValue = "auraskills".equalsIgnoreCase(defaultPriority) && auraSkillsActionBarEnabled;
        return playerAuraSkillsActionBarState.getOrDefault(player.getUniqueId(), defaultValue);
    }
    
    /**
     * Verifica si el toggle entre actionbars está permitido
     * @return true si el toggle está permitido
     */
    public boolean isToggleAllowed() {
        return allowToggle && auraSkillsEnabled;
    }
    
    /**
     * Recarga la integración con AuraSkills
     */
    public void reload() {
        loadConfiguration();
        checkAuraSkillsIntegration();
    }
    
    /**
     * Limpia los datos del jugador cuando se desconecta
     * @param player El jugador que se desconectó
     */
    public void onPlayerQuit(Player player) {
        playerAuraSkillsActionBarState.remove(player.getUniqueId());
        actionBarShownTimestamps.remove(player.getUniqueId());
    }
    
    /**
     * Marca que se ha mostrado una actionbar a un jugador en este tick
     * @param player El jugador al que se le ha mostrado la actionbar
     */
    public void markActionBarShown(Player player) {
        actionBarShownTimestamps.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    /**
     * Verifica si se ha mostrado una actionbar a un jugador recientemente
     * @param player El jugador a verificar
     * @return true si se ha mostrado una actionbar recientemente (en los últimos 100ms)
     */
    public boolean hasActionBarBeenShown(Player player) {
        Long timestamp = actionBarShownTimestamps.get(player.getUniqueId());
        if (timestamp == null) {
            return false;
        }
        
        // Consideramos que la actionbar ha sido mostrada si ha pasado menos de 100ms
        return System.currentTimeMillis() - timestamp < 100;
    }
}