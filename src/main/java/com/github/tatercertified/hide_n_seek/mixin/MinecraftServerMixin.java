package com.github.tatercertified.hide_n_seek.mixin;

import com.github.tatercertified.hide_n_seek.Hide_n_Seek;
import com.github.tatercertified.hide_n_seek.command.HideNSeekCommand;
import com.github.tatercertified.hide_n_seek.events.Event;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerInterface{
    @Shadow public abstract PlayerManager getPlayerManager();

    @Shadow public abstract void sendMessage(Text message);

    private int duration = 0;
    private int current_time = -1;
    private int countdown_usable;
    private int countdown = 0;
    private BlockPos lobby;
    private BlockPos map;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        tickGame();
    }

    @Override
    public int getCurrentGameTime() {
        return current_time;
    }

    @Override
    public void setCurrentGameTime(int time) {
        this.current_time = time;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public int getTimeLeft() {
        return duration - current_time;
    }

    @Override
    public void setTimeLeft(int duration) {
        current_time = this.duration - duration;
    }

    @Override
    public void setCountdown(int countdown) {
        this.countdown = countdown;
        this.countdown_usable = countdown;
    }

    @Override
    public void reset() {
        this.countdown_usable = this.countdown;
        HideNSeekCommand.seekers.clear();
        for (int i = 0; i < this.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = this.getPlayerManager().getPlayerList().get(i);
            player.teleport(lobby.getX(), lobby.getY(), lobby.getZ());
        }
    }

    @Override
    public void setMapTeleport(BlockPos pos) {
        this.map = pos;
    }

    @Override
    public void setLobbyTeleport(BlockPos pos) {
        this.lobby = pos;
    }

    @Override
    public void releaseSeekers() {
        for (int i = 0; i < this.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = this.getPlayerManager().getPlayerList().get(i);
            player.sendMessage(Text.literal("The Seekers have been released!"), true);
            player.playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 100F, 1F);
            if (HideNSeekCommand.seekers.contains(player)) {
                player.teleport(map.getX(), map.getY(), map.getZ());
            }
        }
    }

    public void tickGame() {
        if (current_time > -1 && duration > 0) {
            if (countdown_usable <= 0) {
                if (current_time < duration) {
                    current_time++;
                    checkForEvents();
                } else {
                    //Game Over
                    if (!HideNSeekCommand.seekers.isEmpty()) {
                        gameOver(HideNSeekCommand.seekers);
                    } else {
                        gameOver(getAllAlivePlayers());
                    }
                    this.countdown_usable = this.countdown;
                }
            } else {
                countdown();
            }
        }
    }

    private void countdown() {
        int seconds = Math.round(countdown_usable/20);
        boolean time = seconds == 0;
        for (int i = 0; i < this.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = this.getPlayerManager().getPlayerList().get(i);
            if (!HideNSeekCommand.seekers.contains(player)) {
                if (time) {
                    player.sendMessage(Text.literal("Start!"), true);
                    player.teleport(map.getX(), map.getY(), map.getZ());
                } else {
                    player.sendMessage(Text.literal(String.valueOf(seconds)), true);
                }
            }
        }
        this.countdown_usable--;
    }

    private void checkForEvents() {
        for (int i = 0; i < Hide_n_Seek.registered_events.size(); i++) {
            Event event = Hide_n_Seek.registered_events.get(i);
            if (current_time == event.getTime()) {
                event.setServer(((MinecraftServer)(Object)this));
                event.event();
            }
        }
    }

    private void gameOver(List<ServerPlayerEntity> winners) {
        setCurrentGameTime(-1);
        this.sendMessage(Text.literal("The Game is Over!"));
        for (int i = 0; i < this.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = this.getPlayerManager().getPlayerList().get(i);
            player.sendMessage(Text.literal(winners + " have won the game!"), true);
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 100F, 1F);
            player.playSound(SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, 100F, 1F);
        }
        reset();
    }

    private List<ServerPlayerEntity> getAllAlivePlayers() {
        List<ServerPlayerEntity> alive = new ArrayList<>();
        for (int i = 0; i < this.getPlayerManager().getPlayerList().size(); i++) {
            ServerPlayerEntity player = this.getPlayerManager().getPlayerList().get(i);
            if (!HideNSeekCommand.seekers.contains(player) && !player.isSpectator()) {
                alive.add(player);
            }
        }
        return alive;
    }
}
