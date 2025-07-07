package com.darkbladedev.mechanics.auraskills.skills.nutrition;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Impulso Metabólico
 * Recupera estamina más rápido después de comer.
 */
public class MetabolicBoostSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Impulso Metabólico";
    }

    @Override
    public String getDescription() {
        return "Recupera estamina más rápido después de comer.";
    }

    @Override
    public int getRequiredLevel() {
        return 15;
    }

    @Override
    public void apply(Player player) {
        // Integrar en el sistema de nutrición y estamina
    }
}
