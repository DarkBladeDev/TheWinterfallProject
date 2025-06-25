package com.darkbladedev.commands;

import com.darkbladedev.SavageFrontierMain;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Comando para gestionar la barra de acción personalizable
 */
public class ActionBarCommand implements CommandExecutor, TabCompleter {

    private final SavageFrontierMain plugin;

    /**
     * Constructor del manejador de comandos de la barra de acción
     * @param plugin Instancia del plugin principal
     */
    public ActionBarCommand(SavageFrontierMain plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar si el comando es "actionbar"
        if (!command.getName().equalsIgnoreCase("actionbar")) {
            return false;
        }

        if (!sender.hasPermission("savage.actionbar.admin")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para usar este comando."));
            return true;
        }

        // Si no hay argumentos o el primer argumento es "help", mostrar ayuda
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            showHelp(sender);
            return true;
        }

        // Manejar subcomandos
        switch (args[0].toLowerCase()) {
            case "menu":
                handleMenuCommand(sender);
                break;
            case "maxslots":
                handleMaxSlotsCommand(sender, args);
                break;
            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    /**
     * Muestra la ayuda del comando
     * @param sender Remitente del comando
     */
    private void showHelp(CommandSender sender) {
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<gray>----------------------------------------"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<aqua>ActionBar<gray> - Comandos:"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/actionbar help<gray> - Muestra esta ayuda"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/actionbar menu<gray> - Abre el menú de configuración de la barra de acción"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<yellow>/actionbar maxslots <cantidad><gray> - Configura el número máximo de slots (Admin)"));
        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<gray>----------------------------------------"));
    }

    /**
     * Maneja el comando para abrir el menú de configuración
     * @param sender Remitente del comando
     */
    private void handleMenuCommand(CommandSender sender) {
        // Verificar si el remitente es un jugador
        if (!(sender instanceof Player player)) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Este comando solo puede ser ejecutado por un jugador."));
            return;
        }

        // Verificar permisos
        if (!player.hasPermission("savage.actionbar.menu")) {
            ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para usar este comando."));
            return;
        }

        // Abrir el menú de configuración
        plugin.getActionBarDisplayManager().openDisplayMenu(player);
        ((Audience) player).sendMessage(MiniMessage.miniMessage().deserialize("<green>Menú de configuración de la barra de acción abierto."));
    }

    /**
     * Maneja el comando para configurar el número máximo de slots
     * @param sender Remitente del comando
     * @param args Argumentos del comando
     */
    private void handleMaxSlotsCommand(CommandSender sender, String[] args) {
        // Verificar permisos
        if (!sender.hasPermission("savage.admin.actionbar")) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>No tienes permiso para usar este comando."));
            return;
        }

        // Verificar argumentos
        if (args.length < 2) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>Uso: /actionbar maxslots <cantidad>"));
            return;
        }

        // Obtener la cantidad de slots
        int maxSlots;
        try {
            maxSlots = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>La cantidad debe ser un número entero."));
            return;
        }

        // Validar la cantidad de slots
        if (maxSlots < 1 || maxSlots > 9) {
            ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<red>La cantidad de slots debe estar entre 1 y 9."));
            return;
        }

        // Guardar la configuración
        plugin.getConfig().set("actionbar.max_slots", maxSlots);
        plugin.saveConfig();

        // Reiniciar el ActionBarDisplayManager con la nueva configuración
        plugin.reloadActionBarDisplayManager(maxSlots);

        ((Audience) sender).sendMessage(MiniMessage.miniMessage().deserialize("<green>Número máximo de slots configurado a " + maxSlots + "."));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Verificar si el comando es "actionbar"
        if (!command.getName().equalsIgnoreCase("actionbar")) {
            return null;
        }

        // Autocompletar subcomandos
        if (args.length == 1) {
            String[] subCommands = {"help", "menu", "maxslots"};
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
            return completions;
        }

        // Autocompletar argumentos de subcomandos
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("maxslots") && sender.hasPermission("savage.admin.actionbar")) {
                for (int i = 1; i <= 9; i++) {
                    completions.add(String.valueOf(i));
                }
                return completions;
            }
        }

        return completions;
    }
}