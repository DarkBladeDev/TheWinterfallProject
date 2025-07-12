package com.darkbladedev.mechanics.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

// Evento base para habilidades
public abstract class CustomAbilityEvent extends Event {
    protected final Player player;

    public CustomAbilityEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}

// HardBonesEvent: Reduce daño a extremidades
class HardBonesEvent extends CustomAbilityEvent {
    private double damage;

    public HardBonesEvent(Player player, double damage) {
        super(player);
        this.damage = damage;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

// ThickSkinEvent: Reduce daño total recibido
class ThickSkinEvent extends CustomAbilityEvent {
    private double damage;

    public ThickSkinEvent(Player player, double damage) {
        super(player);
        this.damage = damage;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

// SecondWindEvent: Evento cuando el jugador cae a 0 de stamina
class SecondWindEvent extends CustomAbilityEvent {
    private boolean activated;

    public SecondWindEvent(Player player) {
        super(player);
        this.activated = false;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
