package com.github.tatercertified.hide_n_seek;

import com.github.tatercertified.hide_n_seek.command.HideNSeekCommand;
import com.github.tatercertified.hide_n_seek.events.Event;
import com.github.tatercertified.hide_n_seek.events.Json;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;

public class Hide_n_Seek implements ModInitializer {
    public static List<Event> registered_events = new ArrayList<>();
    private static int duration = 0;
    private static int current_time = -1;
    private int countdown_usable;
    private int countdown = 0;
    private static BlockPos lobby;
    private static BlockPos map;
    private final ServerBossBar bar = new ServerBossBar(null, BossBar.Color.BLUE, BossBar.Style.PROGRESS);


    @Override
    public void onInitialize() {
        HideNSeekCommand.register();
        Json.serializeData();
        List<Event> events = Json.deserializeData();
        for (Event event : events) {
            event.classify();
        }
        events.clear();
        Config.config();

        ServerLifecycleEvents.SERVER_STOPPING.register(Config::saveConfig);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            setDuration(Config.duration);
            setLobbyTeleport(Config.lobby);
            setMapTeleport(Config.map);
        });

        ServerTickEvents.START_SERVER_TICK.register(this::tickGame);
    }

    public void tickGame(MinecraftServer server) {
        if (current_time > -1 && duration > 0) {
            if (countdown_usable <= 0) {
                if (current_time < duration) {
                    current_time++;
                    checkForEvents(server);
                    tickBossBarTimer();
                    if (!HideNSeekCommand.seekers.isEmpty()) {
                        gameOver(getAllAlivePlayers(server), server);
                    }
                    if (!checkForAliveHiders(server)) {
                        gameOver(getNameFromPlayers(HideNSeekCommand.seekers), server);
                    }
                } else {
                    //Game Over
                    gameOver(getAllAlivePlayers(server), server);
                }
            } else {
                countdown(server);
            }
        }
    }

    private void countdown(MinecraftServer server) {
        int seconds = Math.round((float) countdown_usable/20);
        boolean time = seconds == 0;
        for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(i);
            if (!HideNSeekCommand.seekers.contains(player)) {
                if (time) {
                    player.sendMessage(Text.literal("Start!"), true);
                    player.teleport(map.getX(), map.getY(), map.getZ());
                    this.bar.addPlayer(player);
                } else {
                    player.sendMessage(Text.literal(String.valueOf(seconds)), true);
                }
            }
        }
        this.countdown_usable--;
    }

    private void checkForEvents(MinecraftServer server) {
        for (int i = 0; i < Hide_n_Seek.registered_events.size(); i++) {
            Event event = Hide_n_Seek.registered_events.get(i);
            if (current_time == event.getTime()) {
                event.setServer(server);
                event.event();
            }
        }
    }

    private void gameOver(List<String> winners, MinecraftServer server) {
        setCurrentGameTime(-1);
        server.sendMessage(Text.literal("The Game is Over!"));
        for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(i);
            player.sendMessage(Text.literal(String.join(", ", winners) + " have won the game!"), true);
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 100F, 1F);
            player.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, 100F, 1F);
            this.bar.removePlayer(player);
        }
        reset(server);
    }

    private List<String> getAllAlivePlayers(MinecraftServer server) {
        List<String> alive = new ArrayList<>();
        for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(i);
            if (!HideNSeekCommand.seekers.contains(player) && !player.isSpectator()) {
                alive.add(player.getName().getString());
            }
        }
        return alive;
    }

    private boolean checkForAliveHiders(MinecraftServer server) {
        for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(i);
            if (!player.isSpectator() && !HideNSeekCommand.seekers.contains(player)) {
                return true;
            }
        }
        return false;
    }

    private void tickBossBarTimer() {
        bar.setName(Text.literal(formattedTimeLeft()));
        bar.setPercent((float) getCurrentGameTime()/getDuration());
    }

    private String formattedTimeLeft() {
        int sec = Math.round((float) this.getTimeLeft()/20);
        if (sec > 59) {
            int minutes = sec / 60;
            int seconds = sec % 60;
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return String.valueOf(sec);
        }
    }

    private List<String> getNameFromPlayers(List<ServerPlayerEntity> players) {
        List<String> names = new ArrayList<>();
        for (ServerPlayerEntity player : players) {
            names.add(player.getName().getString());
        }
        return names;
    }


    public static int getCurrentGameTime() {
        return current_time;
    }


    public static void setCurrentGameTime(int time) {
        current_time = time;
    }


    public static int getDuration() {
        return duration;
    }


    public static void setDuration(int duration1) {
        duration = duration1;
    }


    public int getTimeLeft() {
        return duration - current_time;
    }


    public void setTimeLeft(int duration1) {
        current_time = duration - duration1;
    }


    public void setCountdown(int countdown) {
        this.countdown = countdown;
        this.countdown_usable = countdown;
    }


    public void reset(MinecraftServer server) {
        this.countdown_usable = this.countdown;
        HideNSeekCommand.seekers.clear();
        for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(i);
            player.teleport(lobby.getX(), lobby.getY(), lobby.getZ());
            player.changeGameMode(GameMode.ADVENTURE);
        }
    }


    public static void setMapTeleport(BlockPos pos) {
        map = pos;
    }


    public static void setLobbyTeleport(BlockPos pos) {
        lobby = pos;
    }


    public static void releaseSeekers(MinecraftServer server) {
        for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(i);
            player.sendMessage(Text.literal("The Seekers have been released!"), true);
            player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 100F, 1F);
            if (HideNSeekCommand.seekers.contains(player)) {
                player.teleport(map.getX(), map.getY(), map.getZ());
            }
        }
    }


    public static BlockPos getMapPos() {
        return map;
    }


    public static BlockPos getLobbyPos() {
        return lobby;
    }
}

