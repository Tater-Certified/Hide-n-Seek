package com.github.tatercertified.hide_n_seek.events;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public void event() {
        server.sendMessage(Text.literal(actions[0]));
        ping(server.getPlayerManager().getPlayerList());
    }

    private void ping(List<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 100F, 1F);
        }
    }
}
