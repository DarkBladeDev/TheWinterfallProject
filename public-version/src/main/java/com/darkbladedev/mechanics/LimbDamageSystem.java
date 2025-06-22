package com.darkbladedev.mechanics;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.CustomTypes.CustomDamageTypes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.bukkit.attribute.Attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sistema de daño por extremidades para "Savage Frontier"
 * Gestiona el estado de las diferentes partes del cuerpo de los jugadores
 */
public class LimbDamageSystem implements Listener {

    private final SavageFrontierMain plugin;
    private final Map<UUID, Map<LimbType, Integer>> limbDamage;
    private boolean isActive;
    
    // Tipos de extremidades
    public enum LimbType {
        HEAD("Cabeza"),
        LEFT_ARM("Brazo Izquierdo"),
        RIGHT_ARM("Brazo Derecho"),
        LEFT_LEG("Pierna Izquierda"),
        RIGHT_LEG("Pierna Derecha");
        
        private final String displayName;
        
        LimbType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Estados de daño
    public enum DamageState {
        HEALTHY(0, "<green>" + "Sano"),
        DAMAGED(1, "<yellow>" + "Herido"),
        CRITICAL(2, "<red>" + "Crítico"),
        BROKEN(3, "<dark_red>" + "Roto");
        
        private final int level;
        private final String displayName;
        
        DamageState(int level, String displayName) {
            this.level = level;
            this.displayName = displayName;
        }
        
        public int getLevel() {
            return level;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static DamageState fromLevel(int level) {
            for (DamageState state : values()) {
                if (state.getLevel() == level) {
                    return state;
                }
            }
            return HEALTHY;
        }
    }
    
    /**
     * Constructor del sistema de daño por extremidades
     * @param plugin Instancia del plugin principal
     */
    public LimbDamageSystem(SavageFrontierMain plugin) {
        this.plugin = plugin;
        this.limbDamage = new HashMap<>();
        this.isActive = false;
        
        // Cargar configuración
        loadConfig();
    }
    
    /**
     * Carga la configuración desde config.yml
     */
    private void loadConfig() {
        @SuppressWarnings("unused")
        FileConfiguration config = plugin.getConfig();
        // Aquí se cargarían valores de configuración específicos si los hubiera
    }
    
    /**
     * Inicializa el sistema de daño por extremidades
     */
    public void initialize() {
        // Verificar si el sistema está habilitado en la configuración
        if (!plugin.getConfig().getBoolean("limb_damage.enabled", true)) {
            plugin.getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + "<yellow> Sistema de daño por extremidades deshabilitado en la configuración"));
            return;
        }
        
