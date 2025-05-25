package com.darkbladedev.commands;

import com.darkbladedev.WinterfallMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manejador de comandos para "El Eternauta"
 * Permite a los jugadores interactuar con las funcionalidades del plugin
 * Implementa TabCompleter para proporcionar autocompletado de comandos
 */
public class WinterfallCommand implements CommandExecutor, TabCompleter {

    private final WinterfallMain plugin;
    
    /**
     * Constructor del manejador de comandos
     * @param plugin Instancia del plugin principal
     */
    public WinterfallCommand(WinterfallMain plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar si el comando es "winterfall"
        if (!command.getName().equalsIgnoreCase("winterfall")) {
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
                
            case "item":
                handleItemCommand(sender, args);
                break;
                
            case "mob":
                handleMobCommand(sender, args);
                break;
                
            case "snow":
                handleSnowCommand(sender, args);
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
                
            case "hydrationrate":
                handleHydrationRateCommand(sender, args);
                break;
                
            case "nutritionrate":
                handleNutritionRateCommand(sender, args);
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Subcomando desconocido. Usa /winterfall help para ver los comandos disponibles.");
                break;
        }
        
        return true;
    }
    
    /**
     * Muestra la ayuda del plugin
     * @param sender Remitente del comando
     */
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "----------------------------------------");
        sender.sendMessage(ChatColor.AQUA + "Winterfall - El Eternauta" + ChatColor.GRAY + " - Comandos:");
        sender.sendMessage(ChatColor.YELLOW + "/winterfall help" + ChatColor.GRAY + " - Muestra esta ayuda");
        sender.sendMessage(ChatColor.YELLOW + "/winterfall item <tipo>" + ChatColor.GRAY + " - Obtiene un ítem especial");
        sender.sendMessage(ChatColor.YELLOW + "/winterfall mob <tipo> [cantidad]" + ChatColor.GRAY + " - Genera mobs alienígenas");
        sender.sendMessage(ChatColor.YELLOW + "/winterfall snow <on/off>" + ChatColor.GRAY + " - Activa/desactiva la nevada tóxica");
        sender.sendMessage(ChatColor.YELLOW + "/winterfall radiation <on/off>" + ChatColor.GRAY + " - Activa/desactiva la radiación");
        sender.sendMessage(ChatColor.YELLOW + "/winterfall bleeding <cure> [jugador]" + ChatColor.GRAY + " - Cura el sangrado");
        sender.sendMessage(ChatColor.YELLOW + "/winterfall hydration <set/add/remove> <cantidad> [jugador]" + ChatColor.GRAY + " - Gestiona la hidratación");
        sender.sendMessage(ChatColor.YELLOW + "/winterfall nutrition <protein/fat/carbs/vitamins> <set/add/remove> <cantidad> [jugador]" + ChatColor.GRAY + " - Gestiona la nutrición");
        sender.sendMessage(ChatColor.YELLOW + "/winterfall limb <set/heal> <extremidad> <nivel> [jugador]" + ChatColor.GRAY + " - Gestiona el daño de extremidades");
        sender.sendMessage(ChatColor.YELLOW + "/winterfall status [jugador]" + ChatColor.GRAY + " - Muestra el estado físico del jugador");
        sender.sendMessage(ChatColor.YELLOW + "/winterfall hydrationrate <normal/activity> <tasa>" + ChatColor.GRAY + " - Ajusta la velocidad de disminución de hidratación");
        sender.sendMessage(ChatColor.YELLOW + "/winterfall nutritionrate <normal/activity> <tasa>" + ChatColor.GRAY + " - Ajusta la velocidad de disminución de nutrientes");
        sender.sendMessage(ChatColor.GRAY + "----------------------------------------");
    }
    
    /**
     * Maneja el subcomando "item"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleItemCommand(CommandSender sender, String[] args) {
        // Verificar si el remitente es un jugador
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador.");
            return;
        }
        
        Player player = (Player) sender;
        
        // Verificar permisos
        if (!player.hasPermission("winterfall.item")) {
            player.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return;
        }
        
        // Verificar argumentos
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /winterfall item <tipo>");
            player.sendMessage(ChatColor.GRAY + "Tipos disponibles: isolation_helmet, isolation_chestplate, isolation_leggings, isolation_boots, flamethrower, electric_gun, full_suit");
            return;
        }
        
        // Procesar tipo de ítem
        String itemType = args[1].toLowerCase();
        ItemStack item = null;
        
        if (itemType.equals("full_suit")) {
            // Dar traje completo
            player.getInventory().addItem(plugin.getItemManager().getItem("isolation_helmet"));
            player.getInventory().addItem(plugin.getItemManager().getItem("isolation_chestplate"));
            player.getInventory().addItem(plugin.getItemManager().getItem("isolation_leggings"));
            player.getInventory().addItem(plugin.getItemManager().getItem("isolation_boots"));
            player.sendMessage(ChatColor.GREEN + "Has recibido el traje aislante completo.");
            return;
        } else {
            // Dar ítem individual
            item = plugin.getItemManager().getItem(itemType);
        }
        
        // Verificar si el ítem existe
        if (item == null) {
            player.sendMessage(ChatColor.RED + "El ítem especificado no existe.");
            return;
        }
        
        // Dar ítem al jugador
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "Has recibido el ítem: " + item.getItemMeta().getDisplayName());
    }
    
    /**
     * Maneja el subcomando "mob"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleMobCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("winterfall.mob")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return;
        }
        
        // Verificar argumentos
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /winterfall mob <tipo> [cantidad]");
            sender.sendMessage(ChatColor.GRAY + "Tipos disponibles: mano, cascarudo, gurbo, random");
            return;
        }
        
        // Obtener tipo de mob
        String mobType = args[1].toLowerCase();
        
        // Obtener cantidad (por defecto 1)
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                amount = Math.max(1, Math.min(amount, 50)); // Limitar entre 1 y 50
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "La cantidad debe ser un número válido.");
                return;
            }
        }
        
        // Verificar si el remitente es un jugador o consola
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            // Generar mobs según el tipo
            switch (mobType) {
                case "mano":
                    for (int i = 0; i < amount; i++) {
                        plugin.getMobManager().spawnMano(player.getLocation());
                    }
                    player.sendMessage(ChatColor.GREEN + "Has generado " + amount + " Mano(s).");
                    break;
                    
                case "cascarudo":
                    for (int i = 0; i < amount; i++) {
                        plugin.getMobManager().spawnCascarudo(player.getLocation());
                    }
                    player.sendMessage(ChatColor.GREEN + "Has generado " + amount + " Cascarudo(s).");
                    break;
                    
                case "gurbo":
                    for (int i = 0; i < amount; i++) {
                        plugin.getMobManager().spawnGurbo(player.getLocation());
                    }
                    player.sendMessage(ChatColor.GREEN + "Has generado " + amount + " Gurbo(s).");
                    break;
                    
                case "random":
                    plugin.getMobManager().spawnRandomAliens(player.getWorld(), amount);
                    player.sendMessage(ChatColor.GREEN + "Has generado " + amount + " alienígenas aleatorios.");
                    break;
                    
                default:
                    player.sendMessage(ChatColor.RED + "Tipo de mob desconocido. Usa mano, cascarudo, gurbo o random.");
                    break;
            }
        } else {
            // Si es la consola, generar en el mundo principal
            World world = Bukkit.getWorlds().get(0);
            
            if (mobType.equals("random")) {
                plugin.getMobManager().spawnRandomAliens(world, amount);
                sender.sendMessage(ChatColor.GREEN + "Has generado " + amount + " alienígenas aleatorios en el mundo principal.");
            } else {
                sender.sendMessage(ChatColor.RED + "La consola solo puede generar mobs aleatorios. Usa /winterfall mob random <cantidad>");
            }
        }
    }
    
    /**
     * Maneja el subcomando "snow"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleSnowCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("winterfall.snow")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return;
        }
        
        // Verificar argumentos
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /winterfall snow <on/off>");
            return;
        }
        
        // Procesar estado
        String state = args[1].toLowerCase();
        
        if (state.equals("on")) {
            // Activar nevada en el mundo actual o todos los mundos
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String worldName = player.getWorld().getName();
                
                plugin.getSnowfallSystem().setWorldEnabled(worldName, true);
                player.sendMessage(ChatColor.GREEN + "Nevada tóxica activada en el mundo: " + worldName);
            } else {
                // Activar en todos los mundos
                for (World world : Bukkit.getWorlds()) {
                    plugin.getSnowfallSystem().setWorldEnabled(world.getName(), true);
                }
                sender.sendMessage(ChatColor.GREEN + "Nevada tóxica activada en todos los mundos.");
            }
        } else if (state.equals("off")) {
            // Desactivar nevada en el mundo actual o todos los mundos
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String worldName = player.getWorld().getName();
                
                plugin.getSnowfallSystem().setWorldEnabled(worldName, false);
                player.sendMessage(ChatColor.GREEN + "Nevada tóxica desactivada en el mundo: " + worldName);
            } else {
                // Desactivar en todos los mundos
                for (World world : Bukkit.getWorlds()) {
                    plugin.getSnowfallSystem().setWorldEnabled(world.getName(), false);
                }
                sender.sendMessage(ChatColor.GREEN + "Nevada tóxica desactivada en todos los mundos.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Estado desconocido. Usa 'on' o 'off'.");
        }
    }
    
    /**
     * Maneja el subcomando "radiation"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleRadiationCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("winterfall.radiation")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return;
        }
        
        // Verificar argumentos
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /winterfall radiation <on/off>");
            return;
        }
        
        // Procesar estado
        String state = args[1].toLowerCase();
        
        if (state.equals("on")) {
            // Activar sistema de radiación
            if (!plugin.getRadiationSystem().isActive()) {
                plugin.getRadiationSystem().initialize();
                sender.sendMessage(ChatColor.GREEN + "Sistema de radiación activado.");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "El sistema de radiación ya está activo.");
            }
        } else if (state.equals("off")) {
            // Desactivar sistema de radiación
            if (plugin.getRadiationSystem().isActive()) {
                plugin.getRadiationSystem().shutdown();
                sender.sendMessage(ChatColor.GREEN + "Sistema de radiación desactivado.");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "El sistema de radiación ya está inactivo.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Estado desconocido. Usa 'on' o 'off'.");
        }
    }
    
    /**
     * Maneja el subcomando "bleeding"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleBleedingCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("winterfall.bleeding")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return;
        }
        
        // Verificar argumentos
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /winterfall bleeding <cure> [jugador]");
            return;
        }
        
        // Procesar acción
        String action = args[1].toLowerCase();
        
        if (action.equals("cure")) {
            // Curar sangrado
            if (args.length >= 3) {
                // Curar a un jugador específico
                String playerName = args[2];
                Player target = Bukkit.getPlayer(playerName);
                
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Jugador no encontrado: " + playerName);
                    return;
                }
                
                plugin.getBleedingSystem().stopBleeding(target);
                sender.sendMessage(ChatColor.GREEN + "Has curado el sangrado de " + target.getName() + ".");
                target.sendMessage(ChatColor.GREEN + "Tu sangrado ha sido curado por " + sender.getName() + ".");
            } else if (sender instanceof Player) {
                // Curar al remitente
                Player player = (Player) sender;
                plugin.getBleedingSystem().stopBleeding(player);
                player.sendMessage(ChatColor.GREEN + "Has curado tu sangrado.");
            } else {
                sender.sendMessage(ChatColor.RED + "Debes especificar un jugador cuando ejecutas desde la consola.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Acción desconocida. Usa 'cure'.");
        }
    }
    
    /**
     * Maneja el subcomando "status"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleStatusCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("winterfall.status")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return;
        }
        
        Player target;
        
        // Determinar el jugador objetivo
        if (args.length >= 2) {
            // Ver estado de un jugador específico (requiere permiso adicional)
            if (!sender.hasPermission("winterfall.status.others")) {
                sender.sendMessage(ChatColor.RED + "No tienes permiso para ver el estado de otros jugadores.");
                return;
            }
            
            String playerName = args[1];
            target = Bukkit.getPlayer(playerName);
            
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Jugador no encontrado: " + playerName);
                return;
            }
        } else if (sender instanceof Player) {
            // Ver estado propio
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Debes especificar un jugador cuando ejecutas desde la consola.");
            return;
        }
        
        // Mostrar información de estado
        showPlayerStatus(sender, target);
    }
    
    /**
     * Muestra el estado completo de un jugador
     * @param sender Remitente del comando
     * @param target Jugador objetivo
     */
    private void showPlayerStatus(CommandSender sender, Player target) {
        sender.sendMessage(ChatColor.GRAY + "----------------------------------------");
        sender.sendMessage(ChatColor.AQUA + "Estado de " + target.getName() + ":");
        
        // Estado de radiación
        if (plugin.getRadiationSystem().isActive()) {
            int radiationLevel = plugin.getRadiationSystem().getPlayerRadiationLevel(target);
            String radiationStatus;
            
            if (radiationLevel <= 0) {
                radiationStatus = ChatColor.GREEN + "Sin radiación";
            } else if (radiationLevel < 30) {
                radiationStatus = ChatColor.YELLOW + "Radiación leve (" + radiationLevel + "%)"; 
            } else if (radiationLevel < 70) {
                radiationStatus = ChatColor.GOLD + "Radiación moderada (" + radiationLevel + "%)"; 
            } else {
                radiationStatus = ChatColor.RED + "Radiación grave (" + radiationLevel + "%)"; 
            }
            
            sender.sendMessage(ChatColor.YELLOW + "Nivel de radiación: " + radiationStatus);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Nivel de radiación: " + ChatColor.GRAY + "Sistema inactivo");
        }
        
        // Estado de sangrado
        if (plugin.getBleedingSystem().isActive()) {
            boolean isBleeding = plugin.getBleedingSystem().isPlayerBleeding(target);
            int bleedingLevel = plugin.getBleedingSystem().getPlayerBleedingLevel(target);
            
            if (isBleeding) {
                String bleedingStatus;
                
                if (bleedingLevel == 1) {
                    bleedingStatus = ChatColor.YELLOW + "Leve";
                } else if (bleedingLevel == 2) {
                    bleedingStatus = ChatColor.GOLD + "Moderado";
                } else {
                    bleedingStatus = ChatColor.RED + "Grave";
                }
                
                sender.sendMessage(ChatColor.YELLOW + "Estado de sangrado: " + bleedingStatus);
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Estado de sangrado: " + ChatColor.GREEN + "Sin sangrado");
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Estado de sangrado: " + ChatColor.GRAY + "Sistema inactivo");
        }
        
        // Estado de hidratación
        if (plugin.getHydrationSystem().isActive()) {
            @SuppressWarnings("unused")
            int hydrationLevel = plugin.getHydrationSystem().getHydrationLevel(target);
            int hydrationPercent = plugin.getHydrationSystem().getHydrationPercentage(target);
            String hydrationBar = plugin.getHydrationSystem().getHydrationBar(target);
            String hydrationStatus;
            
            if (hydrationPercent > 70) {
                hydrationStatus = ChatColor.AQUA + "Bien hidratado (" + hydrationPercent + "%)"; 
            } else if (hydrationPercent > 30) {
                hydrationStatus = ChatColor.YELLOW + "Deshidratación leve (" + hydrationPercent + "%)"; 
            } else {
                hydrationStatus = ChatColor.RED + "Deshidratación grave (" + hydrationPercent + "%)"; 
            }
            
            sender.sendMessage(ChatColor.YELLOW + "Nivel de hidratación: " + hydrationStatus);
            sender.sendMessage(ChatColor.YELLOW + "Barra de hidratación: " + hydrationBar);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Nivel de hidratación: " + ChatColor.GRAY + "Sistema inactivo");
        }
        
        // Estado de nutrición
        if (plugin.getNutritionSystem().isActive()) {
            sender.sendMessage(ChatColor.YELLOW + "Estado nutricional:");
            
            // Mostrar cada tipo de nutriente
            for (com.darkbladedev.mechanics.NutritionSystem.NutrientType nutrient : 
                    com.darkbladedev.mechanics.NutritionSystem.NutrientType.values()) {
                int level = plugin.getNutritionSystem().getNutrientLevel(target, nutrient);
                String bar = plugin.getNutritionSystem().getNutrientBar(target, nutrient);
                
                String status;
                if (level > 70) {
                    status = ChatColor.GREEN + "Óptimo";
                } else if (level > 30) {
                    status = ChatColor.YELLOW + "Deficiente";
                } else {
                    status = ChatColor.RED + "Crítico";
                }
                
                sender.sendMessage(ChatColor.GRAY + "  " + nutrient.getDisplayName() + ": " + 
                        status + ChatColor.GRAY + " (" + level + "%) " + bar);
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Estado nutricional: " + ChatColor.GRAY + "Sistema inactivo");
        }
        
        // Estado de extremidades
        if (plugin.getLimbDamageSystem().isActive()) {
            // Mostrar estado de cada extremidad
            String limbStatus = plugin.getLimbDamageSystem().getLimbStatusMessage(target);
            sender.sendMessage(limbStatus);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Estado de extremidades: " + ChatColor.GRAY + "Sistema inactivo");
        }
        
        sender.sendMessage(ChatColor.GRAY + "----------------------------------------");
    }
    
    /**
     * Maneja el subcomando "hydration"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleHydrationCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("winterfall.hydration")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return;
        }
        
        // Verificar argumentos
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /winterfall hydration <set/add/remove> <cantidad> [jugador]");
            return;
        }
        
        // Obtener acción
        String action = args[1].toLowerCase();
        if (!action.equals("set") && !action.equals("add") && !action.equals("remove")) {
            sender.sendMessage(ChatColor.RED + "Acción inválida. Usa set, add o remove.");
            return;
        }
        
        // Obtener cantidad
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount < 0 || amount > 20) {
                sender.sendMessage(ChatColor.RED + "La cantidad debe estar entre 0 y 20.");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "La cantidad debe ser un número válido.");
            return;
        }
        
        // Determinar el jugador objetivo
        Player target;
        if (args.length >= 4) {
            // Modificar hidratación de un jugador específico (requiere permiso adicional)
            if (!sender.hasPermission("winterfall.hydration.others")) {
                sender.sendMessage(ChatColor.RED + "No tienes permiso para modificar la hidratación de otros jugadores.");
                return;
            }
            
            String playerName = args[3];
            target = Bukkit.getPlayer(playerName);
            
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Jugador no encontrado: " + playerName);
                return;
            }
        } else if (sender instanceof Player) {
            // Modificar hidratación propia
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Debes especificar un jugador cuando ejecutas desde la consola.");
            return;
        }
        
        // Aplicar cambios según la acción
        switch (action) {
            case "set":
                plugin.getHydrationSystem().setHydrationLevel(target, amount);
                sender.sendMessage(ChatColor.GREEN + "Nivel de hidratación de " + target.getName() + " establecido a " + amount + ".");
                break;
                
            case "add":
                plugin.getHydrationSystem().increaseHydration(target, amount);
                sender.sendMessage(ChatColor.GREEN + "Aumentado el nivel de hidratación de " + target.getName() + " en " + amount + ".");
                break;
                
            case "remove":
                plugin.getHydrationSystem().decreaseHydration(target, amount);
                sender.sendMessage(ChatColor.GREEN + "Reducido el nivel de hidratación de " + target.getName() + " en " + amount + ".");
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
        if (!sender.hasPermission("winterfall.nutrition")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return;
        }
        
        // Verificar argumentos
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Uso: /winterfall nutrition <protein/fat/carbs/vitamins> <set/add/remove> <cantidad> [jugador]");
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
                sender.sendMessage(ChatColor.RED + "Tipo de nutriente inválido. Usa protein, fat, carbs o vitamins.");
                return;
        }
        
        // Obtener acción
        String action = args[2].toLowerCase();
        if (!action.equals("set") && !action.equals("add") && !action.equals("remove")) {
            sender.sendMessage(ChatColor.RED + "Acción inválida. Usa set, add o remove.");
            return;
        }
        
        // Obtener cantidad
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
            if (amount < 0 || amount > 100) {
                sender.sendMessage(ChatColor.RED + "La cantidad debe estar entre 0 y 100.");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "La cantidad debe ser un número válido.");
            return;
        }
        
        // Determinar el jugador objetivo
        Player target;
        if (args.length >= 5) {
            // Modificar nutrición de un jugador específico (requiere permiso adicional)
            if (!sender.hasPermission("winterfall.nutrition.others")) {
                sender.sendMessage(ChatColor.RED + "No tienes permiso para modificar la nutrición de otros jugadores.");
                return;
            }
            
            String playerName = args[4];
            target = Bukkit.getPlayer(playerName);
            
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Jugador no encontrado: " + playerName);
                return;
            }
        } else if (sender instanceof Player) {
            // Modificar nutrición propia
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Debes especificar un jugador cuando ejecutas desde la consola.");
            return;
        }
        
        // Aplicar cambios según la acción
        switch (action) {
            case "set":
                plugin.getNutritionSystem().setNutrientLevel(target, nutrientType, amount);
                sender.sendMessage(ChatColor.GREEN + "Nivel de " + nutrientType.getDisplayName() + " de " + target.getName() + " establecido a " + amount + ".");
                break;
                
            case "add":
                plugin.getNutritionSystem().addNutrient(target, nutrientType, amount);
                sender.sendMessage(ChatColor.GREEN + "Aumentado el nivel de " + nutrientType.getDisplayName() + " de " + target.getName() + " en " + amount + ".");
                break;
                
            case "remove":
                plugin.getNutritionSystem().removeNutrient(target, nutrientType, amount);
                sender.sendMessage(ChatColor.GREEN + "Reducido el nivel de " + nutrientType.getDisplayName() + " de " + target.getName() + " en " + amount + ".");
                break;
        }
    }
    
    /**
     * Maneja el subcomando "hydrationrate"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleHydrationRateCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("winterfall.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return;
        }
        
        // Verificar si el sistema está activo
        if (!plugin.getHydrationSystem().isActive()) {
            sender.sendMessage(ChatColor.RED + "El sistema de hidratación no está activo.");
            return;
        }
        
        // Mostrar tasas actuales si no hay suficientes argumentos
        if (args.length < 3) {
            double normalRate = plugin.getHydrationSystem().getNormalDecreaseRate();
            double activityRate = plugin.getHydrationSystem().getActivityDecreaseRate();
            
            sender.sendMessage(ChatColor.YELLOW + "Tasas de disminución de hidratación actuales:");
            sender.sendMessage(ChatColor.AQUA + "  Normal: " + ChatColor.WHITE + normalRate + " (" + (normalRate * 100) + "% por tick)");
            sender.sendMessage(ChatColor.AQUA + "  Actividad: " + ChatColor.WHITE + activityRate + " (" + (activityRate * 100) + "% por tick)");
            sender.sendMessage(ChatColor.YELLOW + "Uso: /winterfall hydrationrate <normal/activity> <tasa>");
            sender.sendMessage(ChatColor.GRAY + "La tasa debe ser un número entre 0.0 y 1.0");
            return;
        }
        
        // Obtener tipo de tasa
        String rateType = args[1].toLowerCase();
        if (!rateType.equals("normal") && !rateType.equals("activity")) {
            sender.sendMessage(ChatColor.RED + "Tipo de tasa inválido. Usa normal o activity.");
            return;
        }
        
        // Obtener valor de tasa
        double rate;
        try {
            rate = Double.parseDouble(args[2]);
            if (rate < 0.0 || rate > 1.0) {
                sender.sendMessage(ChatColor.RED + "La tasa debe estar entre 0.0 y 1.0.");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "La tasa debe ser un número válido.");
            return;
        }
        
        // Aplicar cambio según el tipo
        if (rateType.equals("normal")) {
            plugin.getHydrationSystem().setNormalDecreaseRate(rate);
            sender.sendMessage(ChatColor.GREEN + "Tasa de disminución normal de hidratación establecida a " + rate + " (" + (rate * 100) + "% por tick).");
        } else {
            plugin.getHydrationSystem().setActivityDecreaseRate(rate);
            sender.sendMessage(ChatColor.GREEN + "Tasa de disminución de hidratación durante actividad establecida a " + rate + " (" + (rate * 100) + "% por tick).");
        }
    }
    
    /**
     * Maneja el subcomando "nutritionrate"
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleNutritionRateCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("winterfall.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return;
        }
        
        // Verificar si el sistema está activo
        if (!plugin.getNutritionSystem().isActive()) {
            sender.sendMessage(ChatColor.RED + "El sistema de nutrición no está activo.");
            return;
        }
        
        // Mostrar tasas actuales si no hay suficientes argumentos
        if (args.length < 3) {
            double normalRate = plugin.getNutritionSystem().getNormalDecreaseRate();
            double activityRate = plugin.getNutritionSystem().getActivityDecreaseRate();
            
            sender.sendMessage(ChatColor.YELLOW + "Tasas de disminución de nutrientes actuales:");
            sender.sendMessage(ChatColor.GREEN + "  Normal: " + ChatColor.WHITE + normalRate + " (" + (normalRate * 100) + "% por tick)");
            sender.sendMessage(ChatColor.GREEN + "  Actividad: " + ChatColor.WHITE + activityRate + " (" + (activityRate * 100) + "% por tick)");
            sender.sendMessage(ChatColor.YELLOW + "Uso: /winterfall nutritionrate <normal/activity> <tasa>");
            sender.sendMessage(ChatColor.GRAY + "La tasa debe ser un número entre 0.0 y 1.0");
            return;
        }
        
        // Obtener tipo de tasa
        String rateType = args[1].toLowerCase();
        if (!rateType.equals("normal") && !rateType.equals("activity")) {
            sender.sendMessage(ChatColor.RED + "Tipo de tasa inválido. Usa normal o activity.");
            return;
        }
        
        // Obtener valor de tasa
        double rate;
        try {
            rate = Double.parseDouble(args[2]);
            if (rate < 0.0 || rate > 1.0) {
                sender.sendMessage(ChatColor.RED + "La tasa debe estar entre 0.0 y 1.0.");
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "La tasa debe ser un número válido.");
            return;
        }
        
        // Aplicar cambio según el tipo
        if (rateType.equals("normal")) {
            plugin.getNutritionSystem().setNormalDecreaseRate(rate);
            sender.sendMessage(ChatColor.GREEN + "Tasa de disminución normal de nutrientes establecida a " + rate + " (" + (rate * 100) + "% por tick).");
        } else {
            plugin.getNutritionSystem().setActivityDecreaseRate(rate);
            sender.sendMessage(ChatColor.GREEN + "Tasa de disminución de nutrientes durante actividad establecida a " + rate + " (" + (rate * 100) + "% por tick).");
        }
    }
    
    /**
     * Maneja el subcomando "limb" para manipular el estado de daño de extremidades
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleLimbCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("winterfall.limb")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return;
        }
        
        // Verificar argumentos
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Uso: /winterfall limb <set/heal> <extremidad> <nivel> [jugador]");
            sender.sendMessage(ChatColor.YELLOW + "Extremidades disponibles: head, left_arm, right_arm, left_leg, right_leg");
            return;
        }
        
        // Obtener acción
        String action = args[1].toLowerCase();
        if (!action.equals("set") && !action.equals("heal")) {
            sender.sendMessage(ChatColor.RED + "Acción inválida. Usa set o heal.");
            return;
        }
        
        // Obtener tipo de extremidad
        String limbTypeStr = args[2].toLowerCase();
        com.darkbladedev.mechanics.LimbDamageSystem.LimbType limbType = null;
        
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
                sender.sendMessage(ChatColor.RED + "Extremidad inválida. Opciones: head, left_arm, right_arm, left_leg, right_leg");
                return;
        }
        
        // Determinar el jugador objetivo
        Player target;
        if (args.length >= 5) {
            // Modificar extremidad de un jugador específico (requiere permiso adicional)
            if (!sender.hasPermission("winterfall.limb.others")) {
                sender.sendMessage(ChatColor.RED + "No tienes permiso para modificar las extremidades de otros jugadores.");
                return;
            }
            
            String playerName = args[4];
            target = Bukkit.getPlayer(playerName);
            
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Jugador no encontrado: " + playerName);
                return;
            }
        } else if (sender instanceof Player) {
            // Modificar extremidad propia
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Debes especificar un jugador cuando ejecutas desde la consola.");
            return;
        }
        
        // Verificar si el sistema está activo
        if (!plugin.getLimbDamageSystem().isActive()) {
            sender.sendMessage(ChatColor.RED + "El sistema de daño de extremidades está desactivado.");
            return;
        }
        
        // Ejecutar la acción correspondiente
        if (action.equals("heal")) {
            // Curar la extremidad
            plugin.getLimbDamageSystem().healLimb(target, limbType);
            
            if (sender != target) {
                sender.sendMessage(ChatColor.GREEN + "Has curado la " + limbType.getDisplayName() + " de " + target.getName() + ".");
            }
        } else { // action.equals("set")
            // Obtener nivel de daño
            int damageLevel;
            try {
                damageLevel = Integer.parseInt(args[3]);
                if (damageLevel < 0 || damageLevel > 100) {
                    sender.sendMessage(ChatColor.RED + "El nivel de daño debe estar entre 0 y 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "El nivel de daño debe ser un número válido.");
                return;
            }
            
            // Establecer el nivel de daño
            com.darkbladedev.mechanics.LimbDamageSystem.DamageState newState = 
                    plugin.getLimbDamageSystem().setLimbDamage(target, limbType, damageLevel);
            
            if (sender != target) {
                sender.sendMessage(ChatColor.GREEN + "Has establecido el daño de la " + limbType.getDisplayName() + 
                        " de " + target.getName() + " a " + damageLevel + "% (" + newState.getDisplayName() + ").");
            }
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // Verificar si el comando es "winterfall"
        if (!command.getName().equalsIgnoreCase("winterfall")) {
            return null;
        }
        
        // Autocompletar subcomandos
        if (args.length == 1) {
            String[] subCommands = {"help", "item", "mob", "snow", "radiation", "bleeding", "hydration", "nutrition", "status", "limb", "hydrationrate", "nutritionrate"};
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
                case "item":
                    if (args.length == 2) {
                        String[] itemTypes = {"isolation_helmet", "isolation_chestplate", "isolation_leggings", "isolation_boots", "flamethrower", "electric_gun", "full_suit"};
                        for (String itemType : itemTypes) {
                            if (itemType.startsWith(args[1].toLowerCase())) {
                                completions.add(itemType);
                            }
                        }
                    }
                    break;
                    
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
                        if ("cure".startsWith(args[1].toLowerCase())) {
                            completions.add("cure");
                        }
                    } else if (args.length == 3 && args[1].equalsIgnoreCase("cure")) {
                        // Autocompletar nombres de jugadores
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                                completions.add(player.getName());
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
                        String[] limbTypes = {"head", "left_arm", "right_arm", "left_leg", "right_leg"};
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
            }
        }
        
        // Ordenar completions alfabéticamente
        Collections.sort(completions);
        return completions;
    }
}