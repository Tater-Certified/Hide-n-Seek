package com.github.tatercertified.hide_n_seek.events;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.List;

public class AnnounceEvent extends Event{

    protected int time;
    protected String[] actions;
    protected MinecraftServer server;

    public AnnounceEvent(int time, String[] actions) {
        this.time = time;
        this.actions = actions;
    }

    @Override
    public Integer getTime() {
        return time;
    }

    @Override
    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void event() {
        ping(server.getPlayerManager().getPlayerList(), actions[0]);
    }

    private void ping(List<ServerPlayerEntity> players, String message) {
        for (ServerPlayerEntity player : players) {
            player.playSound(SoundEvents.BLOCK_BELL_USE, SoundCategory.PLAYERS,1F, 1F);
            player.sendMessage(Text.literal(message), true);
        }
    }
}
