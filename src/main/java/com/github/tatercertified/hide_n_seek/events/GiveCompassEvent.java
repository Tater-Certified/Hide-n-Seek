package com.github.tatercertified.hide_n_seek.events;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.github.tatercertified.hide_n_seek.Hide_n_Seek.seekers;

public class GiveCompassEvent extends Event{
    protected int time;
    protected MinecraftServer server;

    public GiveCompassEvent(int time) {
        this.time = time;
    }

    @Override
    public void setServer(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public Integer getTime() {
        return this.time;
    }

    @Override
    public void event() {
        for (ServerPlayerEntity player : seekers) {
            NbtCompound nbt = new NbtCompound();
            nbt.putBoolean("tracker", true);
            ItemStack compass = new ItemStack(Items.COMPASS);
            compass.setNbt(nbt);
            player.giveItemStack(compass);
        }
    }
}
