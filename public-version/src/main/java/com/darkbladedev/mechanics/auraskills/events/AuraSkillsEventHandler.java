package com.darkbladedev.mechanics.auraskills.events;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.mechanics.auraskills.traits.CustomTraits;

import org.bukkit.event.Listener;

/**
 * Manejador de eventos de AuraSkills.
 * Se encarga de inicializar componentes cuando AuraSkills ha terminado de cargar.
 */
public class AuraSkillsEventHandler implements Listener {

    @SuppressWarnings("unused")
    private final SavageFrontierMain plugin;
    @SuppressWarnings("unused")
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
    
}