package com.darkbladedev.mechanics.auraskills.skills;

import org.bukkit.entity.Player;

/**
 * Interfaz base para habilidades pasivas de cualquier stat.
 */
public interface PassiveSkill {
    /**
     * Nombre de la habilidad.
     */
    String getName();

    /**
     * Descripción de la habilidad.
     */
    String getDescription();

    /**
     * Nivel requerido de stat para desbloquear la habilidad.
     */
    int getRequiredLevel();

    /**
     * Aplica el efecto pasivo al jugador si corresponde.
     */
    void apply(Player player);
}
