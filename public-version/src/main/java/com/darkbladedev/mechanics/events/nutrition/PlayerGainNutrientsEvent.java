package com.darkbladedev.mechanics.events.nutrition;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import com.darkbladedev.mechanics.NutritionSystem.NutrientType;

public class PlayerGainNutrientsEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final NutrientType nutrient;
    private final double amount;

    public PlayerGainNutrientsEvent(Player player, NutrientType nutrient, double amount) {
        super(true);
        this.player = player;
        this.nutrient = nutrient;
        this.amount = amount;
    }
    
    public Player getPlayer() {
        return player;
    }

    public NutrientType getNutrient() {
        return nutrient;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }


    public double getAmount() {
        return amount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    


}
