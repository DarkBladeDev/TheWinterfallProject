package com.darkbladedev.mechanics.auraskills.events;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.mechanics.auraskills.traits.CustomTraits;

import dev.aurelium.auraskills.api.event.skill.SkillsLoadEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Manejador de eventos de AuraSkills.
 * Se encarga de inicializar componentes cuando AuraSkills ha terminado de cargar.
 */
public class AuraSkillsEventHandler implements Listener {

    private final SavageFrontierMain plugin;
    private final CustomTraits customTraits;
    
    /**
     * Constructor del manejador de eventos.
     * @param plugin Instancia principal del plugin
     * @param customTraits Instancia de CustomTraits para inicializar
     */
    public AuraSkillsEventHandler(SavageFrontierMain plugin, CustomTraits customTraits) {
        this.plugin = plugin;
        this.customTraits = customTraits;
    }
    
    /**
     * Maneja el evento SkillsLoadEvent que se dispara cuando AuraSkills ha terminado de cargar.
     * Este es el momento adecuado para inicializar componentes que dependen de AuraSkills.
     * @param event Evento de carga de AuraSkills
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onSkillsLoad(SkillsLoadEvent event) {
        try {
            // Inicializar los traits personalizados
            if (customTraits != null && !customTraits.isInitialized()) {
                customTraits.initialize();
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.PREFIX + " <green>Traits y stats personalizados inicializados correctamente mediante SkillsLoadEvent"
                ));
            }
            
            // Aquí se pueden inicializar otros componentes que dependan de AuraSkills
            // Por ejemplo, actualizar modificadores de jugadores conectados
            
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.PREFIX + " <red>Error al inicializar componentes en SkillsLoadEvent: " + e.getMessage()
            ));
            e.printStackTrace();
        }
    }
}