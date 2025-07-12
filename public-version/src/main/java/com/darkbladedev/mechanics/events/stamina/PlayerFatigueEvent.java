package com.darkbladedev.mechanics.events.stamina;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerFatigueEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private double fatigue;
    private boolean cancelled;

    public PlayerFatigueEvent(Player player, double fatigue) {
        this.player = player;
        this.fatigue = fatigue;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public double getFatigue() {
        return fatigue;
    }

    public double getFatiguePercentage() {
        return fatigue / 100;
    }

    public void setFatigue(double fatigue) {
        this.fatigue = fatigue;
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
}
