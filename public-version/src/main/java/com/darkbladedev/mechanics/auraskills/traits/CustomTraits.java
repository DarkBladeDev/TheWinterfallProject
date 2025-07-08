package com.darkbladedev.mechanics.auraskills.traits;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import dev.aurelium.auraskills.api.trait.CustomTrait;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.api.stat.CustomStat;

import org.bukkit.entity.Player;
import com.darkbladedev.SavageFrontierMain;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase centralizada para la gestión de traits personalizados en AuraSkills.
 * Permite crear, registrar y acceder a traits y stats personalizados de forma organizada.
 */
public class CustomTraits {
    
    @SuppressWarnings("unused")
    private final SavageFrontierMain plugin;
    private final AuraSkillsApi api;
    private final NamespacedRegistry registry;
    private final Map<String, CustomTrait> traits;
    private final Map<String, CustomStat> stats;
    private boolean initialized;
    
    /**
     * Constructor de la clase CustomTraits.
     * @param plugin Instancia del plugin principal
     * @param api Instancia de AuraSkillsApi
     */
    public CustomTraits(SavageFrontierMain plugin, AuraSkillsApi api) {
        this.plugin = plugin;
        this.api = api;
        this.registry = api.useRegistry("savage-frontier", new File(plugin.getDataFolder(), "auraskills"));
        this.traits = new HashMap<>();
        this.stats = new HashMap<>();
        this.initialized = false;
    }
    
    /**
     * Inicializa y registra todos los traits y stats personalizados.
     * Este método debe ser llamado después de que AuraSkills haya cargado completamente.
     */
    public void initialize() {
        if (initialized) return;
        
        // Registrar traits de estamina
        registerTraits();

        // Registrar otros traits personalizados aquí
        // registerOtherTraits();
        
        this.initialized = true;
    }
    
    /**
     * Registra los traits relacionados con el sistema de estamina.
     */
    private void registerTraits() {
        // Registrar traits en el registro de AuraSkills
        registry.registerTrait(staminaCapacity);
        registry.registerTrait(staminaRecovery);
        registry.registerTrait(limbDamageReduction);
        registry.registerTrait(limbRecoveryRate);
        
        // Almacenar traits en el mapa interno para acceso fácil
        traits.put("stamina_capacity", staminaCapacity);
        traits.put("stamina_recovery", staminaRecovery);
        traits.put("limb_damage_reduction", limbDamageReduction);
        traits.put("limb_recovery_rate", limbRecoveryRate);
        
    }


    // STAMINA SYSTEM TRAITS
    public static final CustomTrait staminaCapacity = CustomTrait.builder(NamespacedId.of("savage-frontier", "stamina_capacity"))
                                                                .displayName("Capacidad de Estamina")
                                                                .build();

    public static final CustomTrait staminaRecovery = CustomTrait.builder(NamespacedId.of("savage-frontier", "stamina_recovery"))
                                                                .displayName("Recuperación de Estamina")
                                                                .build();


    // LIMB DAMAGE SYSTEM TRAITS
    public static final CustomTrait limbDamageReduction = CustomTrait.builder(NamespacedId.of("savage-frontier", "limb_damage_reduction"))
                                                                .displayName("Reducción de Daño a extremidades")
                                                                .build();
    public static final CustomTrait limbRecoveryRate = CustomTrait.builder(NamespacedId.of("savage-frontier", "limb_recovery_rate"))
                                                                .displayName("Recuperación de extremidades")
                                                                .build();

    
    /**
     * Registra un trait personalizado en AuraSkills y lo almacena en el mapa interno.
     * @param id Identificador del trait
     * @param trait Trait a registrar
     */
    public void registerTrait(String id, CustomTrait trait) {
        registry.registerTrait(trait);
        traits.put(id, trait);
    }
    
    
    /**
     * Obtiene un trait personalizado por su ID.
     * @param id Identificador del trait
     * @return El trait correspondiente o null si no existe
     */
    public CustomTrait getTrait(String id) {
        return traits.get(id);
    }

    
    /**
     * Obtiene el nivel de un trait para un jugador específico.
     * @param player Jugador
     * @param traitId Identificador del trait
     * @return Nivel del trait o 0 si no se puede obtener
     */
    public double getTraitLevel(Player player, String traitId) {
        try {
            CustomTrait trait = getTrait(traitId);
            if (trait == null || player == null) return 0;
            
            SkillsUser user = api.getUser(player.getUniqueId());
            if (user == null) return 0;
            
            return user.getEffectiveTraitLevel(trait);
        } catch (Exception e) {
            return 0;
        }
    }
    
    
    /**
     * Verifica si la clase ha sido inicializada correctamente.
     * @return true si está inicializada, false en caso contrario
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Obtiene el registro de AuraSkills utilizado por esta clase.
     * @return El registro de AuraSkills
     */
    public NamespacedRegistry getRegistry() {
        return registry;
    }
    
    /**
     * Obtiene el trait de capacidad de estamina.
     * @return El trait de capacidad de estamina o null si no está inicializado
     */
    public CustomTrait getStaminaCapacityTrait() {
        return getTrait("stamina_capacity");
    }
    
    /**
     * Obtiene el trait de recuperación de estamina.
     * @return El trait de recuperación de estamina o null si no está inicializado
     */
    public CustomTrait getStaminaRecoveryTrait() {
        return getTrait("stamina_recovery");
    }
    
    /**
     * Obtiene un stat personalizado por su ID.
     * @param id Identificador del stat
     * @return El stat correspondiente o null si no existe
     */
    public CustomStat getStat(String id) {
        return stats.get(id);
    }
    
    /**
     * Obtiene el nivel de un stat para un jugador específico.
     * @param player Jugador
     * @param statId Identificador del stat
     * @return Nivel del stat o 0 si no se puede obtener
     */
    public double getStatLevel(Player player, String statId) {
        try {
            CustomStat stat = getStat(statId);
            if (stat == null || player == null) return 0;
            
            SkillsUser user = api.getUser(player.getUniqueId());
            if (user == null) return 0;
            
            return user.getStatLevel(stat);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Obtiene el stat de resistencia (endurance).
     * @return El stat de resistencia o null si no está inicializado
     */
    public CustomStat getEnduranceStat() {
        return getStat("endurance");
    }
}
