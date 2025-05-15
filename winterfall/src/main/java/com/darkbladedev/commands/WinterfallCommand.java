package com.darkbladedev.commands;

import com.darkbladedev.WinterfallMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Manejador de comandos para "El Eternauta"
 * Permite a los jugadores interactuar con las funcionalidades del plugin
 */
public class WinterfallCommand implements CommandExecutor {

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
}