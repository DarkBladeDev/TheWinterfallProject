package com.darkbladedev.items;

import com.darkbladedev.WinterfallMain;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestor de ítems personalizados para "El Eternauta"
 */
public class ItemManager {

    private final WinterfallMain plugin;
    private final Map<String, ItemStack> customItems;
    
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
        // Traje aislante (protección contra la nieve tóxica)
        createIsolationHelmet();
        createIsolationChestplate();
        createIsolationLeggings();
        createIsolationBoots();
        
        // Armas contra invasores
        createFlamethrower();
        createElectricGun();
    }
    
    /**
     * Registra todas las recetas de crafteo
     */
    private void registerRecipes() {
        // Receta para el casco aislante
        ShapedRecipe helmetRecipe = new ShapedRecipe(new NamespacedKey(plugin, "isolation_helmet"), getItem("isolation_helmet"));
        helmetRecipe.shape("WWW", "WGW", "   ");
        helmetRecipe.setIngredient('W', Material.WHITE_WOOL);
        helmetRecipe.setIngredient('G', Material.GLASS);
        Bukkit.addRecipe(helmetRecipe);
        
        // Receta para el peto aislante
        ShapedRecipe chestplateRecipe = new ShapedRecipe(new NamespacedKey(plugin, "isolation_chestplate"), getItem("isolation_chestplate"));
        chestplateRecipe.shape("W W", "WWW", "WWW");
        chestplateRecipe.setIngredient('W', Material.WHITE_WOOL);
        Bukkit.addRecipe(chestplateRecipe);
        
        // Receta para las polainas aislantes
        ShapedRecipe leggingsRecipe = new ShapedRecipe(new NamespacedKey(plugin, "isolation_leggings"), getItem("isolation_leggings"));
        leggingsRecipe.shape("WWW", "W W", "W W");
        leggingsRecipe.setIngredient('W', Material.WHITE_WOOL);
        Bukkit.addRecipe(leggingsRecipe);
        
        // Receta para las botas aislantes
        ShapedRecipe bootsRecipe = new ShapedRecipe(new NamespacedKey(plugin, "isolation_boots"), getItem("isolation_boots"));
        bootsRecipe.shape("   ", "W W", "W W");
        bootsRecipe.setIngredient('W', Material.WHITE_WOOL);
        Bukkit.addRecipe(bootsRecipe);
    }
    
    /**
     * Crea el casco aislante
     */
    private void createIsolationHelmet() {
        ItemStack helmet = new ItemStack(Material.GLASS);
        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
        
        // Establecer propiedades
        meta.setDisplayName(ChatColor.WHITE + "Casco Aislante");
        meta.setColor(Color.WHITE);
        
        // Lore
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Parte del traje aislante");
        lore.add(ChatColor.GRAY + "Protege contra la nieve tóxica");
        lore.add(ChatColor.DARK_PURPLE + "Usado por Juan Salvo en El Eternauta");
        meta.setLore(lore);
        
        // Añadir encantamiento visual
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        
        // Añadir tag personalizado para identificación
        NamespacedKey key = new NamespacedKey(plugin, "custom_item");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "isolation_helmet");
        
        helmet.setItemMeta(meta);
        customItems.put("isolation_helmet", helmet);
    }
    
    /**
     * Crea el peto aislante
     */
    private void createIsolationChestplate() {
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
        
        // Establecer propiedades
        meta.setDisplayName(ChatColor.WHITE + "Peto Aislante");
        meta.setColor(Color.WHITE);
        
        // Lore
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Parte del traje aislante");
        lore.add(ChatColor.GRAY + "Protege contra la nieve tóxica");
        lore.add(ChatColor.DARK_PURPLE + "Usado por Juan Salvo en El Eternauta");
        meta.setLore(lore);
        
        // Añadir encantamiento visual
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        
        // Añadir tag personalizado para identificación
        NamespacedKey key = new NamespacedKey(plugin, "custom_item");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "isolation_chestplate");
        
        chestplate.setItemMeta(meta);
        customItems.put("isolation_chestplate", chestplate);
    }
    
    /**
     * Crea las polainas aislantes
     */
    private void createIsolationLeggings() {
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta meta = (LeatherArmorMeta) leggings.getItemMeta();
        
        // Establecer propiedades
        meta.setDisplayName(ChatColor.WHITE + "Polainas Aislantes");
        meta.setColor(Color.WHITE);
        
        // Lore
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Parte del traje aislante");
        lore.add(ChatColor.GRAY + "Protege contra la nieve tóxica");
        lore.add(ChatColor.DARK_PURPLE + "Usado por Juan Salvo en El Eternauta");
        meta.setLore(lore);
        
        // Añadir encantamiento visual
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        
        // Añadir tag personalizado para identificación
        NamespacedKey key = new NamespacedKey(plugin, "custom_item");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "isolation_leggings");
        
        leggings.setItemMeta(meta);
        customItems.put("isolation_leggings", leggings);
    }
    
    /**
     * Crea las botas aislantes
     */
    private void createIsolationBoots() {
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
        
        // Establecer propiedades
        meta.setDisplayName(ChatColor.WHITE + "Botas Aislantes");
        meta.setColor(Color.WHITE);
        
        // Lore
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Parte del traje aislante");
        lore.add(ChatColor.GRAY + "Protege contra la nieve tóxica");
        lore.add(ChatColor.DARK_PURPLE + "Usado por Juan Salvo en El Eternauta");
        meta.setLore(lore);
        
        // Añadir encantamiento visual
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        
        // Añadir tag personalizado para identificación
        NamespacedKey key = new NamespacedKey(plugin, "custom_item");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "isolation_boots");
        
        boots.setItemMeta(meta);
        customItems.put("isolation_boots", boots);
    }
    
    /**
     * Crea el lanzallamas (arma contra los Gurbos)
     */
    private void createFlamethrower() {
        ItemStack flamethrower = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = flamethrower.getItemMeta();
        
        // Establecer propiedades
        meta.setDisplayName(ChatColor.GOLD + "Lanzallamas");
        
        // Lore
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Arma efectiva contra los Gurbos");
        lore.add(ChatColor.GRAY + "Dispara una ráfaga de fuego");
        lore.add(ChatColor.DARK_PURPLE + "Usado por la resistencia en El Eternauta");
        meta.setLore(lore);
        
        // Añadir encantamiento visual
        meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
        
        // Añadir tag personalizado para identificación
        NamespacedKey key = new NamespacedKey(plugin, "custom_item");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "flamethrower");
        
        flamethrower.setItemMeta(meta);
        customItems.put("flamethrower", flamethrower);
    }
    
    /**
     * Crea la pistola eléctrica (arma contra las Manos)
     */
    private void createElectricGun() {
        ItemStack electricGun = new ItemStack(Material.TRIDENT);
        ItemMeta meta = electricGun.getItemMeta();
        
        // Establecer propiedades
        meta.setDisplayName(ChatColor.AQUA + "Pistola Eléctrica");
        
        // Lore
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Arma efectiva contra las Manos");
        lore.add(ChatColor.GRAY + "Dispara una descarga eléctrica");
        lore.add(ChatColor.DARK_PURPLE + "Usado por la resistencia en El Eternauta");
        meta.setLore(lore);
        
        // Añadir encantamiento visual
        meta.addEnchant(Enchantment.CHANNELING, 1, true);
        
        // Añadir tag personalizado para identificación
        NamespacedKey key = new NamespacedKey(plugin, "custom_item");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "electric_gun");
        
        electricGun.setItemMeta(meta);
        customItems.put("electric_gun", electricGun);
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
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "custom_item");
        
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return false;
        }
        
        String storedId = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return itemId.equals(storedId);
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
        
        return isCustomItem(item, "isolation_helmet") ||
               isCustomItem(item, "isolation_chestplate") ||
               isCustomItem(item, "isolation_leggings") ||
               isCustomItem(item, "isolation_boots");
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
        return isCustomItem(helmet, "isolation_helmet") &&
               isCustomItem(chestplate, "isolation_chestplate") &&
               isCustomItem(leggings, "isolation_leggings") &&
               isCustomItem(boots, "isolation_boots");
    }
}