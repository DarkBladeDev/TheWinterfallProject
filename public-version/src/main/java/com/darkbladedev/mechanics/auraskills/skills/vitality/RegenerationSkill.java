package com.darkbladedev.mechanics.auraskills.skills.vitality;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Regeneración
 * Recupera vida lentamente cuando la estamina está alta.
 */
public class RegenerationSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Regeneración";
    }

    @Override
    public String getDescription() {
        return "Recupera vida lentamente cuando la estamina está alta.";
    }

    @Override
    public int getRequiredLevel() {
        return 15;
    }

    @Override
    public void apply(Player player) {
        // La lógica real debe integrarse en el sistema de estamina o tick
    }
}
