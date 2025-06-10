package com.darkbladedev.events;

import com.darkbladedev.WinterfallMain;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Manejador de eventos para "El Eternauta"
 * Gestiona las interacciones de los jugadores con el mundo
 */
public class PlayerEvents implements Listener {

    private final WinterfallMain plugin;
    
    /**
     * Constructor del manejador de eventos
     * @param plugin Instancia del plugin principal
     */
    public PlayerEvents(WinterfallMain plugin) {
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
     * Maneja el evento de interacción del jugador
     * @param event Evento de interacción
     */
    //@EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Verificar si el jugador está usando un ítem personalizado
        if (item != null && item.hasItemMeta()) {
            @SuppressWarnings("unused")
            ItemMeta meta = item.getItemMeta();
            
            // Lanzallamas
            if (plugin.getItemManager().isCustomItem(item, "flamethrower")) {
                // Implementar lógica del lanzallamas
                if (event.getAction().name().contains("RIGHT")) {
                    // Efecto visual de fuego
                    player.getWorld().createExplosion(player.getLocation(), 0.0f, false, false, player);
                    
                    // Mensaje de uso
                    player.sendMessage(NamedTextColor.GOLD + "¡Has disparado tu lanzallamas!");
                    
                    // Evitar consumo del ítem
                    event.setCancelled(true);
                }
            }
            
            // Pistola eléctrica
            else if (plugin.getItemManager().isCustomItem(item, "electric_gun")) {
                // Implementar lógica de la pistola eléctrica
                if (event.getAction().name().contains("RIGHT")) {
                    // Efecto visual de electricidad
                    player.getWorld().strikeLightningEffect(player.getTargetBlock(null, 20).getLocation());
                    
                    // Mensaje de uso
                    player.sendMessage(NamedTextColor.AQUA + "¡Has disparado tu pistola eléctrica!");
                    
                    // Evitar consumo del ítem
                    event.setCancelled(true);
                }
            }
        }
    }
}