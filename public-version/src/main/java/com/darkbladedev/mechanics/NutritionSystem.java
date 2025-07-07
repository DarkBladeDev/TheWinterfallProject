package com.darkbladedev.mechanics;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.CustomTypes.CustomDamageTypes;
import com.darkbladedev.mechanics.auraskills.skilltrees.SkillTreeManager;
import com.darkbladedev.utils.AuraSkillsUtil;
import com.darkbladedev.mechanics.auraskills.skills.nutrition.EfficientEaterSkill;
import com.darkbladedev.mechanics.auraskills.skills.nutrition.MetabolicBoostSkill;
import com.darkbladedev.mechanics.auraskills.skills.nutrition.IronStomachSkill;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.stat.CustomStat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sistema avanzado de nutrición que diferencia los alimentos según su aporte nutricional
 * Maneja cuatro tipos de nutrientes: proteínas, grasas, carbohidratos y vitaminas
 */
public class NutritionSystem implements Listener {

    private final SavageFrontierMain plugin;
    private BukkitTask nutritionTask;
    private final Map<UUID, Map<NutrientType, Integer>> playerNutrients;
    private boolean isActive;
    
    // Variables configurables
    private int MAX_NUTRIENT_LEVEL = 100;
    private int DEFAULT_NUTRIENT_LEVEL = 70;
    private int CRITICAL_NUTRIENT_LEVEL = 20;
    private long UPDATE_INTERVAL = 100L; // Intervalo de actualización en ticks
    
    // Factores de disminución (configurables)
    private double normalDecreaseRate = 0.05; // Probabilidad base de disminución (5%)
    private double activityDecreaseRate = 0.15; // Probabilidad de disminución durante actividad (15%)
    
    // Stat de nutrición personalizado (debe inicializarse igual que en StaminaSystemExpansion)
    private CustomStat nutritionStat;
    @SuppressWarnings("unused")
	private void ensureHydrationStatLoaded() {
        if (nutritionStat == null) {
            try {
                AuraSkillsApi api = AuraSkillsApi.get();
                var registry = api.getGlobalRegistry();
                nutritionStat = (CustomStat) registry.getStat(NamespacedId.of("savage-frontier", "nutrition"));
            } catch (Exception ignored) {}
        }
    }
    
    /**
     * Tipos de nutrientes que maneja el sistema
     */
    public enum NutrientType {
        PROTEIN("<red>", "Proteínas"),
        FAT("<yellow>", "Grasas"),
        CARBS("<gold>", "Carbohidratos"),
        VITAMINS("<green>", "Vitaminas");
        
        private final String colorCode;
        private final String displayName;
        
        NutrientType(String colorCode, String displayName) {
            this.colorCode = colorCode;
            this.displayName = displayName;
        }
        
        public String getColorCode() {
            return colorCode;
        }
        
        public Component getColor() {
            return MiniMessage.miniMessage().deserialize(colorCode);
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Definición de valores nutricionales para cada tipo de alimento
     */
    private final Map<Material, Map<NutrientType, Integer>> foodNutrients;
    
    /**
     * Constructor del sistema de nutrición
     * @param plugin Instancia del plugin principal
     */
    public NutritionSystem(SavageFrontierMain plugin) {
        this.plugin = plugin;
        this.playerNutrients = new HashMap<>();
        this.foodNutrients = new HashMap<>();
        this.isActive = false;
        
        // Cargar configuración
        loadConfig();
        
        // Inicializar valores nutricionales de alimentos
        initFoodNutrients();
    }
    
    /**
     * Carga la configuración desde config.yml
     */
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        
        // Cargar valores de configuración
        MAX_NUTRIENT_LEVEL = config.getInt("nutrition.max_nutrient_level", 100);
        DEFAULT_NUTRIENT_LEVEL = config.getInt("nutrition.default_nutrient_level", 70);
        CRITICAL_NUTRIENT_LEVEL = config.getInt("nutrition.critical_nutrient_level", 20);
        normalDecreaseRate = config.getDouble("nutrition.normal_decrease_rate", 0.05);
        activityDecreaseRate = config.getDouble("nutrition.activity_decrease_rate", 0.15);
        UPDATE_INTERVAL = config.getLong("nutrition.update_interval", 100L);
    }
    
