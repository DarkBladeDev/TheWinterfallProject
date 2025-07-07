package com.darkbladedev.mechanics.auraskills.skills.endurance;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Corredor de Fondo
 * Reduce el consumo de estamina al correr largas distancias.
 */
public class MarathonerSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Corredor de Fondo";
    }

    @Override
    public String getDescription() {
        return "Reduce el consumo de estamina al correr largas distancias.";
    }

    @Override
    public int getRequiredLevel() {
        return 20;
    }

    @Override
    public void apply(Player player) {
        // La lógica real debe integrarse en el sistema de estamina
        // Aquí solo se define la interfaz
    }
}
