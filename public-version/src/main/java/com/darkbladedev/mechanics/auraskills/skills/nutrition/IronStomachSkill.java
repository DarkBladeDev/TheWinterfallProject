package com.darkbladedev.mechanics.auraskills.skills.nutrition;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Estómago de Hierro
 * Reduce los efectos negativos de comida podrida o mala.
 */
public class IronStomachSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Estómago de Hierro";
    }

    @Override
    public String getDescription() {
        return "Reduce los efectos negativos de comida podrida o mala.";
    }

    @Override
    public int getRequiredLevel() {
        return 20;
    }

    @Override
    public void apply(Player player) {
        // Integrar en el sistema de nutrición y efectos
    }
}