    /**
     * Inicializa los valores nutricionales para cada tipo de alimento
     */
    private void initFoodNutrients() {
        // Carnes - Altas en proteínas
        addFoodNutrient(Material.COOKED_BEEF, 20, 10, 0, 5);
        addFoodNutrient(Material.COOKED_PORKCHOP, 18, 12, 0, 5);
        addFoodNutrient(Material.COOKED_MUTTON, 16, 10, 0, 4);
        addFoodNutrient(Material.COOKED_CHICKEN, 15, 8, 0, 5);
        addFoodNutrient(Material.COOKED_RABBIT, 18, 5, 0, 6);
        
        // Pescados - Proteínas y grasas saludables
        addFoodNutrient(Material.COOKED_COD, 15, 8, 0, 10);
        addFoodNutrient(Material.COOKED_SALMON, 16, 10, 0, 12);
        
        // Vegetales y frutas - Carbohidratos y vitaminas
        addFoodNutrient(Material.CARROT, 0, 0, 10, 15);
        addFoodNutrient(Material.POTATO, 0, 0, 15, 5);
        addFoodNutrient(Material.BAKED_POTATO, 2, 2, 20, 8);
        addFoodNutrient(Material.BEETROOT, 0, 0, 8, 12);
        addFoodNutrient(Material.APPLE, 0, 0, 12, 15);
        addFoodNutrient(Material.MELON_SLICE, 0, 0, 8, 10);
        addFoodNutrient(Material.SWEET_BERRIES, 0, 0, 5, 12);
        addFoodNutrient(Material.GLOW_BERRIES, 0, 0, 5, 14);
        
        // Panes y dulces - Altos en carbohidratos
        addFoodNutrient(Material.BREAD, 2, 1, 20, 2);
        addFoodNutrient(Material.COOKIE, 0, 5, 15, 0);
        addFoodNutrient(Material.CAKE, 2, 10, 25, 0);
        
        // Alimentos compuestos
        addFoodNutrient(Material.MUSHROOM_STEW, 5, 3, 10, 15);
        addFoodNutrient(Material.RABBIT_STEW, 15, 8, 12, 18);
        addFoodNutrient(Material.BEETROOT_SOUP, 2, 1, 10, 15);
        addFoodNutrient(Material.PUMPKIN_PIE, 3, 8, 20, 5);
        
        // Alimentos especiales
        addFoodNutrient(Material.GOLDEN_APPLE, 5, 5, 15, 30);
        addFoodNutrient(Material.ENCHANTED_GOLDEN_APPLE, 10, 10, 20, 50);
        addFoodNutrient(Material.GOLDEN_CARROT, 2, 2, 15, 25);
    }
    
    /**
     * Agrega valores nutricionales a un alimento
     * @param food Tipo de alimento
     * @param protein Valor de proteínas
     * @param fat Valor de grasas
     * @param carbs Valor de carbohidratos
     * @param vitamins Valor de vitaminas
     */
    private void addFoodNutrient(Material food, int protein, int fat, int carbs, int vitamins) {
        Map<NutrientType, Integer> nutrients = new EnumMap<>(NutrientType.class);
        nutrients.put(NutrientType.PROTEIN, protein);
        nutrients.put(NutrientType.FAT, fat);
        nutrients.put(NutrientType.CARBS, carbs);
        nutrients.put(NutrientType.VITAMINS, vitamins);
        foodNutrients.put(food, nutrients);
    }
    
    /**
     * Inicializa el sistema de nutrición
     */
    public void initialize() {
        // Verificar si el sistema está habilitado en la configuración
        if (!plugin.getConfig().getBoolean("nutrition.enabled", true)) {
            ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <yellow>Sistema de nutrición deshabilitado en la configuración"));
            return;
        }
        
        startNutritionSystem();
        isActive = true;
        
        // Registrar eventos
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Sistema de nutrición inicializado"));
    }
    
