package com.github.tatercertified.hide_n_seek.events;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.github.tatercertified.hide_n_seek.Hide_n_Seek.seekers;

public class HiderPvpEvent extends Event{
    protected int time;
    protected MinecraftServer server;

    public HiderPvpEvent(int time) {
        this.time = time;
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
        for (ServerPlayerEntity player : seekers) {
            player.setInvulnerable(false);
        }
    }
}
