package com.darkbladedev.mechanics.auraskills.skills.hydration;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Recolector de Lluvia
 * Recupera hidratación lentamente bajo la lluvia.
 */
public class RainCollectorSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Recolector de Lluvia";
    }

    @Override
    public String getDescription() {
        return "Recupera hidratación lentamente bajo la lluvia.";
    }

    @Override
    public int getRequiredLevel() {
        return 20;
    }

    @Override
    public void apply(Player player) {
        // Integrar en el sistema de hidratación y clima
    }
}
