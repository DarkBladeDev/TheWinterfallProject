package com.darkbladedev.mechanics.auraskills.skills.fortitude;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Escudo de Radiación
 * Reduce el daño por radiación.
 */
public class RadiationShieldSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Escudo de Radiación";
    }

    @Override
    public String getDescription() {
        return "Reduce el daño por radiación.";
    }

    @Override
    public int getRequiredLevel() {
        return 20;
    }

    @Override
    public void apply(Player player) {
        // Integrar en el sistema de radiación
    }
}
