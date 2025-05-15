package com.darkbladedev.mobs;

import com.darkbladedev.WinterfallMain;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Gestor de mobs personalizados para "El Eternauta"
 * Implementa los invasores alienígenas: Manos, Cascarudos y Gurbos
 */
public class MobManager {

    private final WinterfallMain plugin;
    private final Random random;
    private final Map<UUID, String> customMobs;
    
    // Tipos de mobs alienígenas
    public static final String MOB_MANO = "mano";
    public static final String MOB_CASCARUDO = "cascarudo";
    public static final String MOB_GURBO = "gurbo";
    
    /**
     * Constructor del gestor de mobs
     * @param plugin Instancia del plugin principal
     */
    public MobManager(WinterfallMain plugin) {
        this.plugin = plugin;
        this.random = new Random();
        this.customMobs = new HashMap<>();
        
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Winterfall] Sistema de mobs alienígenas inicializado");
    }
    
    /**
     * Crea una "Mano" (alienígena básico) en la ubicación especificada
     * @param location Ubicación donde crear la Mano
     * @return Entidad creada
     */
    public LivingEntity spawnMano(Location location) {
        // Usar Skeleton como base para la Mano
        Skeleton mano = (Skeleton) location.getWorld().spawnEntity(location, EntityType.SKELETON);
        
        // Configurar propiedades
        mano.setCustomName(ChatColor.AQUA + "Mano");
        mano.setCustomNameVisible(true);
        
        // Establecer atributos
        mano.getAttribute(Attribute.MAX_HEALTH).setBaseValue(16.0); // 8 corazones
        mano.setHealth(16.0);
        mano.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.3); // Más rápido que un zombie normal
        mano.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(4.0); // 2 corazones de daño
        
        // Efectos especiales
        mano.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
        
        // Metadata para identificación
        mano.setMetadata("custom_mob", new FixedMetadataValue(plugin, MOB_MANO));
        
        // Registrar en el mapa de mobs personalizados
        customMobs.put(mano.getUniqueId(), MOB_MANO);
        
        return mano;
    }
    
    /**
     * Crea un "Cascarudo" (alienígena blindado) en la ubicación especificada
     * @param location Ubicación donde crear el Cascarudo
     * @return Entidad creada
     */
    public LivingEntity spawnCascarudo(Location location) {
        // Usar Spider como base para el Cascarudo
        Spider cascarudo = (Spider) location.getWorld().spawnEntity(location, EntityType.SPIDER);
        
        // Configurar propiedades
        cascarudo.setCustomName(ChatColor.DARK_GREEN + "Cascarudo");
        cascarudo.setCustomNameVisible(true);
        
        // Establecer atributos
        cascarudo.getAttribute(Attribute.MAX_HEALTH).setBaseValue(30.0); // 15 corazones
        cascarudo.setHealth(30.0);
        cascarudo.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.25); // Más lento pero resistente
        cascarudo.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(6.0); // 3 corazones de daño
        cascarudo.getAttribute(Attribute.ARMOR).setBaseValue(10.0); // Alta resistencia
        
        // Efectos especiales
        cascarudo.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        
        // Metadata para identificación
        cascarudo.setMetadata("custom_mob", new FixedMetadataValue(plugin, MOB_CASCARUDO));
        
        // Registrar en el mapa de mobs personalizados
        customMobs.put(cascarudo.getUniqueId(), MOB_CASCARUDO);
        
        return cascarudo;
    }
    
    /**
     * Crea un "Gurbo" (alienígena gigante) en la ubicación especificada
     * @param location Ubicación donde crear el Gurbo
     * @return Entidad creada
     */
    @SuppressWarnings("deprecation")
    public LivingEntity spawnGurbo(Location location) {
        // Usar Zombie como base para el Gurbo
        Zombie gurbo = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        
        // Configurar propiedades
        gurbo.setCustomName(ChatColor.RED + "Gurbo");
        gurbo.setCustomNameVisible(true);
        gurbo.setBaby(false);
        
        // Establecer atributos
        gurbo.getAttribute(Attribute.MAX_HEALTH).setBaseValue(50.0); // 25 corazones
        gurbo.setHealth(50.0);
        gurbo.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.2); // Lento pero muy fuerte
        gurbo.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(10.0); // 5 corazones de daño
        gurbo.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(1.0); // Inmune a empujones
        
        // Efectos especiales
        gurbo.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false));
        
        // Metadata para identificación
        gurbo.setMetadata("custom_mob", new FixedMetadataValue(plugin, MOB_GURBO));
        
        // Registrar en el mapa de mobs personalizados
        customMobs.put(gurbo.getUniqueId(), MOB_GURBO);
        
        return gurbo;
    }
    
    /**
     * Verifica si una entidad es un mob personalizado
     * @param entity Entidad a verificar
     * @return true si es un mob personalizado, false en caso contrario
     */
    public boolean isCustomMob(LivingEntity entity) {
        return entity.hasMetadata("custom_mob") || customMobs.containsKey(entity.getUniqueId());
    }
    
    /**
     * Obtiene el tipo de mob personalizado
     * @param entity Entidad a verificar
     * @return Tipo de mob o null si no es un mob personalizado
     */
    public String getCustomMobType(LivingEntity entity) {
        if (entity.hasMetadata("custom_mob")) {
            return entity.getMetadata("custom_mob").get(0).asString();
        }
        
        return customMobs.get(entity.getUniqueId());
    }
    
    /**
     * Genera mobs alienígenas aleatoriamente en el mundo
     * @param world Mundo donde generar los mobs
     * @param amount Cantidad de mobs a generar
     */
    public void spawnRandomAliens(World world, int amount) {
        for (int i = 0; i < amount; i++) {
            // Seleccionar un jugador aleatorio como punto de referencia
            if (world.getPlayers().isEmpty()) {
                continue;
            }
            
            // Obtener ubicación aleatoria cerca de un jugador
            int playerIndex = random.nextInt(world.getPlayers().size());
            Location playerLoc = world.getPlayers().get(playerIndex).getLocation();
            
            // Generar ubicación aleatoria entre 20 y 50 bloques de distancia
            double distance = 20 + random.nextDouble() * 30;
            double angle = random.nextDouble() * 2 * Math.PI;
            
            double x = playerLoc.getX() + distance * Math.cos(angle);
            double z = playerLoc.getZ() + distance * Math.sin(angle);
            double y = world.getHighestBlockYAt((int) x, (int) z) + 1;
            
            Location spawnLoc = new Location(world, x, y, z);
            
            // Decidir qué tipo de alienígena generar
            int mobType = random.nextInt(10);
            
            if (mobType < 6) { // 60% de probabilidad
                spawnMano(spawnLoc);
            } else if (mobType < 9) { // 30% de probabilidad
                spawnCascarudo(spawnLoc);
            } else { // 10% de probabilidad
                spawnGurbo(spawnLoc);
            }
        }
    }
    
    /**
     * Elimina todos los mobs personalizados de un mundo
     * @param world Mundo donde eliminar los mobs
     */
    public void removeAllCustomMobs(World world) {
        world.getLivingEntities().forEach(entity -> {
            if (isCustomMob(entity)) {
                entity.remove();
                customMobs.remove(entity.getUniqueId());
            }
        });
    }
}