package com.darkbladedev.events;

import com.darkbladedev.SavageFrontierMain;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.darkbladedev.CustomTypes.CustomEnchantments;

/**
 * Manejador de eventos
 * Gestiona las interacciones de los jugadores con el mundo
 */
public class PlayerEvents implements Listener {

    private final SavageFrontierMain plugin;
    
    /**
     * Constructor del manejador de eventos
     * @param plugin Instancia del plugin principal
     */
    public PlayerEvents(SavageFrontierMain plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Maneja el evento de unión de un jugador
     * @param event Evento de unión
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Cargar datos del jugador desde la base de datos
        plugin.getDatabaseManager().loadPlayerData(player);
        
    }
    
    /**
     * Maneja el evento de desconexión de un jugador
     * @param event Evento de desconexión
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Guardar datos del jugador en la base de datos
        plugin.getDatabaseManager().savePlayerData(player.getUniqueId());
    }
    
    /**
     * Maneja el evento de reaparición de un jugador
     * @param event Evento de reaparición
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Curar todas las extremidades al reaparecer
        if (plugin.getLimbDamageSystem().isActive()) {
            plugin.getLimbDamageSystem().healAllLimbs(player);
        }
    }
    
    /**
     * Maneja el evento de daño a entidades
     * @param event Evento de daño
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        // Procesar sangrado en caso de daño
        if (event.getEntity() instanceof LivingEntity) {
            plugin.getBleedingSystem().processDamageEvent(event);
        }
    }
    
    /**
     * Maneja el evento de daño entre entidades para aplicar efectos de encantamientos
     * @param event Evento de daño entre entidades
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Verificar si el atacante es un jugador
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        LivingEntity target = (LivingEntity) event.getEntity();
        
        // Verificar si el jugador tiene un arma en la mano
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon == null || !weapon.hasItemMeta()) {
            return;
        }
        
        ItemMeta meta = weapon.getItemMeta();
        
        // Verificar si el arma tiene el encantamiento de Congelación
        if (meta.hasEnchant(CustomEnchantments.getCongelationEnchantment())) {
            int level = meta.getEnchantLevel(CustomEnchantments.getCongelationEnchantment());
            
            // Aplicar efecto de congelación al objetivo
            plugin.getFreezingSystem().applyFreezing(target, level);
        }
    }
}