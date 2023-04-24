package com.github.tatercertified.hide_n_seek.events;

import com.github.tatercertified.hide_n_seek.command.HideNSeekCommand;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GiveItemEvent extends Event{
    protected int time;
    protected String[] actions;
    protected String receiver;
    protected MinecraftServer server;

    public GiveItemEvent(int time, String[] actions, String receiver) {
        this.time = time;
        this.actions = actions;
        this.receiver = receiver;
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
        // Use the format <item><amount>; Ex: torch17
        List<String> strings = Arrays.stream(actions).toList();
        List<ItemStack> items = new ArrayList<>();
        for (String string : strings) {
            String[] splitStr = string.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            Item item = Registries.ITEM.get(new Identifier("minecraft", splitStr[0]));
            ItemStack itemStack = new ItemStack(item, Integer.parseInt(splitStr[1]));
            items.add(itemStack);
        }
        if (Objects.equals(receiver, "seeker")) {
            for (int i = 0; i < HideNSeekCommand.seekers.size(); i++) {
                ServerPlayerEntity player = HideNSeekCommand.seekers.get(i);
                for (ItemStack item : items) {
                    player.giveItemStack(item);
                }
            }
        } else {
            for (int i = 0; i < server.getPlayerManager().getPlayerList().size(); i++) {
                ServerPlayerEntity player =  server.getPlayerManager().getPlayerList().get(i);
                if (!HideNSeekCommand.seekers.contains(player)) {
                    for (ItemStack item : items) {
                        player.giveItemStack(item);
                    }
                }
            }
        }
    }
}
