package com.darkbladedev.items;

import com.darkbladedev.WinterfallMain;
import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import com.ssomar.score.api.executableitems.config.ExecutableItemsManagerInterface;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Gestor de ítems personalizados para "El Eternauta"
 */
public class ItemManager {

    private final WinterfallMain plugin;
    private final Map<String, ItemStack> customItems;

    //ITEMS
    private ExecutableItemInterface basic_prot_boots;
    private ExecutableItemInterface basic_prot_chestplate;
    private ExecutableItemInterface basic_prot_pants;
    private ExecutableItemInterface basic_prot_mask;
    private ExecutableItemsManagerInterface EI_API;
    
    /**
     * Constructor del gestor de ítems
     * @param plugin Instancia del plugin principal
     */
    public ItemManager(WinterfallMain plugin) {
        this.plugin = plugin;
        this.customItems = new HashMap<>();
        
        

        // Inicializar ítems
        initializeItems();
        
        // Registrar recetas
        registerRecipes();
        
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Winterfall] Sistema de ítems inicializado");
    }
    
    /**
     * Inicializa todos los ítems personalizados
     */
    private void initializeItems() {
        EI_API = ExecutableItemsAPI.getExecutableItemsManager();

        basic_prot_boots = EI_API.getExecutableItem("basic-prot-boots").get();
        basic_prot_chestplate = EI_API.getExecutableItem("basic-prot-chestplate").get();
        basic_prot_pants = EI_API.getExecutableItem("basic-prot-pants").get();
        basic_prot_mask = EI_API.getExecutableItem("basic-prot-mask").get();
    }
    
    /**
     * Registra todas las recetas de crafteo
     */
    private void registerRecipes() {
        // Receta para el casco aislante
        ShapedRecipe helmetRecipe = new ShapedRecipe(new NamespacedKey(plugin, "isolation_helmet"), basic_prot_mask.buildItem(1, Optional.empty(), Optional.empty()));
        helmetRecipe.shape("WWW", "WGW", "   ");
        helmetRecipe.setIngredient('W', Material.WHITE_WOOL);
        helmetRecipe.setIngredient('G', Material.GLASS);
        Bukkit.addRecipe(helmetRecipe);
        
        // Receta para el peto aislante
        ShapedRecipe chestplateRecipe = new ShapedRecipe(new NamespacedKey(plugin, "isolation_chestplate"), basic_prot_chestplate.buildItem(1, Optional.empty()));
        chestplateRecipe.shape("W W", "WWW", "WWW");
        chestplateRecipe.setIngredient('W', Material.WHITE_WOOL);
        Bukkit.addRecipe(chestplateRecipe);
        
        // Receta para las polainas aislantes
        ShapedRecipe leggingsRecipe = new ShapedRecipe(new NamespacedKey(plugin, "isolation_leggings"), basic_prot_pants.buildItem(1, Optional.empty()));
        leggingsRecipe.shape("WWW", "W W", "W W");
        leggingsRecipe.setIngredient('W', Material.WHITE_WOOL);
        Bukkit.addRecipe(leggingsRecipe);
        
        // Receta para las botas aislantes
        ShapedRecipe bootsRecipe = new ShapedRecipe(new NamespacedKey(plugin, "isolation_boots"), basic_prot_boots.buildItem(1, Optional.empty()));
        bootsRecipe.shape("   ", "W W", "W W");
        bootsRecipe.setIngredient('W', Material.WHITE_WOOL);
        Bukkit.addRecipe(bootsRecipe);
    }
    
    /**
     * Obtiene un ítem personalizado por su ID
     * @param itemId ID del ítem
     * @return ItemStack del ítem personalizado o null si no existe
     */
    public ItemStack getItem(String itemId) {
        if (customItems.containsKey(itemId)) {
            return customItems.get(itemId).clone();
        }
        return null;
    }
    
    /**
     * Verifica si un ítem es un ítem personalizado específico
     * @param item Ítem a verificar
     * @param itemId ID del ítem personalizado
     * @return true si es el ítem personalizado, false en caso contrario
     */
    public boolean isCustomItem(ItemStack item, String itemId) {
        if (item == null && !EI_API.isValidID(itemId)) {
            return false;
        }
        return true;
    }
    
    /**
     * Verifica si un ítem es parte del traje aislante
     * @param item Ítem a verificar
     * @return true si es parte del traje aislante, false en caso contrario
     */
    public boolean isIsolationSuitPart(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        return isCustomItem(item, "basic-prot-mask") ||
               isCustomItem(item, "basic-prot-chestplate") ||
               isCustomItem(item, "basic-prot-pants") ||
               isCustomItem(item, "basic-prot-boots");
    }
    
    /**
     * Verifica si un jugador tiene el traje aislante completo
     * @param helmet Casco
     * @param chestplate Peto
     * @param leggings Polainas
     * @param boots Botas
     * @return true si tiene el traje completo, false en caso contrario
     */
    public boolean hasFullIsolationSuit(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
        return isCustomItem(helmet, "basic-prot-mask") &&
               isCustomItem(chestplate, "basic-prot-chestplate") &&
               isCustomItem(leggings, "basic-prot-pants") &&
               isCustomItem(boots, "basic-prot-boots");
    }
}