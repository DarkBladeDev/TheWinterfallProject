package com.darkbladedev.events;

import com.darkbladedev.WinterfallMain;
import com.darkbladedev.mobs.MobManager;
import com.darkbladedev.mechanics.LimbDamageSystem.LimbType;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
        
        // Mensaje de bienvenida temático
        player.sendMessage(ChatColor.GRAY + "----------------------------------------");
        player.sendMessage(ChatColor.AQUA + "Bienvenido a " + ChatColor.WHITE + "El Eternauta");
        player.sendMessage(ChatColor.YELLOW + "La nieve mortal ha comenzado a caer...");
        player.sendMessage(ChatColor.RED + "¡Encuentra un traje aislante para sobrevivir!");
        player.sendMessage(ChatColor.GRAY + "----------------------------------------");
    }
    
    /**
     * Maneja el evento de reaparición de un jugador
     * @param event Evento de reaparición
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Mensaje de reaparición temático
        player.sendMessage(ChatColor.RED + "Has muerto en el mundo post-apocalíptico de El Eternauta.");
        player.sendMessage(ChatColor.YELLOW + "La lucha contra los invasores continúa...");
        
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
     * Maneja el evento de daño entre entidades
     * @param event Evento de daño entre entidades
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Obtener atacante (considerando proyectiles)
        Entity damager = event.getDamager();
        if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Entity) {
            damager = (Entity) ((Projectile) damager).getShooter();
        }
        
        // Verificar si el atacante es un jugador
        if (damager instanceof Player) {
            Player player = (Player) damager;
            ItemStack item = player.getInventory().getItemInMainHand();
            
            // Verificar si el jugador está usando un arma especial
            if (plugin.getItemManager().isCustomItem(item, "flamethrower")) {
                // Efecto especial para el lanzallamas contra Gurbos
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    
                    if (plugin.getMobManager().isCustomMob(target) && 
                            plugin.getMobManager().getCustomMobType(target).equals(MobManager.MOB_GURBO)) {
                        // Daño adicional a los Gurbos
                        event.setDamage(event.getDamage() * 2.0);
                        target.setFireTicks(100); // 5 segundos de fuego
                        player.sendMessage(ChatColor.GOLD + "¡Tu lanzallamas es muy efectivo contra el Gurbo!");
                    }
                }
            } else if (plugin.getItemManager().isCustomItem(item, "electric_gun")) {
                // Efecto especial para la pistola eléctrica contra Manos
                if (event.getEntity() instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) event.getEntity();
                    
                    if (plugin.getMobManager().isCustomMob(target) && 
                            plugin.getMobManager().getCustomMobType(target).equals(MobManager.MOB_MANO)) {
                        // Daño adicional a las Manos
                        event.setDamage(event.getDamage() * 2.0);
                        player.sendMessage(ChatColor.AQUA + "¡Tu pistola eléctrica es muy efectiva contra la Mano!");
                    }
                }
            }
        }
        
        // Verificar si el atacante es un mob personalizado
        if (damager instanceof LivingEntity && plugin.getMobManager().isCustomMob((LivingEntity) damager)) {
            String mobType = plugin.getMobManager().getCustomMobType((LivingEntity) damager);
            
            // Efectos especiales según el tipo de mob
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                
                switch (mobType) {
                    case MobManager.MOB_MANO:
                        // Las Manos causan sangrado leve y daño en brazos
                        plugin.getBleedingSystem().applyBleeding(player, 1, 15);
                        // Daño en brazos aleatorio
                        if (Math.random() < 0.7) { // 70% de probabilidad
                            LimbType targetLimb = Math.random() < 0.5 ? LimbType.LEFT_ARM : LimbType.RIGHT_ARM;
                            plugin.getLimbDamageSystem().applyDamageToLimb(player, targetLimb, event.getDamage());
                            player.sendMessage(ChatColor.RED + "¡La Mano ha dañado tu " + targetLimb.getDisplayName() + "!");
                        }
                        break;
                    case MobManager.MOB_CASCARUDO:
                        // Los Cascarudos causan sangrado moderado y daño en piernas
                        plugin.getBleedingSystem().applyBleeding(player, 2, 20);
                        // Daño en piernas aleatorio
                        if (Math.random() < 0.6) { // 60% de probabilidad
                            LimbType targetLimb = Math.random() < 0.5 ? LimbType.LEFT_LEG : LimbType.RIGHT_LEG;
                            plugin.getLimbDamageSystem().applyDamageToLimb(player, targetLimb, event.getDamage() * 1.5);
                            player.sendMessage(ChatColor.RED + "¡El Cascarudo ha dañado tu " + targetLimb.getDisplayName() + "!");
                        }
                        break;
                    case MobManager.MOB_GURBO:
                        // Los Gurbos causan sangrado grave y daño en múltiples extremidades
                        plugin.getBleedingSystem().applyBleeding(player, 3, 30);
                        // Daño en múltiples extremidades
                        if (Math.random() < 0.8) { // 80% de probabilidad
                            // Seleccionar 2 extremidades aleatorias para dañar
                            LimbType[] limbs = LimbType.values();
                            LimbType firstLimb = limbs[(int)(Math.random() * limbs.length)];
                            LimbType secondLimb;
                            do {
                                secondLimb = limbs[(int)(Math.random() * limbs.length)];
                            } while (secondLimb == firstLimb);
                            
                            plugin.getLimbDamageSystem().applyDamageToLimb(player, firstLimb, event.getDamage() * 2.0);
                            plugin.getLimbDamageSystem().applyDamageToLimb(player, secondLimb, event.getDamage() * 1.5);
                            player.sendMessage(ChatColor.DARK_RED + "¡El Gurbo ha dañado gravemente tus extremidades!");
                        }
                        break;
                }
            }
        }
    }
    
    /**
     * Maneja el evento de muerte de entidades
     * @param event Evento de muerte
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        
        // Verificar si la entidad es un mob personalizado
        if (plugin.getMobManager().isCustomMob(entity)) {
            String mobType = plugin.getMobManager().getCustomMobType(entity);
            
            // Drops especiales según el tipo de mob
            switch (mobType) {
                case MobManager.MOB_MANO:
                    // Las Manos pueden soltar componentes para la pistola eléctrica
                    if (Math.random() < 0.3) { // 30% de probabilidad
                        event.getDrops().add(new ItemStack(Material.REDSTONE, 2));
                    }
                    break;
                case MobManager.MOB_CASCARUDO:
                    // Los Cascarudos pueden soltar componentes para el traje aislante
                    if (Math.random() < 0.4) { // 40% de probabilidad
                        event.getDrops().add(new ItemStack(Material.WHITE_WOOL, 3));
                    }
                    break;
                case MobManager.MOB_GURBO:
                    // Los Gurbos pueden soltar componentes para el lanzallamas
                    if (Math.random() < 0.5) { // 50% de probabilidad
                        event.getDrops().add(new ItemStack(Material.BLAZE_POWDER, 2));
                    }
                    break;
            }
            
            // Experiencia adicional por matar mobs alienígenas
            event.setDroppedExp(event.getDroppedExp() * 2);
        }
    }
    
    /**
     * Maneja el evento de interacción del jugador
     * @param event Evento de interacción
     */
    @EventHandler
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
                    player.sendMessage(ChatColor.GOLD + "¡Has disparado tu lanzallamas!");
                    
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
                    player.sendMessage(ChatColor.AQUA + "¡Has disparado tu pistola eléctrica!");
                    
                    // Evitar consumo del ítem
                    event.setCancelled(true);
                }
            }
        }
    }
}