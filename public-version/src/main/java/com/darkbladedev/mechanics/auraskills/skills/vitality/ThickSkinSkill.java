package com.darkbladedev.mechanics.auraskills.skills.vitality;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Piel Gruesa
 * Reduce el daño por sangrado.
 */
public class ThickSkinSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Piel Gruesa";
    }

    @Override
    public String getDescription() {
        return "Reduce el daño por sangrado.";
    }

    @Override
    public int getRequiredLevel() {
        return 10;
    }

    @Override
    public void apply(Player player) {
        // La lógica real debe integrarse en el sistema de sangrado
    }
}
