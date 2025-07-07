package com.darkbladedev.managers;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.integrations.AuraSkillsIntegration;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.user.SkillsUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Clase que se encarga de combinar las actionbars de AuraSkills y Savage Frontier
 * cuando ambas están habilitadas y configuradas para mostrarse juntas
 */
public class ActionBarCombiner {
    
    private final SavageFrontierMain plugin;
    private final ActionBarDisplayManager savageActionBar;
    private final AuraSkillsIntegration auraSkillsIntegration;
    private final MiniMessage miniMessage;
    private BukkitRunnable actionBarPauseTask;
    private AuraSkillsApi auraSkillsApi = AuraSkillsApi.get();
    
    public ActionBarCombiner(SavageFrontierMain plugin, ActionBarDisplayManager savageActionBar, AuraSkillsIntegration auraSkillsIntegration) {
        this.plugin = plugin;
        this.savageActionBar = savageActionBar;
        this.auraSkillsIntegration = auraSkillsIntegration;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public void initialize() {
        this.actionBarPauseTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    SkillsUser user = auraSkillsApi.getUser(player.getUniqueId());
                    if (user != null) {
                        user.pauseActionBar(1, TimeUnit.SECONDS);
                        sendCombinedActionBar(player);
                    }
                }
            }
        };
        this.actionBarPauseTask.runTaskTimer(plugin, 0, 15);
    }
    
    /**
     * Combina las actionbars de AuraSkills y Savage Frontier
     * @param player El jugador al que enviar la actionbar combinada
     */
    public void sendCombinedActionBar(Player player) {
        if (!shouldCombineActionBars(player)) {
            return;
        }
        
        String savageContent = getSavageActionBarContent(player);
        String auraSkillsContent = getAuraSkillsActionBarContent(player);
        
        if (savageContent.isEmpty() && auraSkillsContent.isEmpty()) {
            return;
        }
        
        String combinedContent = buildCombinedContent(savageContent, auraSkillsContent);
        
        if (!combinedContent.isEmpty()) {
            Component component = miniMessage.deserialize(combinedContent);
            // Usamos sendActionBar directamente para evitar que otros plugins interfieran
            player.sendActionBar(component);
            
            // Cancelamos cualquier tarea de actionbar de AuraSkills para este tick
            // Esto evita el parpadeo causado por ambos plugins enviando actionbars
            try {
                if (auraSkillsIntegration.isAuraSkillsEnabled()) {
                    // Marcamos que ya hemos mostrado la actionbar para este jugador
                    // para que AuraSkills no intente mostrar la suya
                    auraSkillsIntegration.markActionBarShown(player);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error al coordinar actionbar con AuraSkills: " + e.getMessage());
            }
        }
    }
    
    /**
     * Verifica si se deben combinar las actionbars para este jugador
     * @param player El jugador a verificar
     * @return true si se deben combinar las actionbars
     */
    private boolean shouldCombineActionBars(Player player) {
        // Solo combinar si:
        // 1. AuraSkills está habilitado
        // 2. La integración está configurada para combinar (no para alternar)
        // 3. El jugador tiene habilitada la actionbar de Savage Frontier
        return auraSkillsIntegration.isAuraSkillsEnabled() &&
               plugin.getConfig().getBoolean("actionbar.auraskills_integration.combine_mode", false) &&
               savageActionBar.isActionBarEnabled(player);
    }
    
    /**
     * Obtiene el contenido de la actionbar de Savage Frontier
     * @param player El jugador
     * @return El contenido de la actionbar como string
     */
    private String getSavageActionBarContent(Player player) {
        if (!savageActionBar.isActionBarEnabled(player)) {
            return "";
        }
        
        StringBuilder content = new StringBuilder();
        var enabledStats = savageActionBar.getEnabledStats(player);
        
        for (int i = 0; i < enabledStats.size(); i++) {
            ActionBarDisplayManager.StatType stat = enabledStats.get(i);
            
            switch (stat) {
                case HEALTH:
                    double health = player.getHealth();
                    double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
                    content.append("<red>❤ ").append(String.format("%.1f", health))
                           .append("/").append(String.format("%.1f", maxHealth));
                    break;
                case STAMINA:
                    String staminaInfo = savageActionBar.getStamina(player);
                    if (!staminaInfo.isEmpty()) {
                        content.append("<yellow>⚡ ").append(staminaInfo);
                    }
                    break;
                case NUTRIENTS:
                    String nutrientInfo = savageActionBar.getNutrients(player);
                    if (!nutrientInfo.isEmpty()) {
                        content.append("<green>🍎 ").append(nutrientInfo);
                    }
                    break;
                case WATER:
                    String waterInfo = savageActionBar.getWater(player);
                    if (!waterInfo.isEmpty()) {
                        content.append("<blue>💧 ").append(waterInfo);
                    }
                    break;
            }
            
            if (i < enabledStats.size() - 1 && !content.toString().isEmpty()) {
                content.append(" <gray>| ");
            }
        }
        
        return content.toString();
    }
    
    /**
     * Obtiene el contenido de la actionbar de AuraSkills
     * @param player El jugador
     * @return El contenido de la actionbar como string
     */
    private String getAuraSkillsActionBarContent(Player player) {
        if (!auraSkillsIntegration.isAuraSkillsEnabled() || 
            !auraSkillsIntegration.isAuraSkillsActionBarEnabled()) {
            return "";
        }
        
        try {
            SkillsUser user = auraSkillsApi.getUser(player.getUniqueId());
            
            if (user == null) {
                return "";
            }
            
            StringBuilder content = new StringBuilder();
            
            // Obtener información de mana si está disponible
            double mana = user.getMana();
            double maxMana = user.getMaxMana();
            
            if (maxMana > 0) {
                content.append("<blue>✦ ").append(String.format("%.1f", mana))
                       .append("/").append(String.format("%.1f", maxMana));
            }
            
            return content.toString();
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error al obtener información de AuraSkills para la actionbar: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Construye el contenido combinado de ambas actionbars
     * @param savageContent Contenido de Savage Frontier
     * @param auraSkillsContent Contenido de AuraSkills
     * @return El contenido combinado
     */
    private String buildCombinedContent(String savageContent, String auraSkillsContent) {
        StringBuilder combined = new StringBuilder();
        
        // No necesitamos pausar manualmente la actionbar de AuraSkills
        // ya que usamos markActionBarShown en el método sendCombinedActionBar
        // para evitar que AuraSkills muestre su actionbar después de la nuestra
        
        if (!savageContent.isEmpty()) {
            combined.append(savageContent);
        }
        
        if (!auraSkillsContent.isEmpty()) {
            if (!combined.toString().isEmpty()) {
                combined.append(" <gray>| ");
            }
            combined.append(auraSkillsContent);
        }
        
        return combined.toString();
    }
}