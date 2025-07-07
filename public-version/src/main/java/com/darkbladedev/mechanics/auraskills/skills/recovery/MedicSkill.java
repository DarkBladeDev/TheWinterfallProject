package com.darkbladedev.mechanics.auraskills.skills.recovery;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Médico
 * Mejora la efectividad de pociones y curaciones.
 */
public class MedicSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Médico";
    }

    @Override
    public String getDescription() {
        return "Mejora la efectividad de pociones y curaciones.";
    }

    @Override
    public int getRequiredLevel() {
        return 20;
    }

    @Override
    public void apply(Player player) {
        // Integrar en el sistema de curaciones y pociones
    }
}
