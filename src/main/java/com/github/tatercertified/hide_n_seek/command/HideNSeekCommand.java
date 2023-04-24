package com.github.tatercertified.hide_n_seek.command;

import com.github.tatercertified.hide_n_seek.Hide_n_Seek;
import com.github.tatercertified.hide_n_seek.interfaces.ServerPlayerEntityInterface;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HideNSeekCommand {

    public static List<ServerPlayerEntity> seekers = new ArrayList<>();
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("hide-n-seek")
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(HideNSeekCommand::listCommands)
                    .then(CommandManager.literal("seeker")
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .then(CommandManager.argument("isSeeker", BoolArgumentType.bool())
                                            .executes(context -> setSeeker(context, EntityArgumentType.getPlayer(context, "player"), BoolArgumentType.getBool(context, "isSeeker"))))))
                    .then(CommandManager.literal("random_seeker")
                            .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                                    .executes(context -> setRandomSeeker(context, IntegerArgumentType.getInteger(context, "amount")))))
                    .then(CommandManager.literal("start")
                            .executes(HideNSeekCommand::startGame))
                    .then(CommandManager.literal("duration")
                            .then(CommandManager.argument("duration", IntegerArgumentType.integer())
                                    .executes(context -> setDuration(context, IntegerArgumentType.getInteger(context, "duration")))))
                    .then(CommandManager.literal("stop")
                            .executes(HideNSeekCommand::stopGame))
                    .then(CommandManager.literal("lobby")
                            .executes(HideNSeekCommand::setLobbyPos))
                    .then(CommandManager.literal("map")
                            .executes(HideNSeekCommand::setMapPos)));
        });
    }

    private static int listCommands(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity source = context.getSource().getPlayer();
        assert source != null;
        source.sendMessage(Text.literal("""
                Commands:
                /hide-n-seek - Lists all available commands
                /hide-n-seek seeker <Player> <bool> - Sets player as seeker
                /hide-n-seek random_seeker - Sets a random seeker
                /hide-n-seek start - Starts the game
                /hide-n-seek duration <int> - Sets the length of the game
                /hide-n-seek stop - Forcefully stops the game
                /hide-n-seek lobby - Sets spawn for lobby
                /hide-n-seek map - Sets spawn for map
                """));
        return 0;
    }

    private static int setSeeker(CommandContext<ServerCommandSource> context ,ServerPlayerEntity reference, boolean bool) {
        ((ServerPlayerEntityInterface)reference).setSeeker(bool);
        if (bool && !seekers.contains(reference)) {
            seekers.add(reference);
        } else if(!bool) {
            seekers.remove(reference);
        }
        ServerPlayerEntity source = context.getSource().getPlayer();
        assert source != null;
        source.sendMessage(Text.literal(reference.getName().getString() + " is seeker: " + bool));
        return 0;
    }

    private static int setRandomSeeker(CommandContext<ServerCommandSource> context, int amount) {
        List<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager().getPlayerList();
        Collections.shuffle(players);
        if (amount > players.size()) {
            amount = players.size();
        }
        for (int i = 0; i < amount; i++) {
            setSeeker(context, players.get(i), true);
        }
        return 0;
    }

    private static int startGame(CommandContext<ServerCommandSource> context) {
        if (seekers.isEmpty()) {
            ServerPlayerEntity source = context.getSource().getPlayer();
            assert source != null;
            source.sendMessage(Text.literal("You must set a seeker with /hide-n-seek seeker!"));
            return 0;
        }
        Hide_n_Seek.setCurrentGameTime(0);
        return 0;
    }

    private static int stopGame(CommandContext<ServerCommandSource> context) {
        Hide_n_Seek.setCurrentGameTime(-1);
        context.getSource().getServer().sendMessage(Text.literal("The Game has Stopped!"));
        return 0;
    }

    private static int setDuration(CommandContext<ServerCommandSource> context, int duration) {
        Hide_n_Seek.setDuration(duration);
        ServerPlayerEntity source = context.getSource().getPlayer();
        assert source != null;
        source.sendMessage(Text.literal("The game duration is now " + duration + " ticks, or " + duration/20 + " seconds"));
        return 0;
    }

    private static int setLobbyPos(CommandContext<ServerCommandSource> context) {
        Hide_n_Seek.setLobbyTeleport(context.getSource().getPlayer().getBlockPos());
        return 0;
    }

    private static int setMapPos(CommandContext<ServerCommandSource> context) {
        Hide_n_Seek.setMapTeleport(context.getSource().getPlayer().getBlockPos());
        return 0;
    }

}
