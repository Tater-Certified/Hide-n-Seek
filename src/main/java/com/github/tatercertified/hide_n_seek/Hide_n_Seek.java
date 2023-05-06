package com.github.tatercertified.hide_n_seek;

import com.github.tatercertified.hide_n_seek.command.HideNSeekCommand;
import com.github.tatercertified.hide_n_seek.events.Event;
import com.github.tatercertified.hide_n_seek.events.Json;
import com.github.tatercertified.hide_n_seek.events.ReleaseSeekersEvent;
import com.github.tatercertified.hide_n_seek.interfaces.ServerPlayerEntityInterface;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;

public class Hide_n_Seek implements ModInitializer {
    public static List<Event> registered_events = new ArrayList<>();
    private static int duration;
    private static int current_time = -1;
    private static int countdown = 200;
    private static int countdown_usable = 200;
    private static int seeker_time_left;
    private int seeker_time_released;
    private static BlockPos lobby;
    private static BlockPos map;
    private static final ServerBossBar bar = new ServerBossBar(null, BossBar.Color.BLUE, BossBar.Style.PROGRESS);
    private static final ServerBossBar seeker_bar = new ServerBossBar(null, BossBar.Color.RED, BossBar.Style.PROGRESS);
    private static Team seeker_team;

    public static final List<ServerPlayerEntity> hiders = new ArrayList<>();
    public static final List<ServerPlayerEntity> seekers = new ArrayList<>();


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

        ServerLifecycleEvents.SERVER_STOPPING.register(server1 -> Config.saveConfig());

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            setDuration(Config.duration);
            setLobbyTeleport(Config.lobby);
            setMapTeleport(Config.map);

            seeker_team = server.getScoreboard().addTeam("seekers");
            if (seeker_team == null) {
                seeker_team = server.getScoreboard().addTeam("seekers");
            }
            seeker_team.setColor(Formatting.RED);
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
                    if (seekers.isEmpty()) {
                        gameOver(getAllAlivePlayers(), "Hiders" , server);
                        return;
                    }
                    if (!checkForAliveHiders()) {
                        gameOver(getNameFromSeekers(), "Seekers", server);
                    }
                } else {
                    //Game Over
                    gameOver(getAllAlivePlayers(), "Hiders", server);
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
            if (time) {
                if (!((ServerPlayerEntityInterface)player).isSeeker()) {
                    player.sendMessage(Text.literal("Start!"), true);
                    player.teleport(map.getX(), map.getY(), map.getZ());
                    bar.setName(Text.literal(formattedTime(getTimeLeft())));
                    bar.addPlayer(player);
                    player.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.PLAYERS, 1F, 1F);
                    player.setInvulnerable(false);
                } else {
                    seeker_bar.setName(Text.literal(formattedTime(seeker_time_left)));
                    seeker_bar.addPlayer(player);
                    server.getScoreboard().addPlayerToTeam(player.getName().getString(), seeker_team);
                }

                player.changeGameMode(GameMode.ADVENTURE);
            } else {
                player.sendMessage(Text.literal(String.valueOf(seconds)), true);
            }
        }
        countdown_usable--;
    }

    private void checkForEvents(MinecraftServer server) {
        for (int i = 0; i < Hide_n_Seek.registered_events.size(); i++) {
            Event event = Hide_n_Seek.registered_events.get(i);
            if (current_time == event.getTime()) {
                event.setServer(server);
                event.event();
            }
            if (seeker_time_left >= 0 && event instanceof ReleaseSeekersEvent) {
                setSeekerReleaseTimeLeft(event.getTime() - getCurrentGameTime());
                seeker_time_released = event.getTime();
            }
        }
    }

    private void gameOver(List<String> winners, String group, MinecraftServer server) {
        setCurrentGameTime(-1);
        server.sendMessage(Text.literal("The Game is Over!"));
        for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(i);
            String winners_msg = String.join(", ", winners) + " have won the game!";
            player.sendMessage(Text.literal(winners_msg));
            TitleS2CPacket title = new TitleS2CPacket(Text.empty());
            SubtitleS2CPacket subtitle = new SubtitleS2CPacket(Text.literal("The " + group + " Won!"));
            TitleFadeS2CPacket fade = new TitleFadeS2CPacket(20, 160, 20);
            server.getPlayerManager().sendToAll(fade);
            server.getPlayerManager().sendToAll(subtitle);
            server.getPlayerManager().sendToAll(title);
        }
        reset(server);
    }

    private List<String> getAllAlivePlayers() {
        List<String> alive = new ArrayList<>();
        for (ServerPlayerEntity player : hiders) {
            alive.add(player.getName().getString());
        }
        return alive;
    }

    private boolean checkForAliveHiders() {
        for (ServerPlayerEntity player : hiders) {
            if (!player.isSpectator()) {
                return true;
            }
        }
        return false;
    }

    public static void fillHiderList(MinecraftServer server) {
        for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(i);
            if (!((ServerPlayerEntityInterface)player).isSeeker()) {
                hiders.add(player);
            }
        }
    }

    private void tickBossBarTimer() {
        bar.setName(Text.literal(formattedTime(getTimeLeft())));
        bar.setPercent((float) getCurrentGameTime()/getDuration());

        seeker_bar.setName(Text.literal(formattedTime(seeker_time_left)));
        seeker_bar.setPercent((float) getCurrentGameTime()/seeker_time_released);
    }

    private String formattedTime(int input_ticks) {
        int sec = Math.round((float) input_ticks/20);
        if (sec > 59) {
            int minutes = sec / 60;
            int seconds = sec % 60;
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return String.valueOf(sec);
        }
    }

    private void setSeekerReleaseTimeLeft(int time) {
        seeker_time_left = time;
    }

    private List<String> getNameFromSeekers() {
        List<String> names = new ArrayList<>();
        for (ServerPlayerEntity player : Hide_n_Seek.seekers) {
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


    public static void setCountdown(int countdown) {
        Hide_n_Seek.countdown = countdown;
        countdown_usable = countdown;
    }

    public static int getCountdown() {
        return countdown;
    }


    public static void reset(MinecraftServer server) {
        countdown_usable = countdown;
        setCurrentGameTime(-1);
        seekers.clear();
        hiders.clear();
        for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayerList().get(i);
            player.setHealth(20.0F);
            player.changeGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
            player.teleport(lobby.getX(), lobby.getY(), lobby.getZ());
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1F, 1F);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setExhaustion(0.0F);
            player.setInvulnerable(true);
            bar.removePlayer(player);
            seeker_bar.removePlayer(player);
            ((ServerPlayerEntityInterface)player).setSeeker(false);
            if (seeker_team.getPlayerList().contains(player.getName().getString())) {
                server.getScoreboard().removePlayerFromTeam(player.getName().getString(), seeker_team);
            }
        }
        seeker_time_left = 0;
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
            player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 1F, 1F);
            if (((ServerPlayerEntityInterface)player).isSeeker()) {
                seeker_bar.removePlayer(player);
                bar.addPlayer(player);
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

