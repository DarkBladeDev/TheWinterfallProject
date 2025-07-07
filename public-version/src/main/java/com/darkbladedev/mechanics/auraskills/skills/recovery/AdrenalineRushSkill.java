package com.darkbladedev.mechanics.auraskills.skills.recovery;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Subidón de Adrenalina
 * Aumenta temporalmente la velocidad al recibir daño.
 */
public class AdrenalineRushSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Subidón de Adrenalina";
    }

    @Override
    public String getDescription() {
        return "Aumenta temporalmente la velocidad al recibir daño.";
    }

    @Override
    public int getRequiredLevel() {
        return 15;
    }

    @Override
    public void apply(Player player) {
        // Integrar en el sistema de daño y efectos
    }
}
