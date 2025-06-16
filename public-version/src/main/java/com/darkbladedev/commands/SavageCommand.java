package com.darkbladedev.commands;

import com.darkbladedev.SavageFrontierMain;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manejador de comandos para TWFP
 * Permite a los jugadores interactuar con las funcionalidades del plugin
 * Implementa TabCompleter para proporcionar autocompletado de comandos
 */
public class SavageCommand implements CommandExecutor, TabCompleter {

    private final SavageFrontierMain plugin;
    
    /**
     * Constructor del manejador de comandos
     * @param plugin Instancia del plugin principal
     */
    public SavageCommand(SavageFrontierMain plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar si el comando es "savage"
        if (!command.getName().equalsIgnoreCase("savage")) {
            return false;
        }
        
        // Mostrar ayuda si no hay argumentos
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        // Procesar subcomandos
        switch (args[0].toLowerCase()) {
            case "help":
                showHelp(sender);
                break;
                
            case "radiation":
                handleRadiationCommand(sender, args);
                break;
                
            case "bleeding":
                handleBleedingCommand(sender, args);
                break;
                
            case "status":
                handleStatusCommand(sender, args);
                break;
                
            case "hydration":
                handleHydrationCommand(sender, args);
                break;
                
            case "nutrition":
                handleNutritionCommand(sender, args);
                break;
                
            case "limb":
                handleLimbCommand(sender, args);
                break;
                
            case "config":
                handleConfigCommand(sender, args);
                break;
                
            // Mantener compatibilidad con comandos antiguos
            case "hydrationrate":
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Este comando está obsoleto. Usa /savage config hydration-rate en su lugar."));
                String[] newArgs = new String[args.length + 1];
                newArgs[0] = "config";
                newArgs[1] = "hydration-rate";
                System.arraycopy(args, 1, newArgs, 2, args.length - 1);
                handleConfigCommand(sender, newArgs);
                break;
                
            case "nutritionrate":
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Este comando está obsoleto. Usa /savage config nutrition-rate en su lugar."));
                String[] newArgs2 = new String[args.length + 1];
                newArgs2[0] = "config";
                newArgs2[1] = "nutrition-rate";
                System.arraycopy(args, 1, newArgs2, 2, args.length - 1);
                handleConfigCommand(sender, newArgs2);
                break;
                
            case "enchantment":
                handleEnchantmentCommand(sender, args);
                break;
                
            default:
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Subcomando desconocido. Usa /savage help para ver los comandos disponibles."));
                break;
        }
        
        return true;
    }
    
