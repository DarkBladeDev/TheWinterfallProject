package com.darkbladedev.integrations;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.mechanics.HydrationSystem;
import com.darkbladedev.mechanics.NutritionSystem;
import com.darkbladedev.mechanics.NutritionSystem.NutrientType;
import com.darkbladedev.mechanics.RadiationSystem;
import com.darkbladedev.mechanics.BleedingSystem;
import com.darkbladedev.mechanics.LimbDamageSystem;
import com.darkbladedev.mechanics.StaminaSystem;
import com.darkbladedev.mechanics.TemperatureSystem;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Clase que maneja los placeholders personalizados para el plugin Savage Frontier
 * Permite mostrar información de hidratación, nutrición, radiación, sangrado y daño de extremidades
 * en otros plugins compatibles con PlaceholderAPI
 */
public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final SavageFrontierMain plugin;
    private final HydrationSystem hydrationSystem;
    private final NutritionSystem nutritionSystem;
    private final RadiationSystem radiationSystem;
    private final BleedingSystem bleedingSystem;
    private final LimbDamageSystem limbDamageSystem;
    private final StaminaSystem staminaSystem;
    private final TemperatureSystem temperatureSystem;

    /**
     * Constructor para los placeholders de Winterfall
     * @param plugin Instancia del plugin principal
     */
    public PlaceholderAPIExpansion(SavageFrontierMain plugin) {
        this.plugin = plugin;
        this.hydrationSystem = plugin.getHydrationSystem();
        this.nutritionSystem = plugin.getNutritionSystem();
        this.radiationSystem = plugin.getRadiationSystem();
        this.bleedingSystem = plugin.getBleedingSystem();
        this.limbDamageSystem = plugin.getLimbDamageSystem();
        this.staminaSystem = plugin.getStaminaSystem();
        this.temperatureSystem = plugin.getTemperatureSystem();
    }

    /**
     * Identificador del plugin para PlaceholderAPI
     * @return Identificador del plugin
     */
    @Override
    public String getIdentifier() {
        return "savage-frontier";
    }

    /**
     * Autor del plugin para PlaceholderAPI
     * @return Autor del plugin
     */
    @Override
    public String getAuthor() {
        return "DarkBladeDev";
    }

    /**
     * Versión del plugin para PlaceholderAPI
     * @return Versión del plugin
     */
    @Override
    public String getVersion() {
        return "1.0.0";
    }

    /**
     * Indica si la expansión debe persistir a través de recargas
     * @return true si debe persistir, false en caso contrario
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Método principal que procesa los placeholders
     * @param player Jugador para el que se solicita el placeholder
     * @param identifier Identificador del placeholder
     * @return Valor del placeholder o null si no se reconoce
     */
    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String identifier) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return "";
        }

        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            Component errorMessage = MiniMessage.miniMessage().deserialize("<red>ERROR: Jugador no encontrado");
            return MiniMessage.miniMessage().serialize(errorMessage);
        }

        // Placeholders de hidratación
        if (identifier.equals("hydration")) {
            return String.valueOf(hydrationSystem.getHydrationLevel(player));
        }

        if (identifier.equals("hydration_percent")) {
            return String.valueOf(hydrationSystem.getHydrationPercentage(player));
        }

        if (identifier.equals("hydration_bar")) {
            // Obtenemos el Component y lo convertimos a un formato legible para PlaceholderAPI
            // Usamos MiniMessage para serializar el Component a un string con formato
            Component hydrationBar = hydrationSystem.getHydrationBar(player);
            // Convertimos el Component a un string con formato MiniMessage
            return MiniMessage.miniMessage().serialize(hydrationBar);
        }

        // Placeholders de nutrición
        if (identifier.equals("nutrition_protein")) {
            return String.valueOf(nutritionSystem.getNutrientLevel(player, NutrientType.PROTEIN));
        }

        if (identifier.equals("nutrition_fat")) {
            return String.valueOf(nutritionSystem.getNutrientLevel(player, NutrientType.FAT));
        }

        if (identifier.equals("nutrition_carbs")) {
            return String.valueOf(nutritionSystem.getNutrientLevel(player, NutrientType.CARBS));
        }

        if (identifier.equals("nutrition_vitamins")) {
            return String.valueOf(nutritionSystem.getNutrientLevel(player, NutrientType.VITAMINS));
        }

        // Barras de progreso para nutrición
        if (identifier.equals("nutrition_protein_bar")) {
            return MiniMessage.miniMessage().serialize(nutritionSystem.getNutrientBar(player, NutrientType.PROTEIN));
        }

        if (identifier.equals("nutrition_fat_bar")) {
            return MiniMessage.miniMessage().serialize(nutritionSystem.getNutrientBar(player, NutrientType.FAT));
        }

        if (identifier.equals("nutrition_carbs_bar")) {
            return MiniMessage.miniMessage().serialize(nutritionSystem.getNutrientBar(player, NutrientType.CARBS));
        }

        if (identifier.equals("nutrition_vitamins_bar")) {
            return MiniMessage.miniMessage().serialize(nutritionSystem.getNutrientBar(player, NutrientType.VITAMINS));
        }

        // Placeholders de radiación
        if (identifier.equals("radiation")) {
            return String.valueOf(radiationSystem.getRadiationLevel(player));
        }

        if (identifier.equals("radiation_percent")) {
            return String.valueOf(radiationSystem.getPlayerRadiationLevel(player));
        }

        // Placeholders de sangrado
        if (identifier.equals("bleeding")) {
            return bleedingSystem.isPlayerBleeding(player) ? "Sí" : "No";
        }

        if (identifier.equals("bleeding_level")) {
            return String.valueOf(bleedingSystem.getPlayerBleedingLevel(player));
        }

        // Placeholders de daño de extremidades
        if (identifier.startsWith("limb_") && limbDamageSystem != null) {
            String fullIdentifier = identifier.substring(5);
            String[] parts = fullIdentifier.split("_");
            
            String limbTypeStr = parts[0];
            boolean isLevel = parts.length > 1 && parts[1].equals("level");
            
            LimbDamageSystem.LimbType type = null;
            
            switch (limbTypeStr) {
                case "head":
                    type = LimbDamageSystem.LimbType.HEAD;
                    break;
                case "torso":
                    type = LimbDamageSystem.LimbType.TORSO;
                    break;
                case "left_arm":
                case "leftarm":
                    type = LimbDamageSystem.LimbType.LEFT_ARM;
                    break;
                case "right_arm":
                case "rightarm":
                    type = LimbDamageSystem.LimbType.RIGHT_ARM;
                    break;
                case "left_leg":
                case "leftleg":
                    type = LimbDamageSystem.LimbType.LEFT_LEG;
                    break;
                case "right_leg":
                case "rightleg":
                    type = LimbDamageSystem.LimbType.RIGHT_LEG;
                    break;
                case "left_foot":
                case "leftfoot":
                    type = LimbDamageSystem.LimbType.LEFT_FOOT;
                    break;
                case "right_foot":
                case "rightfoot":
                    type = LimbDamageSystem.LimbType.RIGHT_FOOT;
                    break;

                default:
                    return "";
            }
            
            if (type != null) {
                if (isLevel) {
                    // Devolver el nivel numérico de daño
                    return String.valueOf(limbDamageSystem.getLimbDamageLevel(player, type));
                } else {
                    // Devolver el estado de daño como texto
                    LimbDamageSystem.DamageState state = limbDamageSystem.getLimbDamageState(player, type);
                    return state.getDisplayName();
                }
            }
            return "";
        } else if (identifier.startsWith("limb_")) {
            // Si el sistema de daño por extremidades no está disponible, devolver un valor por defecto
            return "N/A";
        }

        // Placeholders de temperatura
        if (identifier.equals("temperature")) {
            return String.valueOf(temperatureSystem.getTemperatureLevel(player));
        }

        if (identifier.equals("temperature_percent")) {
            return String.valueOf(temperatureSystem.getTemperaturePercentage(player));
        }

        if (identifier.equals("temperature_bar")) {
            return MiniMessage.miniMessage().serialize(temperatureSystem.getTemperatureBar(player));
        }
        
        // Placeholders de estamina
        if (identifier.equals("stamina")) {
            return String.valueOf(staminaSystem.getStaminaLevel(player));
        }
        
        if (identifier.equals("stamina_percent")) {
            return String.valueOf(staminaSystem.getStaminaPercentage(player));
        }
        
        if (identifier.equals("stamina_bar")) {
            return staminaSystem.getStaminaBar(player);
        }

        // Placeholder no reconocido
        return null;
    }

    /**
     * Registra los placeholders en PlaceholderAPI
     * @return true si se registraron correctamente, false en caso contrario
     */
    @Override
    public boolean register() {
        if (super.register()) {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <green>Placeholders registrados correctamente"));
            return true;
        } else {
            Bukkit.getConsoleSender().sendMessage(MiniMessage.miniMessage().deserialize(plugin.PREFIX + " <red>Error al registrar los placeholders"));
            return false;
        }
    }
}