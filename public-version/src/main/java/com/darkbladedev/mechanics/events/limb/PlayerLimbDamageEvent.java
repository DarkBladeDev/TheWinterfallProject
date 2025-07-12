package com.darkbladedev.mechanics.events.limb;

import com.darkbladedev.mechanics.LimbDamageSystem.LimbType;
import com.darkbladedev.mechanics.LimbDamageSystem.DamageState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;

public class PlayerLimbDamageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final LimbType limb;
    private final DamageState previousState;
    private DamageState newState;
    private boolean cancelled;

    public PlayerLimbDamageEvent(Player player, LimbType limb, DamageState previousState, DamageState newState) {
        this.player = player;
        this.limb = limb;
        this.previousState = previousState;
        this.newState = newState;
        this.cancelled = false;
    }

    public Player getPlayer() { return player; }
    
    public LimbType getLimb() { return limb; }

    public DamageState getPreviousState() { return previousState; }

    public DamageState getNewState() { return newState; }

    public void setNewState(DamageState newState) { this.newState = newState; }


    @Override public boolean isCancelled() { return cancelled; }

    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }


    @Override public HandlerList getHandlers() { return HANDLERS; }

    public static HandlerList getHandlerList() { return HANDLERS; }
}