    /**
     * Inicia el sistema de nutrición
     */
    private void startNutritionSystem() {
        nutritionTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID playerId = player.getUniqueId();
                    
                    // Inicializar nutrientes si es necesario
                    if (!playerNutrients.containsKey(playerId)) {
                        initializePlayerNutrients(playerId);
                    }
                    
                    // Reducir nutrientes gradualmente
                    Map<NutrientType, Integer> nutrients = playerNutrients.get(playerId);
                    
                    // Verificar si el jugador tiene permiso para bypass de nutrientes o está protegido como nuevo jugador
                    if (!player.hasPermission("savage.bypass.nutrients") && !plugin.isPlayerProtectedFromSystem(player, "nutrition")) {
                        // Aplicar reducción a cada nutriente solo si no tiene el permiso y no está protegido
                        for (NutrientType type : NutrientType.values()) {
                            // Probabilidad de reducción basada en actividad
                            double currentRate = normalDecreaseRate;
                            if (player.isSprinting()) {
                                currentRate = activityDecreaseRate;
                            }
                            
                            // Aplicar reducción según la tasa configurada
                            if (Math.random() < currentRate) {
                                int currentLevel = nutrients.get(type);
                                int newLevel = Math.max(0, currentLevel - 1);
                                nutrients.put(type, newLevel);
                            }
                        }
                    }
                    
                    // Aplicar efectos según niveles nutricionales
                    applyNutritionEffects(player, nutrients);
                    
                    // INTEGRACIÓN AURASKILLS: Habilidades pasivas de nutrición
                    // EfficientEaterSkill: La comida recupera más nutrición (aplicar en evento de consumo)
                    // MetabolicBoostSkill: Recupera estamina más rápido después de comer (aplicar en evento de consumo)
                    // IronStomachSkill: Reduce efectos negativos de comida podrida (aplicar en evento de consumo)
                    // Aquí puedes aplicar efectos pasivos globales si los hubiera
                }
            }
        }.runTaskTimer(plugin, UPDATE_INTERVAL, UPDATE_INTERVAL); // Ejecutar según el intervalo configurado
    }
    
    /**
     * Inicializa los nutrientes de un jugador con valores por defecto
     * @param playerId UUID del jugador
     */
    private void initializePlayerNutrients(UUID playerId) {
        Map<NutrientType, Integer> nutrients = new EnumMap<>(NutrientType.class);
        
        // Establecer valores iniciales
        for (NutrientType type : NutrientType.values()) {
            nutrients.put(type, DEFAULT_NUTRIENT_LEVEL);
        }
        
        playerNutrients.put(playerId, nutrients);
    }
    
    /**
     * Aplica efectos según los niveles nutricionales del jugador
     * @param player Jugador
     * @param nutrients Mapa de nutrientes del jugador
     */
    private void applyNutritionEffects(Player player, Map<NutrientType, Integer> nutrients) {
        // Verificar si el jugador tiene permiso para bypass o está protegido como nuevo jugador
        if (player.hasPermission("savage.bypass.nutrients") || 
            plugin.isPlayerProtectedFromSystem(player, "nutrition")) {
            return; // No aplicar efectos si tiene el permiso o está protegido
        }
        
        // Contar nutrientes críticos
        int criticalCount = 0;
        
        for (NutrientType type : NutrientType.values()) {
            int level = nutrients.get(type);
            
            if (level <= CRITICAL_NUTRIENT_LEVEL) {
                criticalCount++;
                
                // Enviar mensaje de advertencia (con probabilidad para no spamear)
                if (Math.random() < 0.1) {
                    Component message = MiniMessage.miniMessage().deserialize(type.getColorCode() + "Tienes deficiencia de " + type.getDisplayName() + ". Deberías consumir alimentos ricos en este nutriente.");
                    Component actionBarMessage = MiniMessage.miniMessage().deserialize(type.getColorCode() + "Deficiencia de " + type.getDisplayName());
                    ((Audience) player).sendMessage(message);
                    ((Audience) player).sendActionBar(actionBarMessage);
                }
            }
        }
        
        // Aplicar efectos según cantidad de nutrientes críticos
        if (criticalCount >= 3) {
            // Deficiencia severa (3-4 nutrientes críticos)
            plugin.getCustomDebuffEffects().applyWeakness(player);
            //player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 0));
            
            // Daño por desnutrición severa
            if (criticalCount == 4 && Math.random() < 0.2) { // 20% de probabilidad con los 4 nutrientes críticos
                DamageSource damageSource = CustomDamageTypes.DamageSourceBuilder(null, player, CustomDamageTypes.DESNUTRITION_KEY);
                player.damage(1.0, damageSource); // Medio corazón de daño
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<dark_red>¡Estás severamente desnutrido! Necesitas una dieta equilibrada urgentemente."));
                ((Audience) player).sendActionBar(MiniMessage.miniMessage().deserialize("<dark_red>¡Estás severamente desnutrido!"));
            }
        } else if (criticalCount >= 1) {
            // Deficiencia moderada (1-2 nutrientes críticos)
            plugin.getCustomDebuffEffects().applyWeakness(player);
            //player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 80, 0));
        }
        
        // Bonificaciones por buena nutrición (todos los nutrientes por encima del 80%)
        boolean allNutrientsHigh = true;
        for (NutrientType type : NutrientType.values()) {
            if (nutrients.get(type) < 80) {
                allNutrientsHigh = false;
                break;
            }
        }
        
        if (allNutrientsHigh) {
            // Bonificación por dieta equilibrada
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0));
            
            // Mensaje ocasional
            if (Math.random() < 0.05 && plugin.getUserPreferencesManager().hasStatusMessages(player)) { // 5% de probabilidad
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<green>Te sientes lleno de energía gracias a tu dieta equilibrada."));
            }
        }
    }
    
    /**
     * Maneja el evento de consumo de alimentos
     * @param event Evento de consumo
     */
    @SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        int nutritionLevel = AuraSkillsUtil.getCustomStatLevel(player, "nutrition");
        Map<NutrientType, Integer> nutrients = playerNutrients.get(player.getUniqueId());
        ItemStack item = event.getItem();
        Material foodType = item.getType();
        // Verificar si el alimento tiene valores nutricionales definidos
        if (foodNutrients.containsKey(foodType)) {
            UUID playerId = player.getUniqueId();
            if (!playerNutrients.containsKey(playerId)) {
                initializePlayerNutrients(playerId);
            }
            Map<NutrientType, Integer> playerNutrientLevels = playerNutrients.get(playerId);
            Map<NutrientType, Integer> foodNutrientValues = foodNutrients.get(foodType);
            // EfficientEaterSkill: La comida recupera más nutrición
            double nutritionMultiplier = 1.0;
            if (SkillTreeManager.getInstance().hasSkill(player, EfficientEaterSkill.class, java.util.Map.of("nutrition", nutritionLevel))) {
                nutritionMultiplier = 1.5;
            }
            // Aplicar valores nutricionales del alimento
            for (NutrientType type : NutrientType.values()) {
                int currentLevel = playerNutrientLevels.get(type);
                int nutritionValue = foodNutrientValues.get(type);
                int added = (int) Math.round(nutritionValue * nutritionMultiplier);
                playerNutrientLevels.put(type, Math.min(MAX_NUTRIENT_LEVEL, currentLevel + added));
            }
        }
        // IronStomachSkill: Reduce efectos negativos de comida podrida
        if (SkillTreeManager.getInstance().hasSkill(player, IronStomachSkill.class, java.util.Map.of("nutrition", nutritionLevel))) {
            if (item.getType().name().contains("ROTTEN") || item.getType().name().contains("POISON")) {
                player.removePotionEffect(PotionEffectType.POISON);
                player.removePotionEffect(PotionEffectType.HUNGER);
            }
        }
        // MetabolicBoostSkill: Recupera estamina más rápido después de comer
        if (SkillTreeManager.getInstance().hasSkill(player, MetabolicBoostSkill.class, java.util.Map.of("nutrition", nutritionLevel))) {
            plugin.getStaminaSystem().increaseStamina(player, 2);
        }
    }
    
    /**
     * Maneja el evento de muerte de un jugador
     * @param event Evento de muerte
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();
        
        // Eliminar datos de nutrición al morir
        playerNutrients.remove(playerId);
    }
    
    /**
     * Maneja el evento de respawn de un jugador
     * @param event Evento de respawn
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Reiniciar nutrientes con valores por defecto
        initializePlayerNutrients(playerId);
    }
    
    /**
     * Maneja el evento de unión de un jugador
     * @param event Evento de unión
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Inicializar nutrientes si es un jugador nuevo
        if (!playerNutrients.containsKey(playerId)) {
            initializePlayerNutrients(playerId);
        }
    }
    
    /**
     * Obtiene el nivel de un nutriente específico para un jugador
     * @param player Jugador
     * @param type Tipo de nutriente
     * @return Nivel del nutriente (0-100)
     */
    public int getNutrientLevel(Player player, NutrientType type) {
        if (player == null) return 0;
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar nutrientes si es necesario
        if (!playerNutrients.containsKey(playerId)) {
            initializePlayerNutrients(playerId);
        }
        
        return playerNutrients.get(playerId).get(type);
    }
    
    /**
     * Obtiene todos los niveles de nutrientes de un jugador
     * @param player Jugador
     * @return Mapa con los niveles de todos los nutrientes
     */
    public Map<NutrientType, Integer> getAllNutrientLevels(Player player) {
        if (player == null) return null;
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar nutrientes si es necesario
        if (!playerNutrients.containsKey(playerId)) {
            initializePlayerNutrients(playerId);
        }
        
        return new EnumMap<>(playerNutrients.get(playerId));
    }
    
    /**
     * Genera una barra de progreso visual para un nutriente específico
     * @param player Jugador
     * @param type Tipo de nutriente
     * @return Barra de progreso como texto
     */
    public Component getNutrientBar(Player player, NutrientType type) {
        int level = getNutrientLevel(player, type);
        
        StringBuilder bar = new StringBuilder();
        MiniMessage mm = MiniMessage.miniMessage();
        
        // Usar el color específico del nutriente
        String barColor = type.getColorCode();
        bar.append(barColor);
        
        // Construir barra de progreso
        int bars = (int) Math.round(level / 10.0);
        
        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                bar.append("■");
            } else {
                bar.append("▢");
            }
        }
        
        return mm.deserialize(bar.toString());
    }
    
    /**
     * Establece el nivel de un nutriente específico para un jugador
     * @param player Jugador
     * @param type Tipo de nutriente
     * @param level Nivel a establecer (0-100)
     */
    public void setNutrientLevel(Player player, NutrientType type, int level) {
        if (player == null) return;
        
        UUID playerId = player.getUniqueId();
        
        // Inicializar nutrientes si es necesario
        if (!playerNutrients.containsKey(playerId)) {
            initializePlayerNutrients(playerId);
        }
        
        // Asegurar que el nivel esté dentro de los límites
        int newLevel = Math.max(0, Math.min(MAX_NUTRIENT_LEVEL, level));
        
        // Actualizar nivel
        Map<NutrientType, Integer> nutrients = playerNutrients.get(playerId);
        nutrients.put(type, newLevel);
        
        // Aplicar efectos si el nivel es crítico
        if (newLevel <= CRITICAL_NUTRIENT_LEVEL) {
            applyNutritionEffects(player, nutrients);
        }
    }
    
    /**
     * Establece todos los niveles de nutrientes para un jugador
     * @param player Jugador
     * @param levels Mapa con los niveles de todos los nutrientes
     */
    public void setAllNutrientLevels(Player player, Map<NutrientType, Integer> levels) {
        if (player == null || levels == null) return;
        
        UUID playerId = player.getUniqueId();
        
        // Crear un nuevo mapa para almacenar los valores
        Map<NutrientType, Integer> nutrients = new EnumMap<>(NutrientType.class);
        
        // Copiar y validar cada nivel
        for (NutrientType type : NutrientType.values()) {
            int level = levels.containsKey(type) ? levels.get(type) : DEFAULT_NUTRIENT_LEVEL;
            nutrients.put(type, Math.max(0, Math.min(MAX_NUTRIENT_LEVEL, level)));
        }
        
        // Actualizar todos los niveles
        playerNutrients.put(playerId, nutrients);
        
        // Verificar si hay niveles críticos y aplicar efectos
        boolean hasCriticalLevel = false;
        for (int level : nutrients.values()) {
            if (level <= CRITICAL_NUTRIENT_LEVEL) {
                hasCriticalLevel = true;
                break;
            }
        }
        
        if (hasCriticalLevel) {
            applyNutritionEffects(player, nutrients);
        }
    }
    
    /**
     * Aumenta el nivel de un nutriente específico para un jugador
     * @param player Jugador
     * @param type Tipo de nutriente
     * @param amount Cantidad a aumentar
     */
    public void addNutrient(Player player, NutrientType type, int amount) {
        int currentLevel = getNutrientLevel(player, type);
        setNutrientLevel(player, type, currentLevel + amount);
        
        // Notificar al jugador si el aumento es significativo
        if (amount >= 10 && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
            Component message = MiniMessage.miniMessage().deserialize(type.getColorCode() + "Has aumentado significativamente tu nivel de " + type.getDisplayName() + ".");
            Component actionBarMessage = MiniMessage.miniMessage().deserialize(type.getColorCode() + "Has aumentado significativamente tu nivel de " + type.getDisplayName());
            ((Audience) player).sendMessage(message);
            ((Audience) player).sendActionBar(actionBarMessage);
        }
    }
    
    /**
     * Reduce el nivel de un nutriente específico para un jugador
     * @param player Jugador
     * @param type Tipo de nutriente
     * @param amount Cantidad a reducir
     */
    public void removeNutrient(Player player, NutrientType type, int amount) {
        int currentLevel = getNutrientLevel(player, type);
        setNutrientLevel(player, type, currentLevel - amount);
        
        // Notificar al jugador si la reducción es significativa y el nivel es bajo
        if (amount >= 10 && currentLevel - amount <= CRITICAL_NUTRIENT_LEVEL && plugin.getUserPreferencesManager().hasStatusMessages(player)) {
            Component message = MiniMessage.miniMessage().deserialize(type.getColorCode() + "Tu nivel de " + type.getDisplayName() + " ha disminuido significativamente.");
            Component actionBarMessage = MiniMessage.miniMessage().deserialize(type.getColorCode() + "Tu nivel de " + type.getDisplayName() + " ha disminuido mucho.");
            ((Audience) player).sendMessage(message);
            ((Audience) player).sendActionBar(actionBarMessage);
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
     * Establece la tasa de disminución normal de nutrientes
     * @param rate Tasa de disminución (0.0 - 1.0)
     */
    public void setNormalDecreaseRate(double rate) {
        this.normalDecreaseRate = Math.max(0.0, Math.min(1.0, rate));
    }
    
    /**
     * Obtiene la tasa de disminución normal de nutrientes
     * @return Tasa de disminución normal
     */
    public double getNormalDecreaseRate() {
        return normalDecreaseRate;
    }
    
    /**
     * Establece la tasa de disminución de nutrientes durante actividad física
     * @param rate Tasa de disminución (0.0 - 1.0)
     */
    public void setActivityDecreaseRate(double rate) {
        this.activityDecreaseRate = Math.max(0.0, Math.min(1.0, rate));
    }
    
    /**
     * Obtiene la tasa de disminución de nutrientes durante actividad física
     * @return Tasa de disminución durante actividad
     */
    public double getActivityDecreaseRate() {
        return activityDecreaseRate;
    }
    
    /**
     * Detiene el sistema de nutrición
     */
    public void shutdown() {
        if (nutritionTask != null) {
            nutritionTask.cancel();
        }
        isActive = false;
        ((Audience) Bukkit.getConsoleSender()).sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Sistema de nutrición desactivado"));
    }

    public int getMaxNutrientLevel() {
        return MAX_NUTRIENT_LEVEL;
    }
}