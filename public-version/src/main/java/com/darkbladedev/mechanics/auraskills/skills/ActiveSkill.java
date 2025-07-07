package com.darkbladedev.mechanics.auraskills.skills;

import org.bukkit.entity.Player;

/**
 * Interfaz base para habilidades activas de cualquier stat.
 */
public interface ActiveSkill {
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
     * Ejecuta la habilidad activa para el jugador.
     */
    void activate(Player player);

    /**
     * Cooldown en segundos.
     */
    int getCooldown();
}
