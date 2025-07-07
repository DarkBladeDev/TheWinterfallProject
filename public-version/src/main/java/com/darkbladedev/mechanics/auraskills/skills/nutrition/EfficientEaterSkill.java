package com.darkbladedev.mechanics.auraskills.skills.nutrition;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Comedor Eficiente
 * La comida recupera más nutrición.
 */
public class EfficientEaterSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Comedor Eficiente";
    }

    @Override
    public String getDescription() {
        return "La comida recupera más nutrición.";
    }

    @Override
    public int getRequiredLevel() {
        return 10;
    }

    @Override
    public void apply(Player player) {
        // Integrar en el sistema de nutrición
    }
}