    /**
     * Muestra la ayuda del plugin
     * @param sender Remitente del comando
     */
    private void showHelp(CommandSender sender) {
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<gray>----------------------------------------"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Savage Frontier<gray> - Comandos:"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/savage help<gray> - Muestra esta ayuda"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/savage radiation <on/off><gray> - Activa/desactiva la radiación"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/savage bleeding <cure/start> [jugador] [severidad] [duración]<gray> - Gestiona el sangrado"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/savage hydration <set/add/remove> <cantidad> [jugador]<gray> - Gestiona la hidratación"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/savage nutrition <protein/fat/carbs/vitamins> <set/add/remove> <cantidad> [jugador]<gray> - Gestiona la nutrición"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/savage limb <set/heal> <extremidad/all> <nivel> [jugador]<gray> - Gestiona el daño de extremidades"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/savage status [jugador]<gray> - Muestra el estado físico del jugador"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/savage config hydration-rate <normal/activity> <tasa><gray> - Ajusta la velocidad de disminución de hidratación"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/savage config nutrition-rate <normal/activity> <tasa><gray> - Ajusta la velocidad de disminución de nutrientes"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/savage enchantment give <encantamiento> [nivel] [jugador]<gray> - Da un libro encantado"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/savage enchantment apply <encantamiento> [nivel]<gray> - Aplica un encantamiento al ítem en mano"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<gray>----------------------------------------"));
    }
    
    
    /**
     * Maneja el subcomando "enchantment"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleEnchantmentCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("savage.enchantment")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para usar este comando."));
            return;
        }
        
        // Verificar argumentos
        if (args.length < 2) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Uso: /savage enchantment <give/apply> <encantamiento> [nivel] [jugador]"));
            return;
        }
        
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "give":
                handleEnchantmentGiveCommand(sender, args);
                break;
                
            case "apply":
                handleEnchantmentApplyCommand(sender, args);
                break;
                
            default:
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Subcomando desconocido. Usa 'give' o 'apply'."));
                break;
        }
    }
    
    /**
     * Maneja el subcomando "enchantment give"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleEnchantmentGiveCommand(CommandSender sender, String[] args) {
        // Verificar argumentos
        if (args.length < 3) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Uso: /savage enchantment give <encantamiento> [nivel] [jugador]"));
            return;
        }
        
        // Obtener el encantamiento
        String enchantmentName = args[2].toUpperCase();
        com.darkbladedev.CustomTypes.CustomEnchantments.CustomEnchantment enchantment = null;
        
        try {
            enchantment = com.darkbladedev.CustomTypes.CustomEnchantments.CustomEnchantment.valueOf(enchantmentName);
        } catch (IllegalArgumentException e) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Encantamiento no encontrado: " + enchantmentName));
            return;
        }
        
        // Obtener el nivel (opcional, por defecto 1)
        int level = 1;
        if (args.length >= 4) {
            try {
                level = Integer.parseInt(args[3]);
                if (level < 1 || level > enchantment.getMaxLevel()) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Nivel inválido. Debe estar entre 1 y " + enchantment.getMaxLevel()));
                    return;
                }
            } catch (NumberFormatException e) {
                // Si no es un número, asumimos que es un nombre de jugador
            }
        }
        
        // Obtener el jugador (opcional, por defecto el remitente)
        Player targetPlayer = null;
        if (args.length >= 5) {
            targetPlayer = Bukkit.getPlayer(args[4]);
        } else if (args.length == 4 && !(args[3].matches("\\d+"))) {
            // Si el cuarto argumento no es un número, asumimos que es un nombre de jugador
            targetPlayer = Bukkit.getPlayer(args[3]);
            level = 1; // Resetear el nivel a 1 ya que no se especificó
        } else if (sender instanceof Player) {
            targetPlayer = (Player) sender;
        } else {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Debes especificar un jugador cuando ejecutas este comando desde la consola."));
            return;
        }
        
        if (targetPlayer == null) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Jugador no encontrado."));
            return;
        }
        
        // Crear y dar el libro encantado
        org.bukkit.inventory.ItemStack book = com.darkbladedev.CustomTypes.CustomEnchantments.getCustomEnchantedBook(enchantment, level);
        targetPlayer.getInventory().addItem(book);
        
        // Notificar al jugador y al remitente
        ((Audience) targetPlayer).sendMessage(MiniMessage.miniMessage().deserialize("<green>Has recibido un libro con el encantamiento " + enchantment.getDisplayName() + " nivel " + level + "."));
        
        if (sender != targetPlayer) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Has dado un libro con el encantamiento " + enchantment.getDisplayName() + " nivel " + level + " a " + targetPlayer.getName() + "."));
        }
    }
    
    /**
     * Maneja el subcomando "enchantment apply"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleEnchantmentApplyCommand(CommandSender sender, String[] args) {
        // Verificar si el remitente es un jugador
        if (!(sender instanceof Player)) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Este comando solo puede ser ejecutado por un jugador."));
            return;
        }
        
        Player player = (Player) sender;
        
        // Verificar argumentos
        if (args.length < 3) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Uso: /savage enchantment apply <encantamiento> [nivel]"));
            return;
        }
        
        // Obtener el encantamiento
        String enchantmentName = args[2].toUpperCase();
        com.darkbladedev.CustomTypes.CustomEnchantments.CustomEnchantment enchantment = null;
        
        try {
            enchantment = com.darkbladedev.CustomTypes.CustomEnchantments.CustomEnchantment.valueOf(enchantmentName);
        } catch (IllegalArgumentException e) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Encantamiento no encontrado: " + enchantmentName));
            return;
        }
        
        // Obtener el nivel (opcional, por defecto 1)
        int level = 1;
        if (args.length >= 4) {
            try {
                level = Integer.parseInt(args[3]);
                if (level < 1 || level > enchantment.getMaxLevel()) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Nivel inválido. Debe estar entre 1 y " + enchantment.getMaxLevel()));
                    return;
                }
            } catch (NumberFormatException e) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Nivel inválido. Debe ser un número entre 1 y " + enchantment.getMaxLevel()));
                return;
            }
        }
        
        // Verificar si el jugador tiene un ítem en la mano
        org.bukkit.inventory.ItemStack itemInHand = player.getInventory().getItemInMainHand();
        
        if (itemInHand.getType() == org.bukkit.Material.AIR) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Debes tener un ítem en la mano para aplicar el encantamiento."));
            return;
        }
        
        // Obtener el encantamiento del registro
        org.bukkit.enchantments.Enchantment customEnchantment = enchantment.getEnchantment(enchantment.getKey());
        
        if (customEnchantment == null) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Error al obtener el encantamiento del registro."));
            return;
        }
        
        // Aplicar el encantamiento al ítem
        itemInHand.addUnsafeEnchantment(customEnchantment, level);
        
        // Notificar al jugador
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Has aplicado el encantamiento " + enchantment.getDisplayName() + " nivel " + level + " a tu ítem."));
    }
    
    /**
     * Maneja el subcomando "radiation"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleRadiationCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("savage.radiation")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para usar este comando."));
            return;
        }
        
        // Verificar argumentos
        if (args.length < 2) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Uso: /savage radiation <on/off>"));
            return;
        }
        
        String state = args[1].toLowerCase();
        
        switch (state) {
            case "on":
            case "true":
            case "enable":
                if (plugin.getRadiationSystem().isActive()) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>El sistema de radiación ya está activo."));
                } else {
                    plugin.getRadiationSystem().initialize();
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Sistema de radiación activado."));
                }
                break;
                
            case "off":
            case "false":
            case "disable":
                if (!plugin.getRadiationSystem().isActive()) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>El sistema de radiación ya está inactivo."));
                } else {
                    plugin.getRadiationSystem().shutdown();
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Sistema de radiación desactivado."));
                }
                break;
                
            default:
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Estado desconocido. Usa 'on' o 'off'."));
                break;
        }
    }
    
    /**
     * Maneja el subcomando "bleeding"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleBleedingCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("savage.bleeding")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para usar este comando."));
            return;
        }
        
        // Verificar argumentos
        if (args.length < 2) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Uso: /savage bleeding <cure/start> [jugador] [severidad] [duración]"));
            return;
        }
        
        String action = args[1].toLowerCase();
        
        if (action.equals("cure")) {
            if (args.length >= 3) {
                // Curar sangrado de otro jugador
                String playerName = args[2];
                Player target = Bukkit.getPlayer(playerName);
                
                if (target == null) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Jugador no encontrado: " + playerName));
                    return;
                }
                
                plugin.getBleedingSystem().stopBleeding(target);
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Has curado el sangrado de " + target.getName() + "."));
                ((Audience) target).sendMessage(MiniMessage.miniMessage().deserialize("<green>Tu sangrado ha sido curado por " + sender.getName() + "."));
                
            } else if (sender instanceof Player) {
                // Curar sangrado propio
                Player player = (Player) sender;
                plugin.getBleedingSystem().stopBleeding(player);
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<green>Has curado tu sangrado."));
            } else {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Debes especificar un jugador cuando ejecutas desde la consola."));
            }
        } else if (action.equals("start")) {
            // Valores predeterminados
            int severity = 1; // Severidad por defecto: leve (1-3)
            int duration = 20; // Duración por defecto: 20 segundos
            
            if (args.length >= 4) {
                try {
                    severity = Integer.parseInt(args[3]);
                    // Asegurar que la severidad esté entre 1 y 3
                    severity = Math.max(1, Math.min(3, severity));
                } catch (NumberFormatException e) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>La severidad debe ser un número entre 1 y 3."));
                    return;
                }
            }
            
            if (args.length >= 5) {
                try {
                    duration = Integer.parseInt(args[4]);
                    // Asegurar que la duración sea al menos 5 segundos
                    duration = Math.max(5, duration);
                } catch (NumberFormatException e) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>La duración debe ser un número mayor a 5."));
                    return;
                }
            }
            
            if (args.length >= 3) {
                // Aplicar sangrado a otro jugador
                String playerName = args[2];
                Player target = Bukkit.getPlayer(playerName);
                
                if (target == null) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Jugador no encontrado: " + playerName));
                    return;
                }
                
                plugin.getBleedingSystem().applyBleeding(target, severity, duration);
                
                String severityText = severity == 1 ? "leve" : (severity == 2 ? "moderado" : "grave");
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Has aplicado sangrado " + severityText + " a " + target.getName() + " por " + duration + " segundos."));
            } else if (sender instanceof Player) {
                // Aplicar sangrado a sí mismo
                Player player = (Player) sender;
                plugin.getBleedingSystem().applyBleeding(player, severity, duration);
                
                String severityText = severity == 1 ? "leve" : (severity == 2 ? "moderado" : "grave");
                ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<green>Te has aplicado sangrado " + severityText + " por " + duration + " segundos."));
            } else {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Debes especificar un jugador cuando ejecutas desde la consola."));
            }
        } else {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Acción desconocida. Usa 'cure' o 'start'."));
        }
    }
    
    /**
     * Maneja el subcomando "status"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleStatusCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("savage.status")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para usar este comando."));
            return;
        }
        
        // Verificar permisos para ver el estado de otros jugadores
        if (args.length >= 2 && !sender.hasPermission("savage.status.others")) {
            ((Audience) ((Audience) sender)).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para ver el estado de otros jugadores."));
            return;
        }
        
        Player target;
        
        if (args.length >= 2) {
            // Ver estado de otro jugador
            String playerName = args[1];
            target = Bukkit.getPlayer(playerName);
            
            if (target == null) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Jugador no encontrado: " + playerName));
                return;
            }
        } else if (sender instanceof Player) {
            // Ver estado propio
            target = (Player) sender;
        } else {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Debes especificar un jugador cuando ejecutas desde la consola."));
            return;
        }
        
        // Mostrar estado del jugador
        displayPlayerStatus(sender, target);
    }
    
    /**
     * Muestra el estado completo de un jugador
     * @param sender Remitente del comando
     * @param target Jugador objetivo
     */
    private void displayPlayerStatus(CommandSender sender, Player target) {
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<gray>----------------------------------------"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<aqua>Estado de " + target.getName() + ":"));
        
        // Estado de radiación
        if (plugin.getRadiationSystem().isActive()) {
            int radiationLevel = plugin.getRadiationSystem().getPlayerRadiationLevel(target);
            String radiationStatus;
            
            if (radiationLevel <= 0) {
                radiationStatus = "<green>Sin radiación";
            } else if (radiationLevel < 30) {
                radiationStatus = "<yellow>Radiación leve (" + radiationLevel + "%)"; 
            } else if (radiationLevel < 70) {
                radiationStatus = "<gold>Radiación moderada (" + radiationLevel + "%)"; 
            } else {
                radiationStatus = "<red>Radiación grave (" + radiationLevel + "%)"; 
            }
            
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Nivel de radiación: " + radiationStatus));
        } else {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Nivel de radiación: <gray>Sistema inactivo"));
        }
        
        // Estado de sangrado
        if (plugin.getBleedingSystem().isActive()) {
            boolean isBleeding = plugin.getBleedingSystem().isPlayerBleeding(target);
            int bleedingLevel = plugin.getBleedingSystem().getPlayerBleedingLevel(target);
            
            if (isBleeding) {
                String bleedingStatus;
                
                if (bleedingLevel == 1) {
                    bleedingStatus = "<yellow>Leve";
                } else if (bleedingLevel == 2) {
                    bleedingStatus = "<gold>Moderado";
                } else {
                    bleedingStatus = "<red>Grave";
                }
                
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Estado de sangrado: " + bleedingStatus));
            } else {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Estado de sangrado: <green>Sin sangrado"));
            }
        } else {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Estado de sangrado: <gray>Sistema inactivo"));
        }
    
        
        // Estado de hidratación
        if (plugin.getHydrationSystem().isActive()) {
            @SuppressWarnings("unused")
            int hydrationLevel = plugin.getHydrationSystem().getHydrationLevel(target);
            int hydrationPercent = plugin.getHydrationSystem().getHydrationPercentage(target);
            Component hydrationBar = plugin.getHydrationSystem().getHydrationBar(target);
            String hydrationStatus;
            
            if (hydrationPercent > 70) {
                hydrationStatus = "<aqua>Bien hidratado (" + hydrationPercent + "%)";
            } else if (hydrationPercent > 30) {
                hydrationStatus = "<yellow>Deshidratación leve (" + hydrationPercent + "%)";
            } else {
                hydrationStatus = "<red>Deshidratación grave (" + hydrationPercent + "%)";
            }
            
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Nivel de hidratación: " + hydrationStatus));
            ((Audience) sender).sendMessage(Component.text("Barra de hidratación: ").color(NamedTextColor.YELLOW).append(hydrationBar));
        } else {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Nivel de hidratación: <gray>Sistema inactivo"));
        }
        
        // Estado de nutrición
        if (plugin.getNutritionSystem().isActive()) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Estado nutricional:"));
            
            // Mostrar cada tipo de nutriente
            for (com.darkbladedev.mechanics.NutritionSystem.NutrientType nutrient : 
                    com.darkbladedev.mechanics.NutritionSystem.NutrientType.values()) {
                int level = plugin.getNutritionSystem().getNutrientLevel(target, nutrient);
                String bar = MiniMessage.miniMessage().serialize(plugin.getNutritionSystem().getNutrientBar(target, nutrient));
                
                String status;
                if (level > 70) {
                    status = "<green>Óptimo";
                } else if (level > 30) {
                    status = "<yellow>Deficiente";
                } else {
                    status = "<red>Crítico";
                }
                
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<gray>  " + nutrient.getDisplayName() + ": " + 
                        status + "<gray> (" + level + "%) " + bar));
            }
        } else {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Estado nutricional: <gray>Sistema inactivo"));
        }
        
        // Estado de extremidades
        if (plugin.getLimbDamageSystem().isActive()) {
            // Mostrar estado de cada extremidad
            Component limbStatus = plugin.getLimbDamageSystem().getLimbStatusMessage(target);
            sender.sendMessage(limbStatus);
        } else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Estado de extremidades:</yellow> <gray>Sistema inactivo</gray>"));
        }
        
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>----------------------------------------</gray>"));
    }
    
    /**
     * Maneja el subcomando "hydration"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleHydrationCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("savage.hydration")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para usar este comando."));
            return;
        }
        
        // Verificar argumentos
        if (args.length < 3) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Uso: /savage hydration <set/add/remove> <cantidad> [jugador]"));
            return;
        }
        
        // Obtener acción
        String action = args[1].toLowerCase();
        if (!action.equals("set") && !action.equals("add") && !action.equals("remove")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Acción inválida. Usa set, add o remove."));
            return;
        }
        
        // Obtener cantidad
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount < 0 || amount > 20) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>La cantidad debe estar entre 0 y 20."));
                return;
            }
        } catch (NumberFormatException e) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>La cantidad debe ser un número válido."));
            return;
        }
        
        // Determinar el jugador objetivo
        Player target;
        if (args.length >= 4) {
            // Modificar hidratación de un jugador específico (requiere permiso adicional)
            if (!sender.hasPermission("savage.hydration.others")) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para modificar la hidratación de otros jugadores."));
                return;
            }
            
            String playerName = args[3];
            target = Bukkit.getPlayer(playerName);
            
            if (target == null) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Jugador no encontrado: " + playerName));
                return;
            }
        } else if (sender instanceof Player) {
            // Modificar hidratación propia
            target = (Player) sender;
        } else {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Debes especificar un jugador cuando ejecutas desde la consola."));
            return;
        }
        
        // Aplicar cambios según la acción
        switch (action) {
            case "set":
                plugin.getHydrationSystem().setHydrationLevel(target, amount);
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Nivel de hidratación de " + target.getName() + " establecido a " + amount + "."));
                break;
                
            case "add":
                plugin.getHydrationSystem().increaseHydration(target, amount);
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Aumentado el nivel de hidratación de " + target.getName() + " en " + amount + "."));
                break;
                
            case "remove":
                plugin.getHydrationSystem().decreaseHydration(target, amount);
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Reducido el nivel de hidratación de " + target.getName() + " en " + amount + "."));
                break;
        }
    }
    
    /**
     * Maneja el subcomando "nutrition"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleNutritionCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("savage.nutrition")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para usar este comando."));
            return;
        }
        
        // Verificar argumentos
        if (args.length < 4) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Uso: /savage nutrition <protein/fat/carbs/vitamins> <set/add/remove> <cantidad> [jugador]"));
            return;
        }
        
        // Obtener tipo de nutriente
        String nutrientTypeStr = args[1].toLowerCase();
        com.darkbladedev.mechanics.NutritionSystem.NutrientType nutrientType;
        
        switch (nutrientTypeStr) {
            case "protein":
                nutrientType = com.darkbladedev.mechanics.NutritionSystem.NutrientType.PROTEIN;
                break;
            case "fat":
                nutrientType = com.darkbladedev.mechanics.NutritionSystem.NutrientType.FAT;
                break;
            case "carbs":
                nutrientType = com.darkbladedev.mechanics.NutritionSystem.NutrientType.CARBS;
                break;
            case "vitamins":
                nutrientType = com.darkbladedev.mechanics.NutritionSystem.NutrientType.VITAMINS;
                break;
            default:
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Tipo de nutriente inválido. Usa protein, fat, carbs o vitamins."));
                return;
        }
        
        // Obtener acción
        String action = args[2].toLowerCase();
        if (!action.equals("set") && !action.equals("add") && !action.equals("remove")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Acción inválida. Usa set, add o remove."));
            return;
        }
        
        // Obtener cantidad
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
            if (amount < 0 || amount > 100) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>La cantidad debe estar entre 0 y 100."));
                return;
            }
        } catch (NumberFormatException e) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>La cantidad debe ser un número válido."));
            return;
        }
        
        // Determinar el jugador objetivo
        Player target;
        if (args.length >= 5) {
            // Modificar nutrición de un jugador específico (requiere permiso adicional)
            if (!sender.hasPermission("savage.nutrition.others")) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para modificar la nutrición de otros jugadores."));
                return;
            }
            
            String playerName = args[4];
            target = Bukkit.getPlayer(playerName);
            
            if (target == null) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Jugador no encontrado: " + playerName));
                return;
            }
        } else if (sender instanceof Player) {
            // Modificar nutrición propia
            target = (Player) sender;
        } else {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Debes especificar un jugador cuando ejecutas desde la consola."));
            return;
        }
        
        // Aplicar cambios según la acción
        switch (action) {
            case "set":
                plugin.getNutritionSystem().setNutrientLevel(target, nutrientType, amount);
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Nivel de " + nutrientType.getDisplayName() + " de " + target.getName() + " establecido a " + amount + "."));
                break;
                
            case "add":
                plugin.getNutritionSystem().addNutrient(target, nutrientType, amount);
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Aumentado el nivel de " + nutrientType.getDisplayName() + " de " + target.getName() + " en " + amount + "."));
                break;
                
            case "remove":
                plugin.getNutritionSystem().removeNutrient(target, nutrientType, amount);
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Reducido el nivel de " + nutrientType.getDisplayName() + " de " + target.getName() + " en " + amount + "."));
                break;
        }
    }
    
    /**
     * Maneja el subcomando "limb" para manipular el estado de daño de extremidades
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleLimbCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("savage.limb")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para usar este comando."));
            return;
        }
        
        // Verificar argumentos
        if (args.length < 4) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Uso: /savage limb <set/heal> <extremidad> <nivel> [jugador]"));
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Extremidades disponibles: head, left_arm, right_arm, left_leg, right_leg, all"));
            return;
        }
        
        // Obtener acción
        String action = args[1].toLowerCase();
        if (!action.equals("set") && !action.equals("heal")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Acción inválida. Usa set o heal."));
            return;
        }
        
        // Obtener tipo de extremidad
        String limbTypeStr = args[2].toLowerCase();
        com.darkbladedev.mechanics.LimbDamageSystem.LimbType limbType = null;
        boolean isAllLimbs = false;
        
        if (limbTypeStr.equals("all")) {
            isAllLimbs = true;
        } else {
            switch (limbTypeStr) {
                case "head":
                    limbType = com.darkbladedev.mechanics.LimbDamageSystem.LimbType.HEAD;
                    break;
                case "left_arm":
                    limbType = com.darkbladedev.mechanics.LimbDamageSystem.LimbType.LEFT_ARM;
                    break;
                case "right_arm":
                    limbType = com.darkbladedev.mechanics.LimbDamageSystem.LimbType.RIGHT_ARM;
                    break;
                case "left_leg":
                    limbType = com.darkbladedev.mechanics.LimbDamageSystem.LimbType.LEFT_LEG;
                    break;
                case "right_leg":
                    limbType = com.darkbladedev.mechanics.LimbDamageSystem.LimbType.RIGHT_LEG;
                    break;
                default:
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Extremidad inválida. Opciones: head, left_arm, right_arm, left_leg, right_leg, all"));
                    return;
            }
        }
        
        // Determinar el jugador objetivo
        Player target;
        if (args.length >= 5) {
            // Modificar extremidad de un jugador específico (requiere permiso adicional)
            if (!sender.hasPermission("savage.limb.others")) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para modificar las extremidades de otros jugadores."));
                return;
            }
            
            String playerName = args[4];
            target = Bukkit.getPlayer(playerName);
            
            if (target == null) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Jugador no encontrado: " + playerName));
                return;
            }
        } else if (sender instanceof Player) {
            // Modificar extremidad propia
            target = (Player) sender;
        } else {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Debes especificar un jugador cuando ejecutas desde la consola."));
            return;
        }
        
        // Verificar si el sistema está activo
        if (!plugin.getLimbDamageSystem().isActive()) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>El sistema de daño de extremidades está desactivado."));
            return;
        }
        
        // Ejecutar la acción correspondiente
        if (action.equals("heal")) {
            if (isAllLimbs) {
                // Curar todas las extremidades
                plugin.getLimbDamageSystem().healAllLimbs(target);
                
                if (sender != target) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Has curado todas las extremidades de " + target.getName() + "."));
                }
            } else {
                // Curar una extremidad específica
                plugin.getLimbDamageSystem().healLimb(target, limbType);
                
                if (sender != target) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Has curado la " + limbType.getDisplayName() + " de " + target.getName() + "."));
                }
            }
        } else { // action.equals("set")
            // Obtener nivel de daño
            int damageLevel;
            try {
                damageLevel = Integer.parseInt(args[3]);
                if (damageLevel < 0 || damageLevel > 100) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>El nivel de daño debe estar entre 0 y 100."));
                    return;
                }
            } catch (NumberFormatException e) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>El nivel de daño debe ser un número válido."));
                return;
            }
            
            if (isAllLimbs) {
                // Establecer el nivel de daño para todas las extremidades
                com.darkbladedev.mechanics.LimbDamageSystem.DamageState newState = null;
                
                for (com.darkbladedev.mechanics.LimbDamageSystem.LimbType limb : com.darkbladedev.mechanics.LimbDamageSystem.LimbType.values()) {
                    newState = plugin.getLimbDamageSystem().setLimbDamage(target, limb, damageLevel);
                }
                
                if (sender != target) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Has establecido el daño de todas las extremidades de " + 
                            target.getName() + " a " + damageLevel + "% (" + newState.getDisplayName() + ")."));
                }
            } else {
                // Establecer el nivel de daño para una extremidad específica
                com.darkbladedev.mechanics.LimbDamageSystem.DamageState newState = 
                        plugin.getLimbDamageSystem().setLimbDamage(target, limbType, damageLevel);
                
                if (sender != target) {
                    ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Has establecido el daño de la " + limbType.getDisplayName() + 
                            " de " + target.getName() + " a " + damageLevel + "% (" + newState.getDisplayName() + ")."));
                }
            }
        }
    }
    
    /**
     * Maneja el subcomando "config" para configurar diferentes aspectos del plugin
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleConfigCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("savage.admin")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para usar este comando."));
            return;
        }
        
        // Verificar argumentos
        if (args.length < 2) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Uso: /savage config <hydration-rate|nutrition-rate> <normal/activity> <tasa>"));
            return;
        }
        
        // Procesar subcomandos de configuración
        String configType = args[1].toLowerCase();
        
        switch (configType) {
            case "hydration-rate":
                handleHydrationRateConfig(sender, args);
                break;
                
            case "nutrition-rate":
                handleNutritionRateConfig(sender, args);
                break;
                
            default:
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Opción de configuración desconocida. Opciones disponibles: hydration-rate, nutrition-rate"));
                break;
        }
    }
    
    /**
     * Maneja la configuración de la tasa de hidratación
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleHydrationRateConfig(CommandSender sender, String[] args) {
        // Verificar si el sistema está activo
        if (!plugin.getHydrationSystem().isActive()) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>El sistema de hidratación no está activo."));
            return;
        }
        
        // Mostrar tasas actuales si no hay suficientes argumentos
        if (args.length < 4) {
            double normalRate = plugin.getHydrationSystem().getNormalDecreaseRate();
            double activityRate = plugin.getHydrationSystem().getActivityDecreaseRate();
            
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Tasas de disminución de hidratación actuales:"));
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<aqua>  Normal: <white>" + normalRate + " (" + (normalRate * 100) + "% por tick)"));
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<aqua>  Actividad: <white>" + activityRate + " (" + (activityRate * 100) + "% por tick)"));
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Uso: /savage config hydration-rate <normal/activity> <tasa>"));
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<gray>La tasa debe ser un número entre 0.0 y 1.0"));
            return;
        }
        
        // Obtener tipo de tasa
        String rateType = args[2].toLowerCase();
        if (!rateType.equals("normal") && !rateType.equals("activity")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Tipo de tasa inválido. Usa normal o activity."));
            return;
        }
        
        // Obtener valor de tasa
        double rate;
        try {
            rate = Double.parseDouble(args[3]);
            if (rate < 0.0 || rate > 1.0) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>La tasa debe estar entre 0.0 y 1.0."));
                return;
            }
        } catch (NumberFormatException e) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>La tasa debe ser un número válido."));
            return;
        }
        
        // Aplicar cambio según el tipo
        if (rateType.equals("normal")) {
            plugin.getHydrationSystem().setNormalDecreaseRate(rate);
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Tasa de disminución normal de hidratación establecida a " + rate + " (" + (rate * 100) + "% por tick)."));
        } else {
            plugin.getHydrationSystem().setActivityDecreaseRate(rate);
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Tasa de disminución de hidratación durante actividad establecida a " + rate + " (" + (rate * 100) + "% por tick)."));
        }
    }
    
    /**
     * Maneja la configuración de la tasa de nutrición
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleNutritionRateConfig(CommandSender sender, String[] args) {
        // Verificar si el sistema está activo
        if (!plugin.getNutritionSystem().isActive()) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>El sistema de nutrición no está activo."));
            return;
        }
        
        // Mostrar tasas actuales si no hay suficientes argumentos
        if (args.length < 4) {
            double normalRate = plugin.getNutritionSystem().getNormalDecreaseRate();
            double activityRate = plugin.getNutritionSystem().getActivityDecreaseRate();
            
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Tasas de disminución de nutrientes actuales:"));
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>  Normal: <white>" + normalRate + " (" + (normalRate * 100) + "% por tick)"));
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>  Actividad: <white>" + activityRate + " (" + (activityRate * 100) + "% por tick)"));
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>Uso: /savage config nutrition-rate <normal/activity> <tasa>"));
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<gray>La tasa debe ser un número entre 0.0 y 1.0"));
            return;
        }
        
        // Obtener tipo de tasa
        String rateType = args[2].toLowerCase();
        if (!rateType.equals("normal") && !rateType.equals("activity")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Tipo de tasa inválido. Usa normal o activity."));
            return;
        }
        
        // Obtener valor de tasa
        double rate;
        try {
            rate = Double.parseDouble(args[3]);
            if (rate < 0.0 || rate > 1.0) {
                ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>La tasa debe estar entre 0.0 y 1.0."));
                return;
            }
        } catch (NumberFormatException e) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>La tasa debe ser un número válido."));
            return;
        }
        
        // Aplicar cambio según el tipo
        if (rateType.equals("normal")) {
            plugin.getNutritionSystem().setNormalDecreaseRate(rate);
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Tasa de disminución normal de nutrientes establecida a " + rate + " (" + (rate * 100) + "% por tick)."));
        } else {
            plugin.getNutritionSystem().setActivityDecreaseRate(rate);
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Tasa de disminución de nutrientes durante actividad establecida a " + rate + " (" + (rate * 100) + "% por tick)."));
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // Verificar si el comando es "savage"
        if (!command.getName().equalsIgnoreCase("savage")) {
            return null;
        }
        
        // Autocompletar subcomandos
        if (args.length == 1) {
            String[] subCommands = {"help", "mob", "radiation", "bleeding", "hydration", "nutrition", "status", "limb", "config", "enchantment"};
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
            return completions;
        }
        
        // Autocompletar argumentos según el subcomando
        if (args.length >= 2) {
            switch (args[0].toLowerCase()) {
                case "mob":
                    if (args.length == 2) {
                        String[] mobTypes = {"mano", "cascarudo", "gurbo", "random"};
                        for (String mobType : mobTypes) {
                            if (mobType.startsWith(args[1].toLowerCase())) {
                                completions.add(mobType);
                            }
                        }
                    } else if (args.length == 3) {
                        // Sugerir cantidades comunes
                        String[] amounts = {"1", "5", "10", "25", "50"};
                        for (String amount : amounts) {
                            if (amount.startsWith(args[2])) {
                                completions.add(amount);
                            }
                        }
                    }
                    break;
                    
                case "snow":
                case "radiation":
                    if (args.length == 2) {
                        String[] states = {"on", "off"};
                        for (String state : states) {
                            if (state.startsWith(args[1].toLowerCase())) {
                                completions.add(state);
                            }
                        }
                    }
                    break;
                    
                case "bleeding":
                    if (args.length == 2) {
                        String[] bleedingActions = {"cure", "start"};
                        for (String action : bleedingActions) {
                            if (action.startsWith(args[1].toLowerCase())) {
                                completions.add(action);
                            }
                        }
                    } else if (args.length == 3) {
                        // Autocompletar nombres de jugadores para ambas acciones
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                                completions.add(player.getName());
                            }
                        }
                    } else if (args.length == 4 && args[1].equalsIgnoreCase("start")) {
                        // Autocompletar severidad (1-3)
                        String[] severities = {"1", "2", "3"};
                        for (String severity : severities) {
                            if (severity.startsWith(args[3])) {
                                completions.add(severity);
                            }
                        }
                    } else if (args.length == 5 && args[1].equalsIgnoreCase("start")) {
                        // Autocompletar duración (sugerencias comunes)
                        String[] durations = {"5", "10", "30", "60", "120", "300"};
                        for (String duration : durations) {
                            if (duration.startsWith(args[4])) {
                                completions.add(duration);
                            }
                        }
                    }
                    break;
                    
                case "status":
                    if (args.length == 2) {
                        // Autocompletar nombres de jugadores
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(player.getName());
                            }
                        }
                    }
                    break;
                    
                case "hydration":
                    // Segundo argumento - acción
                    if (args.length == 2) {
                        completions.add("set");
                        completions.add("add");
                        completions.add("remove");
                    }
                    // Tercer argumento - cantidad
                    else if (args.length == 3) {
                        completions.add("5");
                        completions.add("10");
                        completions.add("15");
                        completions.add("20");
                    }
                    // Cuarto argumento - nombre de jugador (opcional)
                    else if (args.length == 4) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getName().toLowerCase().startsWith(args[3].toLowerCase())) {
                                completions.add(player.getName());
                            }
                        }
                    }
                    break;
                    
                case "nutrition":
                    // Segundo argumento - tipo de nutriente
                    if (args.length == 2) {
                        completions.add("protein");
                        completions.add("fat");
                        completions.add("carbs");
                        completions.add("vitamins");
                    }
                    // Tercer argumento - acción
                    else if (args.length == 3) {
                        completions.add("set");
                        completions.add("add");
                        completions.add("remove");
                    }
                    // Cuarto argumento - cantidad
                    else if (args.length == 4) {
                        completions.add("25");
                        completions.add("50");
                        completions.add("75");
                        completions.add("100");
                    }
                    // Quinto argumento - nombre de jugador (opcional)
                    else if (args.length == 5) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getName().toLowerCase().startsWith(args[4].toLowerCase())) {
                                completions.add(player.getName());
                            }
                        }
                    }
                    break;
                    
                case "limb":
                    // Segundo argumento - acción
                    if (args.length == 2) {
                        String[] actions = {"set", "heal"};
                        for (String action : actions) {
                            if (action.startsWith(args[1].toLowerCase())) {
                                completions.add(action);
                            }
                        }
                    }
                    // Tercer argumento - tipo de extremidad
                    else if (args.length == 3) {
                        String[] limbTypes = {"head", "left_arm", "right_arm", "left_leg", "right_leg", "all"};
                        for (String limbType : limbTypes) {
                            if (limbType.startsWith(args[2].toLowerCase())) {
                                completions.add(limbType);
                            }
                        }
                    }
                    // Cuarto argumento - nivel de daño (solo para set) o nombre de jugador
                    else if (args.length == 4) {
                        if (args[1].equalsIgnoreCase("set")) {
                            String[] levels = {"0", "25", "50", "75", "100"};
                            for (String level : levels) {
                                if (level.startsWith(args[3])) {
                                    completions.add(level);
                                }
                            }
                        } else {
                            // Autocompletar nombres de jugadores
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                if (player.getName().toLowerCase().startsWith(args[3].toLowerCase())) {
                                    completions.add(player.getName());
                                }
                            }
                        }
                    }
                    // Quinto argumento - nombre de jugador (solo para set)
                    else if (args.length == 5 && args[1].equalsIgnoreCase("set")) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getName().toLowerCase().startsWith(args[4].toLowerCase())) {
                                completions.add(player.getName());
                            }
                        }
                    }
                    break;
                    
                case "config":
                    // Segundo argumento - tipo de configuración
                    if (args.length == 2) {
                        String[] configTypes = {"hydration-rate", "nutrition-rate"};
                        for (String configType : configTypes) {
                            if (configType.startsWith(args[1].toLowerCase())) {
                                completions.add(configType);
                            }
                        }
                    }
                    // Tercer argumento - tipo de tasa
                    else if (args.length == 3 && (args[1].equalsIgnoreCase("hydration-rate") || args[1].equalsIgnoreCase("nutrition-rate"))) {
                        String[] rateTypes = {"normal", "activity"};
                        for (String rateType : rateTypes) {
                            if (rateType.startsWith(args[2].toLowerCase())) {
                                completions.add(rateType);
                            }
                        }
                    }
                    // Cuarto argumento - valor de tasa
                    else if (args.length == 4 && (args[1].equalsIgnoreCase("hydration-rate") || args[1].equalsIgnoreCase("nutrition-rate"))) {
                        String[] rates = {"0.05", "0.1", "0.15", "0.2", "0.3", "0.5"};
                        for (String rate : rates) {
                            if (rate.startsWith(args[3])) {
                                completions.add(rate);
                            }
                        }
                    }
                    break;
                    
                // Mantener compatibilidad con comandos antiguos
                case "hydrationrate":
                case "nutritionrate":
                    // Segundo argumento - tipo de tasa
                    if (args.length == 2) {
                        String[] rateTypes = {"normal", "activity"};
                        for (String rateType : rateTypes) {
                            if (rateType.startsWith(args[1].toLowerCase())) {
                                completions.add(rateType);
                            }
                        }
                    }
                    // Tercer argumento - valor de tasa
                    else if (args.length == 3) {
                        String[] rates = {"0.05", "0.1", "0.15", "0.2", "0.3", "0.5"};
                        for (String rate : rates) {
                            if (rate.startsWith(args[2])) {
                                completions.add(rate);
                            }
                        }
                    }
                    break;
                    
                case "enchantment":
                    // Segundo argumento - subcomando
                    if (args.length == 2) {
                        String[] subCommands = {"give", "apply"};
                        for (String subCommand : subCommands) {
                            if (subCommand.startsWith(args[1].toLowerCase())) {
                                completions.add(subCommand);
                            }
                        }
                    }
                    // Tercer argumento - tipo de encantamiento
                    else if (args.length == 3) {
                        for (com.darkbladedev.CustomTypes.CustomEnchantments.CustomEnchantment enchantment : 
                             com.darkbladedev.CustomTypes.CustomEnchantments.CustomEnchantment.values()) {
                            if (enchantment.name().toLowerCase().startsWith(args[2].toLowerCase())) {
                                completions.add(enchantment.name().toLowerCase());
                            }
                        }
                    }
                    // Cuarto argumento - nivel o jugador
                    else if (args.length == 4) {
                        if (args[1].equalsIgnoreCase("give") || args[1].equalsIgnoreCase("apply")) {
                            // Sugerir niveles comunes
                            for (int i = 1; i <= 3; i++) {
                                if (String.valueOf(i).startsWith(args[3])) {
                                    completions.add(String.valueOf(i));
                                }
                            }
                            
                            // Si es 'give', también sugerir jugadores
                            if (args[1].equalsIgnoreCase("give")) {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    if (player.getName().toLowerCase().startsWith(args[3].toLowerCase())) {
                                        completions.add(player.getName());
                                    }
                                }
                            }
                        }
                    }
                    // Quinto argumento - jugador (solo para give con nivel especificado)
                    else if (args.length == 5 && args[1].equalsIgnoreCase("give")) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getName().toLowerCase().startsWith(args[4].toLowerCase())) {
                                completions.add(player.getName());
                            }
                        }
                    }
                    break;
            }
        }
        
        // Ordenar completions alfabéticamente
        Collections.sort(completions);
        return completions;
    }
}