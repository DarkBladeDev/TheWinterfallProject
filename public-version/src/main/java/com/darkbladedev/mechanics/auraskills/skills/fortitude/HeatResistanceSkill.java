package com.darkbladedev.mechanics.auraskills.skills.fortitude;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Resistencia al Calor
 * Reduce el daño por calor extremo.
 */
public class HeatResistanceSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Resistencia al Calor";
    }

    @Override
    public String getDescription() {
        return "Reduce el daño por calor extremo.";
    }

    @Override
    public int getRequiredLevel() {
        return 15;
    }

    @Override
    public void apply(Player player) {
        // Integrar en el sistema de temperatura
    }
}
