package com.darkbladedev.mechanics.auraskills.traits;

import com.darkbladedev.mechanics.StaminaSystem;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.bukkit.BukkitTraitHandler;
import dev.aurelium.auraskills.api.event.skill.SkillLevelUpEvent;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.api.user.SkillsUser;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Manejador de traits para la integración de estamina con AuraSkills
 * Implementa la lógica para aplicar los efectos de los traits de estamina
 */
public class StaminaTraitHandler implements BukkitTraitHandler, Listener {

    private final AuraSkillsApi auraSkillsApi;
    private final StaminaSystem staminaSystem;
    
    /**
     * Constructor del manejador de traits
     * @param auraSkillsApi API de AuraSkills
     * @param staminaSystem Sistema de estamina
     */
    public StaminaTraitHandler(AuraSkillsApi auraSkillsApi, StaminaSystem staminaSystem) {
        this.auraSkillsApi = auraSkillsApi;
        this.staminaSystem = staminaSystem;
        
        // Registrar este manejador en AuraSkills
        auraSkillsApi.getHandlers().registerTraitHandler(this);
    }
    
    /**
     * Maneja el evento de unión de un jugador para aplicar los modificadores de estamina
     * @param event Evento de unión
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updatePlayerStaminaModifiers(player);
    }
    
    /**
     * Maneja el evento de cambio de nivel de trait
     * @param event Evento de cambio de nivel de trait
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTraitLevelChange(SkillLevelUpEvent event) {
        Player player = event.getPlayer();
        
        if (player != null && player.isOnline()) {
            // Actualizar los modificadores cuando cambia cualquier trait
            updatePlayerStaminaModifiers(player);
        }
    }
    
    /**
     * Actualiza los modificadores de estamina para un jugador
     * @param player Jugador a actualizar
     */
    public void updatePlayerStaminaModifiers(Player player) {
        if (player == null || !player.isOnline()) return;
        
        try {
            SkillsUser user = auraSkillsApi.getUser(player.getUniqueId());
            if (user == null) return;
            
            // Obtener el nivel de los traits para el jugador
            double staminaCapacityLevel = user.getEffectiveTraitLevel(user.getTraitModifier("savagefrontier:stamina_capacity").trait());
            double staminaRecoveryLevel = user.getEffectiveTraitLevel(user.getTraitModifier("savagefrontier:stamina_recovery").trait());
            
            // Calcular los modificadores basados en los niveles de los traits
            int maxStaminaModifier = (int) Math.floor(staminaCapacityLevel);
            double recoveryRateModifier = staminaRecoveryLevel * 0.01; // Convertir a porcentaje (0.01 = 1%)
            
            // Aplicar los modificadores al sistema de estamina
            // Nota: Estos métodos deben ser implementados en StaminaSystem
            applyStaminaModifiers(player, maxStaminaModifier, recoveryRateModifier);
        } catch (Exception e) {
            // Manejar silenciosamente cualquier error para evitar problemas en el servidor
        }
    }
    
    /**
     * Aplica los modificadores de estamina a un jugador
     * @param player Jugador a modificar
     * @param maxStaminaModifier Modificador de estamina máxima
     * @param recoveryRateModifier Modificador de tasa de recuperación
     */
    private void applyStaminaModifiers(Player player, int maxStaminaModifier, double recoveryRateModifier) {
        // Aplicar modificador de estamina máxima
        staminaSystem.setMaxStaminaModifier(player, "auraskills", maxStaminaModifier);
        
        // Aplicar modificador de tasa de recuperación
        staminaSystem.setRecoveryRateModifier(player, "auraskills", recoveryRateModifier);
    }
    
    /**
     * Método requerido por la interfaz TraitHandler
     * Se llama cuando se aplican los traits a un jugador
     * @param user Usuario de AuraSkills
     */
    public void applyTraits(SkillsUser user) {
        Player player = Bukkit.getPlayer(user.getUuid());
        if (player != null && player.isOnline()) {
            updatePlayerStaminaModifiers(player);
        }
    }

    @Override
    public Trait[] getTraits() {
        return new Trait[] {
            auraSkillsApi.getGlobalRegistry().getTrait(NamespacedId.of("savage-frontier", "stamina_capacity")),
            auraSkillsApi.getGlobalRegistry().getTrait(NamespacedId.of("savage-frontier", "stamina_recovery"))
        };
    }

    @Override
    public double getBaseLevel(Player player, Trait trait) {
        return 0;
    }

    @Override
    public void onReload(Player player, SkillsUser user, Trait trait) {
        return; // No es necesario implementar lógica de recarga para traits de estamina
    }
}