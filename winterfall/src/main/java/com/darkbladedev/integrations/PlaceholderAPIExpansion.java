package com.darkbladedev.integrations;

import com.darkbladedev.WinterfallMain;
import com.darkbladedev.mechanics.HydrationSystem;
import com.darkbladedev.mechanics.NutritionSystem;
import com.darkbladedev.mechanics.NutritionSystem.NutrientType;
import com.darkbladedev.mechanics.RadiationSystem;
import com.darkbladedev.mechanics.BleedingSystem;
import com.darkbladedev.mechanics.LimbDamageSystem;
import com.darkbladedev.mechanics.StaminaSystem;
import com.darkbladedev.mechanics.TemperatureSystem;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Clase que maneja los placeholders personalizados para el plugin Winterfall
 * Permite mostrar información de hidratación, nutrición, radiación, sangrado y daño de extremidades
 * en otros plugins compatibles con PlaceholderAPI
 */
public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    @SuppressWarnings("unused")
    private final WinterfallMain plugin;
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
    public PlaceholderAPIExpansion(WinterfallMain plugin) {
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
        return "winterfall";
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
            return "";
        }

        // Placeholders de hidratación
        if (identifier.equals("hydration")) {
            return String.valueOf(hydrationSystem.getHydrationLevel(player));
        }

        if (identifier.equals("hydration_percent")) {
            return String.valueOf(hydrationSystem.getHydrationPercentage(player));
        }

        if (identifier.equals("hydration_bar")) {
            return hydrationSystem.getHydrationBar(player);
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
            return nutritionSystem.getNutrientBar(player, NutrientType.PROTEIN);
        }

        if (identifier.equals("nutrition_fat_bar")) {
            return nutritionSystem.getNutrientBar(player, NutrientType.FAT);
        }

        if (identifier.equals("nutrition_carbs_bar")) {
            return nutritionSystem.getNutrientBar(player, NutrientType.CARBS);
        }

        if (identifier.equals("nutrition_vitamins_bar")) {
            return nutritionSystem.getNutrientBar(player, NutrientType.VITAMINS);
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
        if (identifier.startsWith("limb_")) {
            String fullIdentifier = identifier.substring(5);
            String[] parts = fullIdentifier.split("_");
            
            String limbTypeStr = parts[0];
            boolean isLevel = parts.length > 1 && parts[1].equals("level");
            
            LimbDamageSystem.LimbType type = null;
            
            switch (limbTypeStr) {
                case "head":
                    type = LimbDamageSystem.LimbType.HEAD;
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
        }

        // Placeholders de temperatura
        if (identifier.equals("temperature")) {
            return String.valueOf(temperatureSystem.getTemperatureLevel(player));
        }

        if (identifier.equals("temperature_percent")) {
            return String.valueOf(temperatureSystem.getTemperaturePercentage(player));
        }

        if (identifier.equals("temperature_bar")) {
            return temperatureSystem.getTemperatureBar(player);
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
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Winterfall] Placeholders registrados correctamente");
            return true;
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Winterfall] Error al registrar los placeholders");
            return false;
        }
    }
}