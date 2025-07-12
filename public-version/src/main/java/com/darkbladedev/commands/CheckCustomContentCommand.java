package com.darkbladedev.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.darkbladedev.SavageFrontierMain;
import com.darkbladedev.utils.AuraSkillsUtil;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.CustomSkill;
import dev.aurelium.auraskills.api.stat.CustomStat;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.trait.CustomTrait;
import dev.aurelium.auraskills.api.user.SkillsUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckCustomContentCommand implements CommandExecutor, TabCompleter {

    @SuppressWarnings("unused")
    private final SavageFrontierMain plugin;
    @SuppressWarnings("unused")
    private final String permissionReq = "savage.admin.check_custom_content";
    private static Player player;

    public CheckCustomContentCommand(SavageFrontierMain plugin) {
        this.plugin = plugin;
    }

    /* 
    public void build() {
        @SuppressWarnings("unused")
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("savage")
            .then(Commands.literal("admin")
                .then(Commands.literal("check-custom-content")
                    .then(Commands.literal("skills")
                        .then(Commands.argument("player", StringArgumentType.word())
                            .executes(ctx -> {
                                String playerArg = ctx.getArgument("player", String.class);
                                CommandSender sender = ctx.getSource().getSender(); 
                                player = Bukkit.getPlayerExact(playerArg);
                                if (player == null) {
                                    sender.sendMessage("El jugador no se ha encontrado.");
                                    return 0;
                                }
                                return 1;
                            })
                            .then(Commands.argument("skill", StringArgumentType.word())
                                .executes(ctx -> {
                                    String skillArg = ctx.getArgument("skill", String.class);
                                    CommandSender sender = ctx.getSource().getSender();
                                    AuraSkillsApi api = AuraSkillsApi.get();
                                    CustomSkill skill =  (CustomSkill) api.getGlobalRegistry().getSkill(NamespacedId.of("savage-frontier", skillArg));
                                    
                                    if (player == null) {
                                        sender.sendMessage("El jugador no se ha encontrado.");
                                        return 0;
                                    } else {
                                        sender.sendMessage(AuraSkillsUtil.verifyPlayerSkills(player, skill));
                                    }

                                    return 1; // Devuelve 1 para indicar éxito
                                }))))
                    .then(Commands.literal("stats"))
                    .then(Commands.literal("traits"))
                    )
            );
                
            // )
            // .then(Commands.literal("killall")
            //     .then(Commands.literal("entities"))
            //     .then(Commands.literal("players"))
            //     .then(Commands.literal("zombies"))

    }
    */

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Verificar permisos
        if (!sender.hasPermission(permissionReq)) {
            sender.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }
        
        // Mostrar ayuda si no hay suficientes argumentos
        if (args.length < 3) {
            sender.sendMessage("§eUso: /savage admin check-custom-content <player> <skill|stat|trait> <nombre>");
            return true;
        }
        
        // Obtener el jugador
        player = Bukkit.getPlayerExact(args[0]);
        if (player == null) {
            sender.sendMessage("§cEl jugador " + args[0] + " no se ha encontrado.");
            return true;
        }
        
        // Verificar si AuraSkills está disponible
        AuraSkillsApi api = plugin.getAuraSkillsApi();
        if (api == null) {
            sender.sendMessage("§cAuraSkills no está disponible. Asegúrate de que esté instalado y habilitado.");
            return true;
        }
        
        // Procesar el comando según el tipo de contenido
        String contentType = args[1].toLowerCase();
        String contentName = args[2].toLowerCase();
        
        switch (contentType) {
            case "skill":
                checkSkill(sender, api, contentName);
                break;
            case "stat":
                checkStat(sender, api, contentName);
                break;
            case "trait":
                checkTrait(sender, api, contentName);
                break;
            default:
                sender.sendMessage("§cTipo de contenido no válido. Usa: skill, stat o trait.");
                break;
        }
        
        return true;
    }

    /**
     * Verifica y muestra información sobre una habilidad personalizada
     * @param sender Remitente del comando
     * @param api API de AuraSkills
     * @param skillName Nombre de la habilidad
     */
    private void checkSkill(CommandSender sender, AuraSkillsApi api, String skillName) {
        try {
            // Obtener la habilidad personalizada
            CustomSkill skill = (CustomSkill) api.getGlobalRegistry().getSkill(
                    dev.aurelium.auraskills.api.registry.NamespacedId.of("savage-frontier", skillName));
            
            if (skill == null) {
                sender.sendMessage("§cLa habilidad '" + skillName + "' no existe.");
                return;
            }
            
            // Usar el método de AuraSkillsUtil para obtener información formateada
            CustomSkill[] skills = {skill};
            String result = AuraSkillsUtil.verifyPlayerSkills(player, skills);
            
            // Convertir el formato MiniMessage a formato de código de color tradicional
            result = result.replace(plugin.PREFIX, "§e===== Información de Habilidad =====§r")
                          .replace("<yellow>", "§e")
                          .replace("<gray>", "§7")
                          .replace("<green>", "§a")
                          .replace("<light_purple>", "§d")
                          .replace("<white>", "§f")
                          .replace("</light_purple>", "")
                          .replace("</gray>", "");
            
            sender.sendMessage(result);
            
        } catch (Exception e) {
            sender.sendMessage("§cError al verificar la habilidad: " + e.getMessage());
            plugin.getLogger().warning("Error al verificar habilidad: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica y muestra información sobre un stat personalizado
     * @param sender Remitente del comando
     * @param api API de AuraSkills
     * @param statName Nombre del stat
     */
    private void checkStat(CommandSender sender, AuraSkillsApi api, String statName) {
        try {
            // Obtener el stat personalizado
            CustomStat stat = (CustomStat) api.getGlobalRegistry().getStat(
                    dev.aurelium.auraskills.api.registry.NamespacedId.of("savage-frontier", statName));
            
            if (stat == null) {
                sender.sendMessage("§cEl stat '" + statName + "' no existe.");
                return;
            }
            
            // Usar el método de AuraSkillsUtil para obtener información formateada
            String result = AuraSkillsUtil.verifyPlayerTraits(player, stat);
            
            // Convertir el formato MiniMessage a formato de código de color tradicional
            result = result.replace(plugin.PREFIX, "§e===== Información de Stat =====§r")
                          .replace("<yellow>", "§e")
                          .replace("<gray>", "§7")
                          .replace("<green>", "§a")
                          .replace("<light_purple>", "§d")
                          .replace("<white>", "§f")
                          .replace("</light_purple>", "")
                          .replace("</gray>", "");
            
            sender.sendMessage(result);
            
            // Añadir información adicional sobre el nivel del stat
            SkillsUser user = api.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                sender.sendMessage("§7Nivel del stat: §f" + user.getStatLevel(stat));
            }
            
        } catch (Exception e) {
            sender.sendMessage("§cError al verificar el stat: " + e.getMessage());
            plugin.getLogger().warning("Error al verificar stat: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Verifica y muestra información sobre un trait personalizado
     * @param sender Remitente del comando
     * @param api API de AuraSkills
     * @param traitName Nombre del trait
     */
    private void checkTrait(CommandSender sender, AuraSkillsApi api, String traitName) {
        try {
            // Obtener el trait personalizado
            CustomTrait trait = plugin.getCustomTraits().getTrait(traitName);
            
            if (trait == null) {
                sender.sendMessage("§cEl trait '" + traitName + "' no existe.");
                return;
            }
            
            // Obtener el usuario de AuraSkills
            SkillsUser user = api.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                sender.sendMessage("§cNo se pudo obtener información del jugador en AuraSkills.");
                return;
            }
            
            // Obtener el valor del trait
            double traitValue = user.getEffectiveTraitLevel(trait);
            
            // Mostrar información del trait
            StringBuilder message = new StringBuilder();
            message.append("§e===== Información de Trait: ").append(trait.getDisplayName(Locale.ENGLISH)).append(" §e=====").append("\n");
            message.append("§7Jugador: §f").append(player.getName()).append("\n");
            message.append("§7Valor efectivo: §f").append(traitValue).append("\n");
            
            // Mostrar stats asociados si existen
            message.append("§7Stats asociados:").append("\n");
            boolean foundStats = false;
            
            // Buscar stats que contengan este trait
            for (Stat stat : api.getGlobalRegistry().getStats()) {
                if (stat instanceof CustomStat && 
                    stat.getId().getNamespace().equals("savage-frontier") &&
                    ((CustomStat) stat).getTraits().contains(trait)) {
                    
                    message.append("  §7- §a").append(stat.getDisplayName(Locale.ENGLISH))
                           .append(" §7(Nivel: §f").append(user.getStatLevel(stat)).append("§7)").append("\n");
                    foundStats = true;
                }
            }
            
            if (!foundStats) {
                message.append("  §7- §cNo se encontraron stats asociados a este trait.").append("\n");
            }
            
            message.append("§7---------------------------------------------------------------------");
            sender.sendMessage(message.toString());
            
        } catch (Exception e) {
            sender.sendMessage("§cError al verificar el trait: " + e.getMessage());
            plugin.getLogger().warning("Error al verificar trait: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(permissionReq)) {
            return new ArrayList<>();
        }
        
        List<String> completions = new ArrayList<>();
        
        // Autocompletar jugadores
        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(p.getName());
                }
            }
            return completions;
        }
        
        // Autocompletar tipo de contenido
        if (args.length == 2) {
            String[] contentTypes = {"skill", "stat", "trait"};
            for (String type : contentTypes) {
                if (type.startsWith(args[1].toLowerCase())) {
                    completions.add(type);
                }
            }
            return completions;
        }
        
        // Autocompletar nombre del contenido según el tipo
        if (args.length == 3) {
            AuraSkillsApi api = plugin.getAuraSkillsApi();
            if (api == null) {
                return completions;
            }
            
            String prefix = "savage-frontier";
            
            try {
                switch (args[1].toLowerCase()) {
                    case "skill":
                        // Obtener dinámicamente las habilidades personalizadas registradas
                        api.getGlobalRegistry().getSkills().forEach(skill -> {
                            if (skill instanceof CustomSkill && 
                                skill.getId().getNamespace().equals(prefix)) {
                                String skillName = skill.getId().getKey();
                                if (skillName.startsWith(args[2].toLowerCase())) {
                                    completions.add(skillName);
                                }
                            }
                        });
                        
                        // Si no hay habilidades registradas, mostrar ejemplos
                        if (completions.isEmpty()) {
                            String[] exampleSkills = {"survival", "hunting", "fishing", "mining"};
                            for (String skill : exampleSkills) {
                                if (skill.startsWith(args[2].toLowerCase())) {
                                    completions.add(skill);
                                }
                            }
                        }
                        break;
                        
                    case "stat":
                        // Obtener dinámicamente los stats personalizados registrados
                        api.getGlobalRegistry().getStats().forEach(stat -> {
                            if (stat instanceof CustomStat && 
                                stat.getId().getNamespace().equals(prefix)) {
                                String statName = stat.getId().getKey();
                                if (statName.startsWith(args[2].toLowerCase())) {
                                    completions.add(statName);
                                }
                            }
                        });
                        
                        // Si no hay stats registrados, mostrar ejemplos
                        if (completions.isEmpty() && plugin.getCustomTraits() != null) {
                            String[] exampleStats = {"endurance", "toughness", "vitality", "strength"};
                            for (String stat : exampleStats) {
                                if (stat.startsWith(args[2].toLowerCase())) {
                                    completions.add(stat);
                                }
                            }
                        }
                        break;
                        
                    case "trait":
                        // Obtener dinámicamente los traits personalizados registrados
                        api.getGlobalRegistry().getTraits().forEach(trait -> {
                            if (trait.getId().getNamespace().equals(prefix)) {
                                String traitName = trait.getId().getKey();
                                if (traitName.startsWith(args[2].toLowerCase())) {
                                    completions.add(traitName);
                                }
                            }
                        });
                        
                        // Si no hay traits registrados, mostrar ejemplos
                        if (completions.isEmpty() && plugin.getCustomTraits() != null) {
                            String[] exampleTraits = {
                                "stamina_capacity", "stamina_recovery", 
                                "limb_damage_reduction", "limb_recovery_rate", 
                                "radiation_resistance", "cold_resistance"
                            };
                            for (String trait : exampleTraits) {
                                if (trait.startsWith(args[2].toLowerCase())) {
                                    completions.add(trait);
                                }
                            }
                        }
                        break;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error al generar autocompletado: " + e.getMessage());
                if (plugin.isDebugMode()) {
                    e.printStackTrace();
                }
            }
        }
        
        return completions;
    }
}
