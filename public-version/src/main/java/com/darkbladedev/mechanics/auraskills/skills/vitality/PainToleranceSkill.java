package com.darkbladedev.mechanics.auraskills.skills.vitality;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Tolerancia al Dolor
 * Reduce la duración de debuffs por daño en extremidades.
 */
public class PainToleranceSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Tolerancia al Dolor";
    }

    @Override
    public String getDescription() {
        return "Reduce la duración de debuffs por daño en extremidades.";
    }

    @Override
    public int getRequiredLevel() {
        return 20;
    }

    @Override
    public void apply(Player player) {
        // La lógica real debe integrarse en el sistema de daño por extremidades
    }
}
