package com.darkbladedev.mechanics.auraskills.skills.recovery;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Curación Rápida
 * Reduce el tiempo de recuperación de debuffs.
 */
public class QuickHealerSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Curación Rápida";
    }

    @Override
    public String getDescription() {
        return "Reduce el tiempo de recuperación de debuffs.";
    }

    @Override
    public int getRequiredLevel() {
        return 10;
    }

    @Override
    public void apply(Player player) {
        // Integrar en el sistema de debuffs
    }
}
