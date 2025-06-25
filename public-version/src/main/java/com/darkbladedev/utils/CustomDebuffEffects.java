package com.darkbladedev.utils;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CustomDebuffEffects implements Listener {

    private final Set<UUID> weakenedPlayers = new HashSet<>();
    private final Set<UUID> miningFatiguePlayers = new HashSet<>();

    private final JavaPlugin plugin;

    public CustomDebuffEffects(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Aplica debilidad personalizada (menos daño en ataques físicos)
     */
    public void applyWeakness(Player player) {
        weakenedPlayers.add(player.getUniqueId());
        player.sendActionBar(MiniMessage.miniMessage().deserialize("<gray><i>Te sientes débil..."));
    }

    public void removeWeakness(Player player) {
        weakenedPlayers.remove(player.getUniqueId());
    }

    /**
     * Aplica fatiga personalizada (romper bloques más lento)
     */
    public void applyMiningFatigue(Player player) {
        miningFatiguePlayers.add(player.getUniqueId());
        player.sendActionBar(MiniMessage.miniMessage().deserialize("<gray><i>Te cuesta trabajo usar herramientas..."));
    }

    public void removeMiningFatigue(Player player) {
        miningFatiguePlayers.remove(player.getUniqueId());
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!weakenedPlayers.contains(player.getUniqueId())) return;

        // Reducimos el daño infligido en un 40%
        double originalDamage = event.getDamage();
        event.setDamage(originalDamage * 0.6);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!miningFatiguePlayers.contains(player.getUniqueId())) return;

        // Simula fatiga aplicando un retraso
        Material type = event.getBlock().getType();
        long delay = 10L; // ticks de retraso (0.5 segundos)

        event.setCancelled(true);
        player.sendActionBar(MiniMessage.miniMessage().deserialize("<gray><i>Rompiendo lentamente..."));
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.getTargetBlockExact(5) != null && player.getTargetBlockExact(5).getType() == type) {
                (player).breakBlock(event.getBlock());
            }
        }, delay);
    }

    // Por si necesitas reestablecer atributos al reconectar
    //@EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        removeWeakness(player);
        removeMiningFatigue(player);
    }

    public boolean hasWeakness(Player player) {
        return weakenedPlayers.contains(player.getUniqueId());
    }

    public boolean hasMiningFatigue(Player player) {
        return miningFatiguePlayers.contains(player.getUniqueId());
    }
} 
