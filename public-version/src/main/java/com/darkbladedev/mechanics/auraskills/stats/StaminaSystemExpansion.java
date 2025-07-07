package com.darkbladedev.mechanics.auraskills.stats;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.mechanics.StaminaSystem;
import com.darkbladedev.mechanics.auraskills.traits.StaminaTraitHandler;
import com.darkbladedev.utils.AuraSkillsUtil;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import dev.aurelium.auraskills.api.stat.CustomStat;
import dev.aurelium.auraskills.api.trait.CustomTrait;
import dev.aurelium.auraskills.api.user.SkillsUser;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Integración del sistema de estamina con AuraSkills
 * Permite que la estamina sea afectada por estadísticas y habilidades de AuraSkills
 */
public class StaminaSystemExpansion implements Listener {

    private final SavageFrontierMain plugin;
    private final StaminaSystem staminaSystem;
    private final AuraSkillsApi auraSkillsApi;
    @SuppressWarnings("unused")
    private MiniMessage mm = MiniMessage.miniMessage();
    private StaminaTraitHandler traitHandler;
    private boolean enabled;
    
    // Instancias de los traits y stats personalizados
    private CustomTrait staminaCapacityTrait;
    private CustomTrait staminaRecoveryTrait;
    private CustomStat enduranceStat;
    
    /**
     * Constructor de la integración
     * @param plugin Instancia principal del plugin
     * @param staminaSystem Sistema de estamina
     * @param auraSkillsApi API de AuraSkills
     */
    public StaminaSystemExpansion(SavageFrontierMain plugin, StaminaSystem staminaSystem, AuraSkillsApi auraSkillsApi) {
        this.plugin = plugin;
        this.staminaSystem = staminaSystem;
        this.enabled = false;
        this.auraSkillsApi = auraSkillsApi;
        
        // La inicialización se realiza mediante el método público initialize()
        // para permitir que SavageFrontierMain controle cuándo se inicializa
    }
    
    /**
     * Inicializa la integración con AuraSkills
     * Este método puede ser llamado desde SavageFrontierMain
     */
    public void initialize() {
        // Si ya está inicializado, no hacer nada
        if (this.enabled) {
            return;
        }
        // Verificar si AuraSkills está presente
        if (Bukkit.getPluginManager().getPlugin("AuraSkills") == null) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>AuraSkills no encontrado. La integración de estamina no estará disponible."));
            return;
        }
        try {
            // Usar AuraSkillsUtil para crear y registrar traits/stats
            Object[] staminaObjects = AuraSkillsUtil.createStaminaTraitsAndStat();
            this.staminaCapacityTrait = (CustomTrait) staminaObjects[0];
            this.staminaRecoveryTrait = (CustomTrait) staminaObjects[1];
            this.enduranceStat = (CustomStat) staminaObjects[2];
            NamespacedRegistry registry = this.auraSkillsApi.useRegistry("savage-frontier", new File(plugin.getDataFolder(), "auraskills"));
            AuraSkillsUtil.registerStaminaTraitsAndStat(registry, staminaCapacityTrait, staminaRecoveryTrait, enduranceStat);
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Traits y stats de AuraSkills registrados correctamente"));
            // Crear y registrar el manejador de traits
            this.traitHandler = new StaminaTraitHandler(auraSkillsApi, staminaSystem);
            // Registrar eventos
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getServer().getPluginManager().registerEvents(traitHandler, plugin);
            this.enabled = true;
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Integración con AuraSkills inicializada correctamente"));
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al inicializar la integración con AuraSkills: " + e.getMessage()));
            e.printStackTrace();
        }
    }
    
    /**
     * Maneja el evento de salida de un jugador
     * @param event Evento de salida
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Limpiar los modificadores del jugador cuando se desconecta
        if (staminaSystem != null) {
            staminaSystem.clearPlayerModifiers(player);
        }
    }
    
    /**
     * Verifica si la integración está habilitada
     * @return true si la integración está habilitada, false en caso contrario
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Obtiene el nivel de resistencia de un jugador
     * @param player Jugador
     * @return Nivel de resistencia
     */
    public int getEnduranceLevel(Player player) {
        if (!enabled || player == null) return 0;
        
        try {
            SkillsUser user = auraSkillsApi.getUser(player.getUniqueId());
            
            if (user == null) return 0;
            
            return (int) user.getStatLevel(this.enduranceStat);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Calcula el modificador de capacidad de estamina basado en el nivel de resistencia
     * @param player Jugador
     * @return Modificador de capacidad de estamina
     */
    public int calculateStaminaCapacityModifier(Player player) {
        int enduranceLevel = getEnduranceLevel(player);
        return enduranceLevel; // Cada nivel de resistencia da +1 de capacidad
    }
    
    /**
     * Calcula el modificador de recuperación de estamina basado en el nivel de resistencia
     * @param player Jugador
     * @return Modificador de recuperación de estamina
     */
    public double calculateStaminaRecoveryModifier(Player player) {
        int enduranceLevel = getEnduranceLevel(player);
        return enduranceLevel * 0.02; // Cada nivel de resistencia da +2% (0.02) de recuperación
    }
    
    /**
     * Actualiza los modificadores de estamina para un jugador
     * @param player Jugador a actualizar
     */
    public void updatePlayerModifiers(Player player) {
        if (!enabled || player == null) return;
        
        // Calcular y aplicar los modificadores
        int capacityModifier = calculateStaminaCapacityModifier(player);
        double recoveryModifier = calculateStaminaRecoveryModifier(player);
        
        // Aplicar los modificadores al sistema de estamina
        staminaSystem.setMaxStaminaModifier(player, capacityModifier);
        staminaSystem.setRecoveryRateModifier(player, recoveryModifier);
    }
    
    /**
     * Obtiene la API de AuraSkills
     * @return API de AuraSkills o null si no está disponible
     */
    public AuraSkillsApi getAuraSkillsApi() {
        return auraSkillsApi;
    }
    
    /**
     * Obtiene el stat de resistencia personalizado
     * @return Stat de resistencia o null si no está inicializado
     */
    public CustomStat getEnduranceStat() {
        return this.enduranceStat;
    }
    
    /**
     * Obtiene el trait de capacidad de estamina
     * @return Trait de capacidad de estamina o null si no está inicializado
     */
    public CustomTrait getStaminaCapacityTrait() {
        return this.staminaCapacityTrait;
    }
    
    /**
     * Obtiene el trait de recuperación de estamina
     * @return Trait de recuperación de estamina o null si no está inicializado
     */
    public CustomTrait getStaminaRecoveryTrait() {
        return this.staminaRecoveryTrait;
    }
}