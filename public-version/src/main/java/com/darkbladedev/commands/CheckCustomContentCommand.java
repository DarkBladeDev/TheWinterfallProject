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
import java.util.ArrayList;
import java.util.List;

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
        if (args.length == 0) {
            sender.sendMessage("Uso: /savage admin check-custom-content <player> <skill|stat|trait>");
            return true;
        }
        if (args.length == 1) {
            sender.sendMessage("Uso: /savage admin check-custom-content <player> <skill|stat|trait>");
            return true;
        }
        if (args.length == 2) {
            player = Bukkit.getPlayerExact(args[0]);
            if (player == null) {
                sender.sendMessage("El jugador no se ha encontrado.");
                return false;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