        if (!isActive) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            isActive = true;
            plugin.getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Sistema de daño por extremidades activado"));
        }
    }
    
    /**
     * Desactiva el sistema de daño por extremidades
     */
    public void shutdown() {
        if (isActive) {
            isActive = false;
            plugin.getServer().getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Sistema de daño por extremidades desactivado"));
        }
    }
    
    /**
     * Verifica si el sistema está activo
     * @return true si el sistema está activo, false en caso contrario
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Maneja el evento de daño a entidades para aplicar daño a extremidades
     * @param event Evento de daño
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!isActive || !(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        DamageCause cause = event.getCause();
        DamageSource source = event.getDamageSource();
        double damage = event.getDamage();
        
        // Verificar si el daño es por poción (no aplicar daño a extremidades)
        if (source == CustomDamageTypes.DamageSourceBuilder(player, player, CustomDamageTypes.HYPERTHERMIA_KEY) ||
        source == CustomDamageTypes.DamageSourceBuilder(player, player, CustomDamageTypes.HYPOTHERMIA_KEY) ||
        source == CustomDamageTypes.DamageSourceBuilder(player, player, CustomDamageTypes.BLEEDING_KEY) ||
        source == CustomDamageTypes.DamageSourceBuilder(player, player, CustomDamageTypes.FREEZING_KEY) ||
        source == CustomDamageTypes.DamageSourceBuilder(player, player, CustomDamageTypes.DEHYDRATION_KEY)) {
            return; // No aplicar daño a extremidades por daños custom
        }
        
        // Determinar qué extremidad dañar según el tipo de daño
        LimbType targetLimb = null;
        
        switch (cause) {
            case FALL:
                // Daño de caída afecta a las piernas
                targetLimb = Math.random() < 0.5 ? LimbType.LEFT_LEG : LimbType.RIGHT_LEG;
                break;
                
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
            case PROJECTILE:
                // Ataques pueden afectar cualquier extremidad
                double random = Math.random();
                if (random < 0.2) {
                    targetLimb = LimbType.HEAD;
                } else if (random < 0.4) {
                    targetLimb = LimbType.LEFT_ARM;
                } else if (random < 0.6) {
                    targetLimb = LimbType.RIGHT_ARM;
                } else if (random < 0.8) {
                    targetLimb = LimbType.LEFT_LEG;
                } else {
                    targetLimb = LimbType.RIGHT_LEG;
                }
                break;
                
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                // Explosiones pueden dañar múltiples extremidades
                for (LimbType limb : LimbType.values()) {
                    if (Math.random() < 0.4) { // 40% de probabilidad para cada extremidad
                        applyDamageToLimb(player, limb, damage);
                    }
                }
                return; // Ya aplicamos el daño a múltiples extremidades
                
            default:
                // Otros tipos de daño afectan una extremidad aleatoria
                targetLimb = LimbType.values()[(int) (Math.random() * LimbType.values().length)];
                break;
        }
        
        // Aplicar daño a la extremidad seleccionada
        if (targetLimb != null) {
            applyDamageToLimb(player, targetLimb, damage);
        }
    }
    
    /**
     * Aplica daño a una extremidad específica
     * @param player Jugador afectado
     * @param limbType Tipo de extremidad
     * @param damage Cantidad de daño
     */
    public void applyDamageToLimb(Player player, LimbType limbType, double damage) {
        // Verificar si el jugador está protegido como nuevo jugador
        if (plugin.isPlayerProtectedFromSystem(player, "limb_damage")) {
            return; // No aplicar daño si el jugador está protegido
        }
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar mapa de daño para el jugador si no existe
        limbDamage.putIfAbsent(playerId, new HashMap<>());
        Map<LimbType, Integer> playerLimbDamage = limbDamage.get(playerId);
        
        // Inicializar daño para la extremidad si no existe
        playerLimbDamage.putIfAbsent(limbType, 0);
        
        // Calcular nuevo nivel de daño
        int currentDamage = playerLimbDamage.get(limbType);
        int damageToAdd = (int) Math.ceil(damage / 4.0); // Convertir daño a niveles de daño de extremidad
        
        int newDamage = Math.min(currentDamage + damageToAdd, DamageState.BROKEN.getLevel());
        playerLimbDamage.put(limbType, newDamage);
        
        // Notificar al jugador si el estado de la extremidad ha cambiado
        DamageState oldState = DamageState.fromLevel(currentDamage);
        DamageState newState = DamageState.fromLevel(newDamage);
        
        if (oldState != newState) {
            if (plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Tu " + limbType.getDisplayName() + " <red>está ahora " + newState.getDisplayName()));
            }
            applyEffectsForLimbDamage(player, limbType, newState);
        }
    }
    
    /**
     * Aplica efectos según el daño en la extremidad
     * @param player Jugador afectado
     * @param limbType Tipo de extremidad
     * @param state Estado de daño
     */
    private void applyEffectsForLimbDamage(Player player, LimbType limbType, DamageState state) {
        // Verificar si el jugador está protegido como nuevo jugador
        if (plugin.isPlayerProtectedFromSystem(player, "limb_damage")) {
            return; // No aplicar efectos si el jugador está protegido
        }
        
        // Actualizar la salud máxima del jugador basado en extremidades rotas
        updatePlayerMaxHealth(player);
        
        switch (limbType) {
            case HEAD:
                // Daño en la cabeza afecta la visión y causa náuseas
                if (state == DamageState.DAMAGED) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 0));
                } else if (state == DamageState.CRITICAL) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 400, 0));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                } else if (state == DamageState.BROKEN) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 600, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 1));
                }
                break;
                
            case LEFT_ARM:
            case RIGHT_ARM:
                // Daño en los brazos afecta la velocidad de ataque y minado
                if (state == DamageState.DAMAGED) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 600, 0));
                } else if (state == DamageState.CRITICAL) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 1200, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 1200, 0));
                } else if (state == DamageState.BROKEN) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 2400, 2));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 2400, 1));
                }
                break;
                
            case LEFT_LEG:
            case RIGHT_LEG:
                // Daño en las piernas afecta la velocidad de movimiento y salto
                if (state == DamageState.DAMAGED) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 600, 0));
                } else if (state == DamageState.CRITICAL) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 1200, 1));
                } else if (state == DamageState.BROKEN) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2400, 3));
                }
                break;
        }
    }
    
    /**
     * Obtiene el estado de daño de una extremidad
     * @param player Jugador a verificar
     * @param limbType Tipo de extremidad
     * @return Estado de daño de la extremidad
     */
    public DamageState getLimbDamageState(Player player, LimbType limbType) {
        UUID playerId = player.getUniqueId();
        
        if (!limbDamage.containsKey(playerId)) {
            return DamageState.HEALTHY;
        }
        
        Map<LimbType, Integer> playerLimbDamage = limbDamage.get(playerId);
        
        if (!playerLimbDamage.containsKey(limbType)) {
            return DamageState.HEALTHY;
        }
        
        int damage = playerLimbDamage.get(limbType);
        return DamageState.fromLevel(damage);
    }
    
    /**
     * Cura una extremidad específica
     * @param player Jugador a curar
     * @param limbType Tipo de extremidad
     */
    public void healLimb(Player player, LimbType limbType) {
        UUID playerId = player.getUniqueId();
        
        if (!limbDamage.containsKey(playerId)) {
            return;
        }
        
        Map<LimbType, Integer> playerLimbDamage = limbDamage.get(playerId);
        
        if (playerLimbDamage.containsKey(limbType)) {
            playerLimbDamage.put(limbType, 0);
            if (plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Tu " + limbType.getDisplayName() + " ha sido curada."));
            }
            
            // Actualizar la salud máxima del jugador después de curar
            updatePlayerMaxHealth(player);
        }
    }
    
    /**
     * Cura todas las extremidades de un jugador
     * @param player Jugador a curar
     */
    public void healAllLimbs(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!limbDamage.containsKey(playerId)) {
            return;
        }
        
        Map<LimbType, Integer> playerLimbDamage = limbDamage.get(playerId);
        
        for (LimbType limbType : LimbType.values()) {
            playerLimbDamage.put(limbType, 0);
        }
        
        if (plugin.getUserPreferencesManager().hasStatusMessages(player)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Todas tus extremidades han sido curadas."));
        }
        
        // Restaurar la salud máxima del jugador después de curar todas las extremidades
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
        
    }
    
    /**
     * Muestra el estado de todas las extremidades de un jugador
     * @param player Jugador a mostrar
     * @return Mensaje con el estado de las extremidades
     */
    public @NotNull Component getLimbStatusMessage(Player player) {
        StringBuilder message = new StringBuilder();
        MiniMessage mm = MiniMessage.miniMessage();

        message.append(("<gray>----------------------------------------\n"));
        message.append(("<aqua>Estado de las Extremidades:\n"));
        
        for (LimbType limbType : LimbType.values()) {
            DamageState state = getLimbDamageState(player, limbType);
            message.append(("<yellow>" + limbType.getDisplayName() + ": " + state.getDisplayName() + "\n"));
        }
        
        message.append(("<gray>----------------------------------------"));
        return mm.deserialize(message.toString());
    }
    
    /**
     * Establece el nivel de daño de una extremidad específica
     * @param player Jugador al que se le aplicará el cambio
     * @param limbType Tipo de extremidad a modificar
     * @param damageLevel Nivel de daño a establecer (0-100)
     * @return Estado de daño resultante
     */
    public DamageState setLimbDamage(Player player, LimbType limbType, int damageLevel) {
        if (player == null || limbType == null) {
            return DamageState.HEALTHY;
        }
        
        // Asegurar que el nivel de daño esté en el rango válido
        damageLevel = Math.max(0, Math.min(100, damageLevel));
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar el mapa de daño si no existe
        if (!limbDamage.containsKey(playerId)) {
            limbDamage.put(playerId, new java.util.HashMap<>());
        }
        
        Map<LimbType, Integer> playerLimbDamage = limbDamage.get(playerId);
        
        // Establecer el nivel de daño
        playerLimbDamage.put(limbType, damageLevel);
        
        // Aplicar efectos basados en el nuevo nivel de daño
        DamageState newState = getDamageState(damageLevel);
        applyEffectsForLimbDamage(player, limbType, newState);
        
        // Actualizar la salud máxima del jugador
        updatePlayerMaxHealth(player);
        
        // Notificar al jugador sobre el cambio
        String stateMessage;
        if (newState == DamageState.HEALTHY) {
            stateMessage = "<green>" + "saludable";
        } else if (newState == DamageState.DAMAGED) {
            stateMessage = "<yellow>" + "lesionada";
        } else if (newState == DamageState.BROKEN) {
            stateMessage = "<red>" + "fracturada";
        } else {
            stateMessage = "<dark_red>" + "crítica";
        }
        
        if (plugin.getUserPreferencesManager().hasStatusMessages(player)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<gray>Tu <yellow>" + limbType.getDisplayName() + " <gray>ahora está " + stateMessage + 
                    "<yellow>" + " (" + damageLevel + "%)"));
        }
        
        return newState;
    }
    
    /**
     * Determina el estado de daño basado en un nivel numérico
     * @param damageLevel Nivel de daño (0-100)
     * @return Estado de daño correspondiente
     */
    private DamageState getDamageState(int damageLevel) {
        if (damageLevel < 30) {
            return DamageState.HEALTHY;
        } else if (damageLevel < 60) {
            return DamageState.DAMAGED;
        } else if (damageLevel < 90) {
            return DamageState.BROKEN;
        } else {
            return DamageState.CRITICAL;
        }
    }
    
    /**
     * Obtiene el nivel de daño numérico de una extremidad
     * @param player Jugador a consultar
     * @param limbType Tipo de extremidad
     * @return Nivel de daño (0-100)
     */
    public int getLimbDamageLevel(Player player, LimbType limbType) {
        if (player == null || limbType == null) {
            return 0;
        }
        
        UUID playerId = player.getUniqueId();
        
        if (!limbDamage.containsKey(playerId)) {
            return 0;
        }
        
        Map<LimbType, Integer> playerLimbDamage = limbDamage.get(playerId);
        
        return playerLimbDamage.getOrDefault(limbType, 0);
    }
    
    /**
     * Obtiene todos los niveles de daño de un jugador
     * @param player Jugador a consultar
     * @return Mapa con los niveles de daño de todas las extremidades
     */
    public Map<LimbType, Integer> getAllLimbDamageLevels(Player player) {
        if (player == null) {
            return null;
        }
        
        UUID playerId = player.getUniqueId();
        
        if (!limbDamage.containsKey(playerId)) {
            Map<LimbType, Integer> newDamageMap = new HashMap<>();
            for (LimbType type : LimbType.values()) {
                newDamageMap.put(type, 0);
            }
            return newDamageMap;
        }
        
        // Crear una copia del mapa para evitar modificaciones externas
        Map<LimbType, Integer> result = new HashMap<>(limbDamage.get(playerId));
        
        // Asegurar que todas las extremidades estén incluidas
        for (LimbType type : LimbType.values()) {
            if (!result.containsKey(type)) {
                result.put(type, 0);
            }
        }
        
        return result;
    }
    
    /**
     * Establece todos los niveles de daño para un jugador
     * @param player Jugador al que se le aplicarán los cambios
     * @param damageLevels Mapa con los niveles de daño para cada extremidad
     */
    public void setAllLimbDamageLevels(Player player, Map<LimbType, Integer> damageLevels) {
        if (player == null || damageLevels == null) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        // Crear un nuevo mapa para almacenar los valores
        Map<LimbType, Integer> newDamageMap = new HashMap<>();
        
        // Copiar y validar cada nivel de daño
        for (LimbType type : LimbType.values()) {
            int level = damageLevels.containsKey(type) ? damageLevels.get(type) : 0;
            newDamageMap.put(type, Math.max(0, Math.min(100, level)));
        }
        
        // Actualizar todos los niveles
        limbDamage.put(playerId, newDamageMap);
        
        // Aplicar efectos para cada extremidad si es necesario
        for (LimbType type : LimbType.values()) {
            int level = newDamageMap.get(type);
            if (level > 0) {
                DamageState state = getDamageState(level);
                applyEffectsForLimbDamage(player, type, state);
            }
        }
        
        // Actualizar la salud máxima del jugador
        updatePlayerMaxHealth(player);
    }
    
    /**
     * Actualiza la salud máxima del jugador basado en el número de extremidades rotas
     * @param player Jugador a actualizar
     */
    private void updatePlayerMaxHealth(Player player) {
        if (player == null) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        
        if (!limbDamage.containsKey(playerId)) {
            // Si no hay datos de daño, restaurar la salud máxima por defecto
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
            return;
        }
        
        Map<LimbType, Integer> playerLimbDamage = limbDamage.get(playerId);
        int brokenLimbs = 0;
        
        // Contar extremidades rotas (estado BROKEN o CRITICAL)
        for (LimbType type : LimbType.values()) {
            int damageLevel = playerLimbDamage.getOrDefault(type, 0);
            DamageState state = getDamageState(damageLevel);
            if (state == DamageState.BROKEN || state == DamageState.CRITICAL) {
                brokenLimbs++;
            }
        }
        
        // Calcular nueva salud máxima (20.0 - 3.5 por cada extremidad rota)
        double maxHealth = Math.max(1.0, 20.0 - (brokenLimbs * 3.5));
        
        // Aplicar nueva salud máxima
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
        
        // Informar al jugador si hay cambios significativos
        if (brokenLimbs > 0 && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Tus extremidades rotas han reducido tu salud máxima a " + 
                    "<gold>" + maxHealth + "<red>" + " puntos."));
        }
    }
}