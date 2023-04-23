package com.github.tatercertified.hide_n_seek;

import com.github.tatercertified.hide_n_seek.command.HideNSeekCommand;
import com.github.tatercertified.hide_n_seek.events.Event;
import com.github.tatercertified.hide_n_seek.events.Json;
import net.fabricmc.api.ModInitializer;

import java.util.ArrayList;
import java.util.List;

public class Hide_n_Seek implements ModInitializer {
    public static List<Event> registered_events = new ArrayList<>();


    @Override
    public void onInitialize() {
        HideNSeekCommand.register();
        List<Event> events = Json.deserializeData();
        for (Event event : events) {
            event.classify();
        }
        events.clear();
    }
}
