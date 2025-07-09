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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.ArrayList;
import java.util.List;

public class CheckCustomContentCommand implements CommandExecutor, TabCompleter {

    @SuppressWarnings("unused")
    private final SavageFrontierMain plugin;
    @SuppressWarnings("unused")
    private final String permissionReq = "savage.admin.check_custom_content";

    public CheckCustomContentCommand(SavageFrontierMain plugin) {
        this.plugin = plugin;
    }

    public void build() {
        @SuppressWarnings("unused")
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("savage")
            .then(Commands.literal("admin")
                .then(Commands.literal("check-custom-content")
                    .then(Commands.literal("skills")
                        .then(Commands.argument("player", StringArgumentType.word())
                            .executes(context -> {
                                String playerArg = context.getArgument("player", String.class);
                                // Lógica para verificar si existe la habilidad
                                Player player = Bukkit.getPlayerExact(playerArg);
                                if (player == null) {
                                    ((CommandSender) context.getSource()).sendMessage("El jugador no se ha encontrado.");
                                    return 0;
                                } else {
                                    
                                }

                                return 1; // Devuelve 1 para indicar éxito
                            })))
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
