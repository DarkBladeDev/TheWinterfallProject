package com.darkbladedev.mechanics.auraskills.stats;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.mechanics.StaminaSystem;
import com.darkbladedev.mechanics.auraskills.traits.StaminaTraitHandler;
import com.darkbladedev.utils.AuraSkillsUtil;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
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
        
        // Registrar comando para verificar traits de jugadores
        plugin.getServer().getCommandMap().register("savagefrontier", plugin.getCommand("check-stamina-traits"));

        plugin.getCommand("checkstaminatraits").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("savage.admin")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>No tienes permiso para usar este comando"));
                return true;
            }
            
            if (!isEnabled()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>La integración con AuraSkills no está habilitada"));
                return true;
            }
            
            // Verificar todos los jugadores en línea
            if (args.length > 0 && args[0].equalsIgnoreCase("all")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Verificando traits de estamina para todos los jugadores en línea..."));
                
                int totalPlayers = 0;
                int playersWithIssues = 0;
                int playersFixed = 0;
                int playersWithErrors = 0;
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    totalPlayers++;
                    try {
                        boolean traitsOk = verifyPlayerTraits(player);
                        
                        if (!traitsOk) {
                            playersWithIssues++;
                            try {
                                updatePlayerModifiers(player);
                                
                                // Verificar nuevamente después de actualizar
                                boolean updatedTraitsOk = verifyPlayerTraits(player);
                                if (updatedTraitsOk) {
                                    playersFixed++;
                                }
                            } catch (Exception e) {
                                playersWithErrors++;
                                if (plugin.isDebugMode()) {
                                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                        plugin.PREFIX + " <red>Error al actualizar modificadores para " + player.getName() + ": " + e.getMessage()
                                    ));
                                }
                            }
                        }
                    } catch (Exception e) {
                        playersWithErrors++;
                        if (plugin.isDebugMode()) {
                            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                plugin.PREFIX + " <red>Error al verificar traits para " + player.getName() + ": " + e.getMessage()
                            ));
                        }
                    }
                }
                
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Verificación completada:"));
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>- Total de jugadores: " + totalPlayers));
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>- Jugadores con problemas: " + playersWithIssues));
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>- Jugadores corregidos: " + playersFixed));
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>- Errores: " + playersWithErrors));
                
                return true;
            }
            
            // Verificar un jugador específico
            if (args.length < 1) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Uso: /checkstaminatraits <jugador|all>"));
                return true;
            }
            
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Jugador no encontrado: " + args[0]));
                return true;
            }
            
            boolean traitsOk = verifyPlayerTraits(target);
            
            if (traitsOk) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Los traits de estamina para " + target.getName() + " están aplicados correctamente"));
            } else {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Los traits de estamina para " + target.getName() + " no están aplicados correctamente"));
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Intentando actualizar modificadores..."));
                
                try {
                    updatePlayerModifiers(target);
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Modificadores actualizados para " + target.getName()));
                    
                    // Verificar nuevamente después de actualizar
                    boolean updatedTraitsOk = verifyPlayerTraits(target);
                    if (updatedTraitsOk) {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Los traits ahora están aplicados correctamente"));
                    } else {
                        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Los traits siguen sin aplicarse correctamente después de la actualización"));
                    }
                } catch (Exception e) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al actualizar modificadores: " + e.getMessage()));
                }
            }
            
            return true;
        });
        
        // Si ya está inicializado, no hacer nada
        if (this.enabled) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>La integración de estamina con AuraSkills ya está inicializada."));
            return;
        }
        
        // Verificar si AuraSkills está presente
        if (Bukkit.getPluginManager().getPlugin("AuraSkills") == null) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>AuraSkills no encontrado. La integración de estamina no estará disponible."));
            return;
        }
        
        try {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Inicializando integración de estamina con AuraSkills..."));
            
            // Verificar que la API de AuraSkills esté disponible
            if (auraSkillsApi == null) {
                throw new IllegalStateException("La API de AuraSkills no está disponible");
            }
            
            // Obtener las instancias de traits y stats desde CustomTraits y CustomStats
            if (plugin.getCustomTraits() != null && plugin.getCustomStats() != null) {
                // Verificar que CustomStats y CustomTraits estén inicializados
                if (!plugin.getCustomTraits().isInitialized()) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>CustomTraits no está inicializado. Inicializando..."));
                    plugin.getCustomTraits().initialize();
                }
                
                // Obtener stats y traits
                this.enduranceStat = plugin.getCustomStats().getStat("endurance");
                this.staminaCapacityTrait = plugin.getCustomTraits().getStaminaCapacityTrait();
                this.staminaRecoveryTrait = plugin.getCustomTraits().getStaminaRecoveryTrait();
                
                // Verificar que se hayan obtenido correctamente
                boolean hasErrors = false;
                
                if (this.staminaCapacityTrait == null) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error: staminaCapacityTrait es null"));
                    hasErrors = true;
                }
                if (this.staminaRecoveryTrait == null) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error: staminaRecoveryTrait es null"));
                    hasErrors = true;
                }
                if (this.enduranceStat == null) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error: enduranceStat es null"));
                    hasErrors = true;
                }
                
                if (hasErrors) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Intentando usar método alternativo para obtener traits y stats..."));
                    // Intentar usar el método alternativo
                    try {
                        Object[] staminaObjects = AuraSkillsUtil.createStaminaTraitsAndStat();
                        this.staminaCapacityTrait = (CustomTrait) staminaObjects[0];
                        this.staminaRecoveryTrait = (CustomTrait) staminaObjects[1];
                        this.enduranceStat = (CustomStat) staminaObjects[2];
                        NamespacedRegistry registry = this.auraSkillsApi.useRegistry("savage-frontier", new File(plugin.getDataFolder(), "auraskills"));
                        AuraSkillsUtil.registerStaminaTraitsAndStat(registry, staminaCapacityTrait, staminaRecoveryTrait, enduranceStat);
                        
                        // Verificar nuevamente
                        hasErrors = (this.staminaCapacityTrait == null || this.staminaRecoveryTrait == null || this.enduranceStat == null);
                        
                        if (!hasErrors) {
                            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Traits y stats creados y registrados correctamente mediante método alternativo"));
                        } else {
                            throw new IllegalStateException("No se pudieron crear los traits y stats mediante método alternativo");
                        }
                    } catch (Exception e) {
                        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al usar método alternativo: " + e.getMessage()));
                        throw e; // Re-lanzar para que se maneje en el catch exterior
                    }
                } else {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Traits y stats de AuraSkills obtenidos correctamente"));
                }
            } else {
                // Si CustomTraits o CustomStats no están disponibles, usar el método antiguo como fallback
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>CustomTraits o CustomStats no disponibles. Usando método alternativo."));
                
                Object[] staminaObjects = AuraSkillsUtil.createStaminaTraitsAndStat();
                this.staminaCapacityTrait = (CustomTrait) staminaObjects[0];
                this.staminaRecoveryTrait = (CustomTrait) staminaObjects[1];
                this.enduranceStat = (CustomStat) staminaObjects[2];
                NamespacedRegistry registry = this.auraSkillsApi.useRegistry("savage-frontier", new File(plugin.getDataFolder(), "auraskills"));
                AuraSkillsUtil.registerStaminaTraitsAndStat(registry, staminaCapacityTrait, staminaRecoveryTrait, enduranceStat);
                
                if (this.staminaCapacityTrait == null || this.staminaRecoveryTrait == null || this.enduranceStat == null) {
                    throw new IllegalStateException("No se pudieron crear los traits y stats mediante método alternativo");
                }
                
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Traits y stats creados y registrados correctamente mediante método alternativo"));
            }
            
            // Crear y registrar el manejador de traits
            this.traitHandler = new StaminaTraitHandler(auraSkillsApi, staminaSystem);
            
            // Registrar eventos
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getServer().getPluginManager().registerEvents(traitHandler, plugin);
            this.enabled = true;
            
            // Verificar que los traits estén registrados correctamente
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Verificando registro de traits de estamina..."));
            boolean traitsRegistered = verifyStaminaTraitsRegistration();
            
            if (!traitsRegistered) {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Advertencia: Algunos traits de estamina no están registrados correctamente. Intentando registrar nuevamente..."));
                
                // Intentar registrar nuevamente los traits
                try {
                    retryTraitsRegistration();
                    
                    // Verificar nuevamente
                    traitsRegistered = verifyStaminaTraitsRegistration();
                    if (!traitsRegistered) {
                        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>No se pudieron registrar los traits correctamente después de reintentar."));
                    } else {
                        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Traits registrados correctamente después de reintentar."));
                    }
                } catch (Exception e) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al reintentar registro de traits: " + e.getMessage()));
                    if (plugin.isDebugMode()) {
                        e.printStackTrace();
                    }
                }
            } else {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Todos los traits de estamina están registrados correctamente."));
            }
            
            // Actualizar modificadores para todos los jugadores en línea con un retraso mayor
            // para asegurar que AuraSkills haya cargado completamente los datos de los jugadores
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!enabled) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>La integración con AuraSkills se ha deshabilitado. No se actualizarán los modificadores."));
                    return;
                }
                
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Actualizando modificadores de estamina para todos los jugadores..."));
                int playersUpdated = 0;
                int playersWithErrors = 0;
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        updatePlayerModifiers(player);
                        playersUpdated++;
                        
                        if (plugin.isDebugMode()) {
                            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                plugin.PREFIX + " <green>Modificadores actualizados para " + player.getName() + 
                                ": Endurance=" + getEnduranceLevel(player) + 
                                ", Capacidad=" + staminaSystem.getMaxStaminaModifier(player, "auraskills") + 
                                ", Recuperación=" + staminaSystem.getRecoveryRateModifier(player, "auraskills")
                            ));
                        }
                    } catch (Exception e) {
                        playersWithErrors++;
                        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                            plugin.PREFIX + " <red>Error al actualizar modificadores para " + player.getName() + ": " + e.getMessage()
                        ));
                        if (plugin.isDebugMode()) {
                            e.printStackTrace();
                        }
                    }
                }
                
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.PREFIX + " <green>Modificadores de estamina actualizados: " + 
                    playersUpdated + " jugadores actualizados, " + 
                    playersWithErrors + " errores"
                ));
            }, 60L); // Retrasar 3 segundos para asegurar que todo esté cargado
            
            // Configurar tarea periódica para verificar traits de estamina
            int verifyInterval = plugin.getConfig().getInt("mechanics.stamina.auraskills_integration.verify-traits-interval", 60);
            if (verifyInterval > 0) {
                // Convertir minutos a ticks (20 ticks = 1 segundo, 1200 ticks = 1 minuto)
                long intervalTicks = verifyInterval * 1200L;
                
                Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if (!enabled) return;
                    
                    if (plugin.isDebugMode()) {
                        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                            plugin.PREFIX + " <yellow>Verificando periódicamente traits de estamina para todos los jugadores en línea..."
                        ));
                    }
                    
                    int totalPlayers = 0;
                    int playersWithIssues = 0;
                    int playersFixed = 0;
                    int playersWithErrors = 0;
                    
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        totalPlayers++;
                        try {
                            boolean traitsOk = verifyPlayerTraits(player);
                            
                            if (!traitsOk) {
                                playersWithIssues++;
                                try {
                                    updatePlayerModifiers(player);
                                    
                                    // Verificar nuevamente después de actualizar
                                    boolean updatedTraitsOk = verifyPlayerTraits(player);
                                    if (updatedTraitsOk) {
                                        playersFixed++;
                                    }
                                } catch (Exception e) {
                                    playersWithErrors++;
                                    if (plugin.isDebugMode()) {
                                        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                            plugin.PREFIX + " <red>Error al actualizar modificadores para " + player.getName() + ": " + e.getMessage()
                                        ));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            playersWithErrors++;
                            if (plugin.isDebugMode()) {
                                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                    plugin.PREFIX + " <red>Error al verificar traits para " + player.getName() + ": " + e.getMessage()
                                ));
                            }
                        }
                    }
                    
                    if (plugin.isDebugMode() || playersWithIssues > 0) {
                        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                            plugin.PREFIX + " <green>Verificación periódica completada: " +
                            totalPlayers + " jugadores, " +
                            playersWithIssues + " con problemas, " +
                            playersFixed + " corregidos, " +
                            playersWithErrors + " errores"
                        ));
                    }
                }, 1200L, intervalTicks); // Iniciar después de 1 minuto y repetir según el intervalo configurado
                
                if (plugin.isDebugMode()) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                        plugin.PREFIX + " <green>Programada verificación periódica de traits cada " + verifyInterval + " minutos"
                    ));
                }
            }
            
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al inicializar la integración con AuraSkills (StaminaSystem): " + e.getMessage()));
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
            this.enabled = false;
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
     * Obtiene el modificador de capacidad de estamina para un jugador directamente desde AuraSkills
     * @param player Jugador
     * @return Valor del modificador de capacidad de estamina
     */
    public double getStaminaCapacityModifier(Player player) {
        if (!enabled || auraSkillsApi == null || player == null) return 0.0;
        try {
            SkillsUser user = auraSkillsApi.getUserManager().getUser(player.getUniqueId());
            if (user == null) return 0.0;
            
            // Usar el ID completo con namespace para evitar errores
            return user.getTraitModifier("savage-frontier:stamina_capacity").value();
        } catch (Exception e) {
            if (plugin.isDebugMode()) {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.PREFIX + " <red>Error al obtener modificador de capacidad de estamina para " + player.getName() + ": " + e.getMessage()
                ));
            }
            return 0.0;
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
     * Obtiene el modificador de recuperación de estamina para un jugador directamente desde AuraSkills
     * @param player Jugador
     * @return Valor del modificador de recuperación de estamina
     */
    public double getStaminaRecoveryModifier(Player player) {
        if (!enabled || auraSkillsApi == null || player == null) return 0.0;
        try {
            SkillsUser user = auraSkillsApi.getUserManager().getUser(player.getUniqueId());
            if (user == null) return 0.0;
            
            // Usar el ID completo con namespace para evitar errores
            return user.getTraitModifier("savage-frontier:stamina_recovery").value();
        } catch (Exception e) {
            if (plugin.isDebugMode()) {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.PREFIX + " <red>Error al obtener modificador de recuperación de estamina para " + player.getName() + ": " + e.getMessage()
                ));
            }
            return 0.0;
        }
    }
    
    /**
     * Actualiza los modificadores de estamina para un jugador
     * @param player Jugador a actualizar
     */
    public void updatePlayerModifiers(Player player) {
        if (!enabled || player == null) return;
        
        try {
            // Intentar obtener los modificadores directamente desde AuraSkills
            double capacityModifierDirect = getStaminaCapacityModifier(player);
            double recoveryModifierDirect = getStaminaRecoveryModifier(player);
            
            // Si no se pueden obtener directamente, calcularlos basados en el nivel de resistencia
            int capacityModifier = (capacityModifierDirect > 0) ? 
                    (int)Math.floor(capacityModifierDirect) : 
                    calculateStaminaCapacityModifier(player);
                    
            double recoveryModifier = (recoveryModifierDirect > 0) ? 
                    recoveryModifierDirect : 
                    calculateStaminaRecoveryModifier(player);
            
            // Aplicar los modificadores al sistema de estamina con el origen "auraskills"
            staminaSystem.setMaxStaminaModifier(player, "auraskills", capacityModifier);
            staminaSystem.setRecoveryRateModifier(player, "auraskills", recoveryModifier);
            
            // Registrar en la consola para depuración
            if (plugin.isDebugMode()) {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.PREFIX + " <green>Aplicados modificadores de estamina para " + player.getName() + ": " +
                    "Capacidad +" + capacityModifier + " (directo: " + capacityModifierDirect + "), " +
                    "Recuperación +" + recoveryModifier + " (directo: " + recoveryModifierDirect + ")"
                ));
            }
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.PREFIX + " <red>Error al actualizar modificadores de estamina para " + player.getName() + ": " + e.getMessage()
            ));
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
            
            // Intentar limpiar los modificadores para evitar valores incorrectos
            try {
                staminaSystem.setMaxStaminaModifier(player, "auraskills", 0);
                staminaSystem.setRecoveryRateModifier(player, "auraskills", 0);
            } catch (Exception ex) {
                // Ignorar errores al limpiar modificadores
                if (plugin.isDebugMode()) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                        plugin.PREFIX + " <red>No se pudieron limpiar los modificadores para " + player.getName()
                    ));
                }
            }
        }
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
    
    /**
     * Verifica si un jugador tiene los traits de estamina aplicados correctamente
     * @param player Jugador a verificar
     * @return true si los traits están aplicados correctamente, false en caso contrario
     */
    public boolean verifyPlayerTraits(Player player) {
        if (!enabled || auraSkillsApi == null || player == null) {
            return false;
        }
        
        try {
            SkillsUser user = auraSkillsApi.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.PREFIX + " <red>No se pudo obtener el usuario de AuraSkills para " + player.getName()
                ));
                return false;
            }
            
            // Obtener los modificadores de estamina directamente desde AuraSkills
            double capacityModifier = user.getTraitModifier("savage-frontier:stamina_capacity").value();
            double recoveryModifier = user.getTraitModifier("savage-frontier:stamina_recovery").value();
            
            // Obtener el nivel de resistencia personalizado
            int enduranceLevel = (int) user.getStatLevel(this.enduranceStat);
            
            // Verificar si los modificadores están aplicados en el sistema de estamina
            double appliedCapacity = staminaSystem.getMaxStaminaModifier(player, "auraskills");
            double appliedRecovery = staminaSystem.getRecoveryRateModifier(player, "auraskills");
            
            StringBuilder statusMessage = new StringBuilder();
            statusMessage.append(plugin.PREFIX).append(" <yellow>Estado de traits para ").append(player.getName()).append(":\n");
            statusMessage.append("  <gray>- <green>Nivel de resistencia: ").append(enduranceLevel).append("\n");
            statusMessage.append("      <gray>- <white>Modificador de capacidad <gray>(AuraSkills): ").append(capacityModifier).append("\n");
            statusMessage.append("      <gray>- <white>Modificador de recuperación <gray>(AuraSkills): ").append(recoveryModifier).append("\n");
            statusMessage.append("      <gray>- <white>Modificador de capacidad <gray>(Savage-Frontier): ").append(appliedCapacity).append("\n");
            statusMessage.append("      <gray>- <white>Modificador de recuperación <gray>(Savage-Frontier): ").append(appliedRecovery);
            
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(statusMessage.toString()));
            
            // Verificar si hay discrepancias significativas
            boolean capacityMatch = (Math.abs(capacityModifier - appliedCapacity) < 0.1) || 
                                   (enduranceLevel > 0 && appliedCapacity > 0);
            boolean recoveryMatch = (Math.abs(recoveryModifier - appliedRecovery) < 0.01) || 
                                   (enduranceLevel > 0 && appliedRecovery > 0);
            
            return capacityMatch && recoveryMatch;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.PREFIX + " <red>Error al verificar traits para " + player.getName() + ": " + e.getMessage()
            ));
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
            return false;
        }
    }
    
    /**
     * Verifica si los traits de estamina están registrados correctamente en AuraSkills
     * @return true si los traits están registrados, false en caso contrario
     */
    public boolean verifyStaminaTraitsRegistration() {
        if (!enabled || auraSkillsApi == null) {
            return false;
        }
        
        boolean allRegistered = true;
        StringBuilder statusMessage = new StringBuilder();
        statusMessage.append(plugin.PREFIX).append(" <yellow>Estado de registro de traits de estamina:\n");
        
        try {
            // Verificar si los traits están registrados en AuraSkills
            boolean capacityRegistered = auraSkillsApi.getGlobalRegistry().getTrait(NamespacedId.of("savage-frontier", "stamina_capacity")).isEnabled();
            boolean recoveryRegistered = auraSkillsApi.getGlobalRegistry().getTrait(NamespacedId.of("savage-frontier", "stamina_recovery")).isEnabled();
            boolean enduranceRegistered = auraSkillsApi.getGlobalRegistry().getStat(NamespacedId.of("savage-frontier", "endurance")).isEnabled();
            
            statusMessage.append("  - Trait de capacidad: ").append(capacityRegistered ? "<green>Registrado" : "<red>No registrado").append("\n");
            statusMessage.append("  - Trait de recuperación: ").append(recoveryRegistered ? "<green>Registrado" : "<red>No registrado").append("\n");
            statusMessage.append("  - Stat de resistencia: ").append(enduranceRegistered ? "<green>Registrado" : "<red>No registrado").append("\n");
            
            allRegistered = capacityRegistered && recoveryRegistered && enduranceRegistered;
            
            // Verificar si las instancias locales son correctas
            statusMessage.append("  - Instancia de capacidad: ").append(staminaCapacityTrait != null ? "<green>Disponible" : "<red>No disponible").append("\n");
            statusMessage.append("  - Instancia de recuperación: ").append(staminaRecoveryTrait != null ? "<green>Disponible" : "<red>No disponible").append("\n");
            statusMessage.append("  - Instancia de resistencia: ").append(enduranceStat != null ? "<green>Disponible" : "<red>No disponible");
            
            allRegistered = allRegistered && staminaCapacityTrait != null && staminaRecoveryTrait != null && enduranceStat != null;
            
            // Mostrar el mensaje de estado
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(statusMessage.toString()));
            
            return allRegistered;
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.PREFIX + " <red>Error al verificar registro de traits: " + e.getMessage()
            ));
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
            return false;
        }
    }
    
    /**
     * Reintenta el registro de traits de estamina en caso de que no estén registrados correctamente
     * @throws Exception Si ocurre un error durante el registro
     */
    public void retryTraitsRegistration() throws Exception {
        if (!enabled || auraSkillsApi == null) {
            throw new IllegalStateException("La integración con AuraSkills no está habilitada o la API no está disponible");
        }
        
        Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Reintentando registro de traits de estamina..."));
        
        try {
            // Crear nuevas instancias de traits y stats
            Object[] staminaObjects = AuraSkillsUtil.createStaminaTraitsAndStat();
            this.staminaCapacityTrait = (CustomTrait) staminaObjects[0];
            this.staminaRecoveryTrait = (CustomTrait) staminaObjects[1];
            this.enduranceStat = (CustomStat) staminaObjects[2];
            
            // Obtener el registro con namespace
            NamespacedRegistry registry = this.auraSkillsApi.useRegistry("savage-frontier", new File(plugin.getDataFolder(), "auraskills"));
            
            // Intentar desregistrar los traits existentes si es posible
            try {
                if (auraSkillsApi.getGlobalRegistry().getTrait(NamespacedId.of("savage-frontier" , "stamina_capacity")).isEnabled()) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Desregistrando trait de capacidad existente..."));
                    // No hay método directo para desregistrar, pero podemos intentar sobrescribir
                }
                
                if (auraSkillsApi.getGlobalRegistry().getTrait(NamespacedId.of("savage-frontier", "stamina_recovery")).isEnabled()) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Desregistrando trait de recuperación existente..."));
                    // No hay método directo para desregistrar, pero podemos intentar sobrescribir
                }
                
                if (auraSkillsApi.getGlobalRegistry().getStat(NamespacedId.of("savage-frontier", "endurance")).isEnabled()) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Desregistrando stat de resistencia existente..."));
                    // No hay método directo para desregistrar, pero podemos intentar sobrescribir
                }
            } catch (Exception e) {
                // Ignorar errores al desregistrar, continuamos con el registro
                if (plugin.isDebugMode()) {
                    Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al desregistrar traits existentes: " + e.getMessage()));
                }
            }
            
            // Registrar los traits y stats
            AuraSkillsUtil.registerStaminaTraitsAndStat(registry, staminaCapacityTrait, staminaRecoveryTrait, enduranceStat);
            
            // Verificar que se hayan registrado correctamente
            if (staminaCapacityTrait == null || staminaRecoveryTrait == null || enduranceStat == null) {
                throw new IllegalStateException("No se pudieron crear los traits y stats");
            }
            
            // Actualizar el manejador de traits
            if (this.traitHandler != null) {
                // Desregistrar el manejador anterior si existe
                try {
                    //auraSkillsApi.getHandlers().unregisterHandler(traitHandler);
                } catch (Exception e) {
                    // Ignorar errores al desregistrar
                }
            }
            
            // Crear y registrar un nuevo manejador de traits
            this.traitHandler = new StaminaTraitHandler(auraSkillsApi, staminaSystem);
            auraSkillsApi.getHandlers().registerTraitHandler(traitHandler);
            
            // Actualizar modificadores para todos los jugadores en línea
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                int playersUpdated = 0;
                int playersWithErrors = 0;
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        updatePlayerModifiers(player);
                        playersUpdated++;
                        
                        if (plugin.isDebugMode()) {
                            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                plugin.PREFIX + " <green>Modificadores actualizados después de reregistro para " + player.getName() + 
                                ": Endurance=" + getEnduranceLevel(player) + 
                                ", Capacidad=" + staminaSystem.getMaxStaminaModifier(player, "auraskills") + 
                                ", Recuperación=" + staminaSystem.getRecoveryRateModifier(player, "auraskills")
                            ));
                        }
                    } catch (Exception e) {
                        playersWithErrors++;
                        if (plugin.isDebugMode()) {
                            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                plugin.PREFIX + " <red>Error al actualizar modificadores después de reregistro para " + player.getName() + ": " + e.getMessage()
                            ));
                        }
                    }
                }
                
                Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.PREFIX + " <green>Modificadores de estamina actualizados después de reregistro: " + 
                    playersUpdated + " jugadores actualizados, " + 
                    playersWithErrors + " errores"
                ));
            }, 20L); // Retrasar 1 segundo para asegurar que todo esté registrado
            
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Traits y stats recreados y registrados correctamente"));
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al reintentar registro de traits: " + e.getMessage()));
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
            throw e; // Re-lanzar para que se maneje en el nivel superior
        }
    }
}