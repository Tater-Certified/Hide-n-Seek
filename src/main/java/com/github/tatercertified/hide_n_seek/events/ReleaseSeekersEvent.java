package com.github.tatercertified.hide_n_seek.events;

import com.github.tatercertified.hide_n_seek.mixin.MinecraftServerInterface;
import net.minecraft.server.MinecraftServer;

public class ReleaseSeekersEvent extends Event{
    protected int time;
    protected MinecraftServer server;

    public ReleaseSeekersEvent(int time) {
        this.time = time;
    }

    @Override
    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void event() {
        ((MinecraftServerInterface)server).releaseSeekers();
    }
}
