package com.darkbladedev.mechanics.auraskills.skills.endurance;

import com.darkbladedev.mechanics.auraskills.skills.PassiveSkill;
import org.bukkit.entity.Player;

/**
 * Habilidad pasiva: Segundo Aliento
 * Recupera una pequeña cantidad de estamina al recibir daño.
 */
public class SecondWindSkill implements PassiveSkill {
    @Override
    public String getName() {
        return "Segundo Aliento";
    }

    @Override
    public String getDescription() {
        return "Recupera estamina al recibir daño.";
    }

    @Override
    public int getRequiredLevel() {
        return 15;
    }

    @Override
    public void apply(Player player) {
        // La lógica real debe llamarse desde un listener de daño
        // Aquí solo se define la interfaz
    }
}
