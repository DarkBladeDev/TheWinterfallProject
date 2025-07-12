package com.darkbladedev.mechanics;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.CustomTypes.CustomDamageTypes;
import com.darkbladedev.mechanics.events.limb.PlayerLimbDamageEvent;
import com.darkbladedev.utils.AuraSkillsUtil;

import bodyhealth.api.BodyHealthAPI;
import bodyhealth.core.BodyPart;
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
import org.bukkit.Bukkit;
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
        HEAD("Cabeza", BodyPart.HEAD),
        TORSO("Torso", BodyPart.BODY),
        LEFT_ARM("Brazo Izquierdo", BodyPart.ARM_LEFT),
        RIGHT_ARM("Brazo Derecho", BodyPart.ARM_RIGHT),
        LEFT_LEG("Pierna Izquierda", BodyPart.LEG_LEFT),
        RIGHT_LEG("Pierna Derecha", BodyPart.LEG_RIGHT),
        LEFT_FOOT("Pie Izquierdo", BodyPart.FOOT_LEFT),
        RIGHT_FOOT("Pie Derecho", BodyPart.FOOT_RIGHT);
        
        private final String displayName;
        private final BodyPart bodyPart;
        
        LimbType(String displayName, BodyPart bodyPart) {
            this.displayName = displayName;
            this.bodyPart = bodyPart;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public BodyPart getBodyPart() {
            return bodyPart;
        }
        
        /**
         * Convierte un BodyPart a LimbType
         * @param bodyPart El BodyPart a convertir
         * @return El LimbType correspondiente o null si no hay correspondencia
         */
        public static LimbType fromBodyPart(BodyPart bodyPart) {
            for (LimbType type : values()) {
                if (type.getBodyPart() == bodyPart) {
                    return type;
                }
            }
            return null;
        }
    }

    // Estados de daño
    public enum DamageState {
        HEALTHY(0, "<green>" + "Sano"),
        DAMAGED(1, "<yellow>" + "Herido"),
        CRITICAL(2, "<red>" + "Crítico"),
        BROKEN(3, "<gray>" + "Roto");
        
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
        BodyPart targetBodyPart = null;
        
        switch (cause) {
            case FALL:
                // Daño de caída afecta a las piernas
                targetBodyPart = Math.random() < 0.5 ? BodyPart.LEG_LEFT : BodyPart.LEG_RIGHT;
                break;
                
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
            case PROJECTILE:
                // Ataques pueden afectar cualquier extremidad
                double random = Math.random();
                if (random < 0.2) {
                    targetBodyPart = BodyPart.HEAD;
                } else if (random < 0.4) {
                    targetBodyPart = BodyPart.ARM_LEFT;
                } else if (random < 0.6) {
                    targetBodyPart = BodyPart.ARM_RIGHT;
                } else if (random < 0.8) {
                    targetBodyPart = BodyPart.LEG_LEFT;
                } else {
                    targetBodyPart = BodyPart.LEG_RIGHT;
                }
                break;
                
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                // Explosiones pueden dañar múltiples extremidades
                for (BodyPart bodyPart : BodyPart.values()) {
                    if (Math.random() < 0.4) { // 40% de probabilidad para cada extremidad
                        // Aplicar modificadores de daño antes de usar la API
                        double modifiedDamage = applyDamageModifiers(player, bodyPart, damage);
                        // Usar la API de BodyHealth para aplicar el daño
                        BodyHealthAPI.damagePlayerWithConfig(player, cause, modifiedDamage, bodyPart);
                    }
                }
                return; // Ya aplicamos el daño a múltiples extremidades
                
            default:
                // Otros tipos de daño afectan una extremidad aleatoria
                BodyPart[] bodyParts = BodyPart.values();
                targetBodyPart = bodyParts[(int) (Math.random() * bodyParts.length)];
                break;
        }
        
        // Aplicar daño a la extremidad seleccionada
        if (targetBodyPart != null) {
            // Aplicar modificadores de daño antes de usar la API
            double modifiedDamage = applyDamageModifiers(player, targetBodyPart, damage);
            // Usar la API de BodyHealth para aplicar el daño
            BodyHealthAPI.damagePlayerWithConfig(player, cause, modifiedDamage, targetBodyPart);
            
            // Obtener el LimbType correspondiente para mantener compatibilidad con eventos personalizados
            LimbType targetLimb = LimbType.fromBodyPart(targetBodyPart);
            if (targetLimb != null) {
                // Disparar evento personalizado para mantener compatibilidad con otros sistemas
                DamageState oldState = getLimbDamageState(player, targetLimb);
                // Calcular nuevo estado basado en la salud actual del BodyPart
                double healthPercent = BodyHealthAPI.getHealth(player, targetBodyPart);
                DamageState newState = getDamageStateFromHealthPercent(healthPercent);
                
                if (oldState != newState) {
                    Bukkit.getPluginManager().callEvent(new PlayerLimbDamageEvent(player, targetLimb, oldState, newState));
                }
            }
        }
    }
    
    /**
     * Aplica modificadores de daño basados en habilidades y stats del jugador
     * @param player Jugador afectado
     * @param bodyPart Parte del cuerpo afectada
     * @param damage Cantidad de daño base
     * @return Daño modificado
     */
    private double applyDamageModifiers(Player player, BodyPart bodyPart, double damage) {
        // Verificar si el jugador está protegido como nuevo jugador
        if (plugin.isPlayerProtectedFromSystem(player, "limb_damage")) {
            return 0; // No aplicar daño si el jugador está protegido
        }
        
        // INTEGRACIÓN AURASKILLS: Consultar stat de fortaleza para modificar la severidad del daño
        int fortitudeLevel = AuraSkillsUtil.getCustomStatLevel(player, "fortitude");
        // Cada nivel de fortaleza reduce el daño en un 2% (máx 40%)
        double fortitudeReduction = Math.min(0.02 * fortitudeLevel, 0.4);
        damage = damage * (1.0 - fortitudeReduction);
        
        // INTEGRACIÓN AURASKILLS: Reducción de daño por PainToleranceSkill
        Map<String, Integer> stats = new HashMap<>();
        stats.put("vitality", AuraSkillsUtil.getCustomStatLevel(player, "vitality"));
        boolean hasPainTolerance = com.darkbladedev.mechanics.auraskills.skilltrees.SkillTreeManager.getInstance()
                .hasSkill(player, com.darkbladedev.mechanics.auraskills.skills.vitality.PainToleranceSkill.class, stats);
        if (hasPainTolerance) {
            damage = damage * 0.7; // Reduce el daño en un 30%
        }
        
        return damage;
    }
    
    /**
     * Convierte un porcentaje de salud a un estado de daño
     * @param healthPercent Porcentaje de salud (0-100)
     * @return Estado de daño correspondiente
     */
    private DamageState getDamageStateFromHealthPercent(double healthPercent) {
        if (healthPercent > 70) {
            return DamageState.HEALTHY;
        } else if (healthPercent > 40) {
            return DamageState.DAMAGED;
        } else if (healthPercent > 10) {
            return DamageState.CRITICAL;
        } else {
            return DamageState.BROKEN;
        }
    }
    
    /**
     * Aplica daño a una extremidad específica del jugador
     * @param player Jugador afectado
     * @param limbType Tipo de extremidad
     * @param damage Cantidad de daño base
     * @deprecated Usar directamente bodyHealthAPI.damagePlayerWithConfig()
     */
    @Deprecated
    public void applyDamageToLimb(Player player, LimbType limbType, double damage) {
        // Verificar si el jugador está protegido como nuevo jugador
        if (plugin.isPlayerProtectedFromSystem(player, "limb_damage")) {
            return; // No aplicar daño si el jugador está protegido
        }
        
        // Convertir LimbType a BodyPart
        BodyPart bodyPart = limbType.getBodyPart();
        if (bodyPart == null) {
            return; // No se pudo convertir
        }
        
        // Aplicar modificadores de daño
        double modifiedDamage = applyDamageModifiers(player, bodyPart, damage);
        
        // Obtener estado anterior para comparación
        DamageState oldState = getLimbDamageState(player, limbType);
        
        // Usar la API de BodyHealth para aplicar el daño
        BodyHealthAPI.damagePlayerWithConfig(player, DamageCause.CUSTOM, modifiedDamage, bodyPart);
        
        // Obtener el nuevo estado basado en la salud actual
        double healthPercent = BodyHealthAPI.getHealth(player, bodyPart);
        DamageState newState = getDamageStateFromHealthPercent(healthPercent);
        
        // Si el estado cambió, notificar al jugador y aplicar efectos
        if (oldState != newState) {
            if (plugin.getUserPreferencesManager().hasStatusMessages(player)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize("<red>Tu " + limbType.getDisplayName() + " <red>está ahora " + newState.getDisplayName()));
            }
            try {
                applyEffectsForLimbDamage(player, limbType, newState);
                Bukkit.getPluginManager().callEvent(new PlayerLimbDamageEvent(player, limbType, oldState, newState));
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                    plugin.getCustomDebuffEffects().applyWeakness(player);
                    //player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 1));
                }
                break;
                
            case LEFT_ARM:
            case RIGHT_ARM:
                // Daño en los brazos afecta la velocidad de ataque y minado
                if (state == DamageState.DAMAGED) {
                    plugin.getCustomDebuffEffects().applyMiningFatigue(player);
                    //player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 600, 0));
                } else if (state == DamageState.CRITICAL) {
                    plugin.getCustomDebuffEffects().applyWeakness(player);
                    plugin.getCustomDebuffEffects().applyMiningFatigue(player);
                    //player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 1200, 1));
                    //player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 1200, 0));
                } else if (state == DamageState.BROKEN) {
                    plugin.getCustomDebuffEffects().applyMiningFatigue(player);
                    plugin.getCustomDebuffEffects().applyWeakness(player);
                    //player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 2400, 2));
                    //player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 2400, 1));
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
            case LEFT_FOOT:
                break;
            case RIGHT_FOOT:
                break;
            case TORSO:
                break;
            default:
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
        BodyPart bodyPart = limbType.getBodyPart();
        if (bodyPart == null) {
            return DamageState.HEALTHY; // Valor por defecto si no se puede convertir
        }
        
        // Obtener salud actual del BodyPart usando la API
        double healthPercent = BodyHealthAPI.getHealth(player, bodyPart);
        return getDamageStateFromHealthPercent(healthPercent);
    }
    
    /**
     * Cura una extremidad específica
     * @param player Jugador a curar
     * @param limbType Tipo de extremidad
     */
    public void healLimb(Player player, LimbType limbType) {
        BodyPart bodyPart = limbType.getBodyPart();
        if (bodyPart == null) {
            return; // No se pudo convertir
        }
        
        // Usar la API de BodyHealth para curar la parte del cuerpo
        BodyHealthAPI.setHealth(player, 100.0, bodyPart); // Establecer salud al 100%
        
        if (plugin.getUserPreferencesManager().hasStatusMessages(player)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Tu " + limbType.getDisplayName() + " ha sido curada."));
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<green>Tu " + limbType.getDisplayName() + " ha sido curada."));
        }
        
        // Actualizar la salud máxima del jugador después de curar
        updatePlayerMaxHealth(player);
    }
    
    /**
     * Cura todas las extremidades de un jugador
     * @param player Jugador a curar
     */
    public void healAllLimbs(Player player) {
        // Usar la API de BodyHealth para curar todas las partes del cuerpo
        for (BodyPart bodyPart : BodyPart.values()) {
            BodyHealthAPI.setHealth(player, 100.0, bodyPart); // Establecer salud al 100%
        }
        
        // Actualizar salud máxima del jugador
        updatePlayerMaxHealth(player);
        
        if (plugin.getUserPreferencesManager().hasStatusMessages(player)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize("<green>Todas tus extremidades han sido curadas."));
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<green>Todas tus extremidades han sido curadas."));
        }
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
        
        // Convertir LimbType a BodyPart
        BodyPart bodyPart = limbType.getBodyPart();
        if (bodyPart == null) {
            return DamageState.HEALTHY; // No se pudo convertir
        }
        
        // Convertir nivel de daño a porcentaje de salud (invertido)
        // 0 daño = 100% salud, 100 daño = 0% salud
        double healthPercent = 100.0 - damageLevel;
        
        // Usar la API de BodyHealth para establecer la salud
        BodyHealthAPI.setHealth(player, healthPercent, bodyPart);
        
        // Obtener nuevo estado basado en el nivel de daño
        DamageState newState = getDamageStateFromHealthPercent(healthPercent);
        
        // Aplicar efectos basados en el nuevo nivel de daño
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
    public DamageState getDamageState(int damageLevel) {
        if (damageLevel < 30) {
            return DamageState.HEALTHY;
        } else if (damageLevel < 60) {
            return DamageState.DAMAGED;
        } else if (damageLevel < 90) {
            return DamageState.CRITICAL;
        } else {
            return DamageState.BROKEN;
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
        
        BodyPart bodyPart = limbType.getBodyPart();
        if (bodyPart == null) {
            return 0; // No se pudo convertir
        }
        
        // Obtener salud actual del BodyPart usando la API
        double healthPercent = BodyHealthAPI.getHealth(player, bodyPart);
        
        // Convertir porcentaje de salud a nivel de daño (invertido)
        // 100% salud = 0 daño, 0% salud = 100 daño
        return (int) Math.round(100.0 - healthPercent);
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
        
        Map<LimbType, Integer> damageMap = new HashMap<>();
         
         // Obtener niveles de daño para cada extremidad usando la API de BodyHealth
         for (LimbType limbType : LimbType.values()) {
             BodyPart bodyPart = limbType.getBodyPart();
             if (bodyPart != null) {
                 // Obtener salud y convertir a nivel de daño
                 double healthPercent = BodyHealthAPI.getHealth(player, bodyPart);
                 int damageLevel = (int) Math.round(100.0 - healthPercent);
                 damageMap.put(limbType, damageLevel);
             } else {
                 // Si no hay equivalente en BodyPart, establecer como saludable
                 damageMap.put(limbType, 0);
             }
         }
         
        return damageMap;
    }
    
    /**
     * Verifica todos los niveles de daño para un jugador
     * @param player Jugador a verificar
     * @return Mapa con los niveles de daño para cada extremidad
     */
    public Map<LimbType, Integer> checkAllLimbDamageLevels(Player player) {
        if (player == null) {
            return null;
        }
        
        // Usar el método getAllLimbDamageLevels que ya está actualizado para usar BodyHealth API
        return getAllLimbDamageLevels(player);
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
        
        // Aplicar cada nivel de daño usando la API de BodyHealth
        for (Map.Entry<LimbType, Integer> entry : damageLevels.entrySet()) {
            LimbType limbType = entry.getKey();
            int damageLevel = Math.max(0, Math.min(100, entry.getValue())); // Validar rango
            
            // Convertir LimbType a BodyPart
            BodyPart bodyPart = limbType.getBodyPart();
            if (bodyPart != null) {
                // Convertir nivel de daño a porcentaje de salud (invertido)
                double healthPercent = 100.0 - damageLevel;
                
                // Usar la API de BodyHealth para establecer la salud
                BodyHealthAPI.setHealth(player, healthPercent, bodyPart);
                
                // Aplicar efectos si es necesario
                if (damageLevel > 0) {
                    DamageState state = getDamageStateFromHealthPercent(healthPercent);
                    applyEffectsForLimbDamage(player, limbType, state);
                }
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
        
        int brokenLimbs = 0;
        
        // Contar extremidades rotas (estado BROKEN o CRITICAL) usando BodyHealth API
        for (LimbType type : LimbType.values()) {
            BodyPart bodyPart = type.getBodyPart();
            if (bodyPart != null) {
                double healthPercent = BodyHealthAPI.getHealth(player, bodyPart);
                DamageState state = getDamageStateFromHealthPercent(healthPercent);
                if (state == DamageState.BROKEN || state == DamageState.CRITICAL) {
                    brokenLimbs++;
                }
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