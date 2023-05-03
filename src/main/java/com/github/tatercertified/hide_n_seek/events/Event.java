package com.github.tatercertified.hide_n_seek.events;

import com.github.tatercertified.hide_n_seek.Hide_n_Seek;
import net.minecraft.server.MinecraftServer;

public class Event {
    private Integer time;
    private String actionType;
    private String[] data;

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getActionType() {
        return actionType;
    }

    public void setEventType(String actionType) {
        this.actionType = actionType;
    }

    public String[] getData() {
        return data;
    }

    public void setData(String[] data) {
        this.data = data;
    }

    public void classify() {
        switch (getActionType()) {
            case "announcement" -> Hide_n_Seek.registered_events.add(new AnnounceEvent(getTime(), getData()));
            case "item-hider" -> Hide_n_Seek.registered_events.add(new GiveItemEvent(getTime(), getData(), "hider"));
            case "item-seeker" -> Hide_n_Seek.registered_events.add(new GiveItemEvent(getTime(), getData(), "seeker"));
            case "release" -> Hide_n_Seek.registered_events.add(new ReleaseSeekersEvent(getTime()));
            case "compass" -> Hide_n_Seek.registered_events.add(new GiveCompassEvent(getTime()));
        }
    }

    public void event() {
        // Override for Event
    }

    public void setServer(MinecraftServer server) {
        // Override for Event
    }
}
