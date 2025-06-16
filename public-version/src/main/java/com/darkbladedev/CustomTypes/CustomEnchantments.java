package com.darkbladedev.CustomTypes;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import net.kyori.adventure.key.Key;

public class CustomEnchantments {

    public static final Key CONGELATION_KEY = Key.key("savage-frontier:freeze");
    public static final Key RADIATION_PROTECTION_KEY = Key.key("savage-frontier:radiation_protection");
    public static final Key COLD_PROTECTION_KEY = Key.key("savage-frontier:cold_protection");
    public static final Key HEAT_PROTECTION_KEY = Key.key("savage-frontier:heat_protection");
    
    /**
     * Obtiene el encantamiento de Congelación del registro
     * @return El encantamiento de Congelación
     */
    @SuppressWarnings("deprecation")
    public static Enchantment getCongelationEnchantment() {
        return Registry.ENCHANTMENT.get(CONGELATION_KEY);
    }
    
    /**
     * Obtiene el encantamiento de Protección contra Radiación del registro
     * @return El encantamiento de Protección contra Radiación
     */
    @SuppressWarnings("deprecation")
    public static Enchantment getRadiationProtectionEnchantment() {
        return Registry.ENCHANTMENT.get(RADIATION_PROTECTION_KEY);
    }

    /**
     * Obtiene el encantamiento de Protección contra el Frio del registro
     * @return El encantamiento de Protección contra Radiación
     */
    @SuppressWarnings("deprecation")
    public static Enchantment getColdProtectionEnchantment() {
        return Registry.ENCHANTMENT.get(COLD_PROTECTION_KEY);
    }

    /**
     * Obtiene el encantamiento de Protección contra el Calor del registro
     * @return El encantamiento de Protección contra Radiación
     */
    @SuppressWarnings("deprecation")
    public static Enchantment getHeatProtectionEnchantment() {
        return Registry.ENCHANTMENT.get(HEAT_PROTECTION_KEY);
    }

    /**
     * Enum que contiene todos los encantamientos personalizados registrados
     */
    public enum CustomEnchantment {
        CONGELATION(CONGELATION_KEY, "Congelación", 1),
        RADIATION_PROTECTION(RADIATION_PROTECTION_KEY, "Protección contra radiación", 3),
        COLD_PROTECTION(COLD_PROTECTION_KEY, "Protección contra frío", 3),
        HEAT_PROTECTION(HEAT_PROTECTION_KEY, "Protección contra calor", 3);
        
        private final Key key;
        private final String displayName;
        private final int maxLevel;
        
        CustomEnchantment(Key key, String displayName, int maxLevel) {
            this.key = key;
            this.displayName = displayName;
            this.maxLevel = maxLevel;
        }
        
        public Key getKey() {
            return key;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getMaxLevel() {
            return maxLevel;
        }
        
        @SuppressWarnings("deprecation")
        public Enchantment getEnchantment(Key key) {
            return Registry.ENCHANTMENT.get(key);
        }
    }

    public static List<ItemStack> buildEquipmentSlots(List<ItemStack> list, String type) {
        switch (type) {
            case "tools":
                String[] list0 = {"WOODEN_PICKAXE", "STONE_PICKAXE", "IRON_PICKAXE", "DIAMOND_PICKAXE", "NETHERITE_PICKAXE"};
                for (String i : list0) {
                    list.add(
                        ItemStack.of(Material.getMaterial(i))
                        );
                }
                break;
                
                case "weapons":
                    String[] list1 = {"WOODEN_SWORD", "STONE_SWORD", "IRON_SWORD", "DIAMOND_SWORD", "NETHERITE_SWORD"};
                    for (String i : list1) {
                        list.add(
                            ItemStack.of(Material.getMaterial(i))
                            );
                    }
                break;
                
                default:
                break;
            }
            return list;
    };

    /**
     * Crea un libro encantado con el encantamiento personalizado especificado
     * @param enchantment El encantamiento personalizado
     * @param level El nivel del encantamiento (debe ser entre 1 y el nivel máximo del encantamiento)
     * @return Un ItemStack con un libro encantado
     */
    public static ItemStack getCustomEnchantedBook(CustomEnchantment enchantment, int level) {
        // Validar nivel
        if (level < 1 || level > enchantment.getMaxLevel()) {
            level = 1; // Nivel por defecto si está fuera de rango
        }
        
        // Crear libro encantado
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        
        // Obtener el encantamiento del registro y agregarlo al libro
        Enchantment customEnchantment = null;
        
        // Seleccionar el encantamiento correcto según el tipo
        switch (enchantment) {
            case CONGELATION:
                customEnchantment = getCongelationEnchantment();
                break;
            case RADIATION_PROTECTION:
                customEnchantment = getRadiationProtectionEnchantment();
                break;
            case COLD_PROTECTION:
                customEnchantment = getColdProtectionEnchantment();
                break;
            case HEAT_PROTECTION:
                customEnchantment = getHeatProtectionEnchantment();
                break;
        }
        
        if (customEnchantment != null) {
            meta.addStoredEnchant(customEnchantment, level, true);
            book.setItemMeta(meta);
        }
        
        return book;
    }
    
    /**
     * Sobrecarga del método para usar el nivel 1 por defecto
     * @param enchantment El encantamiento personalizado
     * @return Un ItemStack con un libro encantado nivel 1
     */
    public static ItemStack getCustomEnchantedBook(CustomEnchantment enchantment) {
        return getCustomEnchantedBook(enchantment, 1);
    }
}