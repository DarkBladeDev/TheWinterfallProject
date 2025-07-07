package com.darkbladedev.mechanics.auraskills.skills.hydration;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Caminante del Desierto
 * Reduce el consumo de agua en biomas cálidos.
 */
public class DesertWalkerSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Caminante del Desierto";
    }

    @Override
    public String getDescription() {
        return "Reduce el consumo de agua en biomas cálidos.";
    }

    @Override
    public int getRequiredLevel() {
        return 15;
    }

    @Override
    public void apply(Player player) {
        // Integrar en el sistema de hidratación y biomas
    }
}
