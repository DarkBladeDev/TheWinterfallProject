package com.darkbladedev.mechanics.events.stamina;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerStaminaRegenEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private double stamina;
    private double staminaRegen;
    private long sprintDuration;
    private boolean cancelled;

    public PlayerStaminaRegenEvent(Player player, double stamina, double staminaRegen, long sprintDuration) {
        this.player = player;
        this.stamina = stamina;
        this.staminaRegen = staminaRegen;
        this.sprintDuration = sprintDuration;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public double getStamina() {
        return stamina;
    }

    public double getStaminaRegenPerSecond() {
        return staminaRegen;
    }

    public void setStaminaRegenPerSecond(double staminaRegen) {
        this.staminaRegen = staminaRegen;
    }
    public double getStaminaPercentage() {
        return stamina / 100;
    }

    public void setStamina(double stamina) {
        this.stamina = stamina;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public long getSprintDuration() {
        return sprintDuration;
    }

}
