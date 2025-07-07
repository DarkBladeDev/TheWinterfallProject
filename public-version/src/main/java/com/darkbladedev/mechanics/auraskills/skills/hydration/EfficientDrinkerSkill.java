package com.darkbladedev.mechanics.auraskills.skills.hydration;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Bebedor Eficiente
 * El agua recupera más hidratación.
 */
public class EfficientDrinkerSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Bebedor Eficiente";
    }

    @Override
    public String getDescription() {
        return "El agua recupera más hidratación.";
    }

    @Override
    public int getRequiredLevel() {
        return 10;
    }

    @Override
    public void apply(Player player) {
        // Integrar en el sistema de hidratación
    }
}
