package com.darkbladedev.mechanics.auraskills.skills.fortitude;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Resistencia al Frío
 * Reduce el daño por frío y congelación.
 */
public class ColdResistanceSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Resistencia al Frío";
    }

    @Override
    public String getDescription() {
        return "Reduce el daño por frío y congelación.";
    }

    @Override
    public int getRequiredLevel() {
        return 10;
    }

    @Override
    public void apply(Player player) {
        // Integrar en el sistema de temperatura/congelación
    }
}
