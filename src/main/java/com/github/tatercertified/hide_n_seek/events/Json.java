package com.github.tatercertified.hide_n_seek.events;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Json {
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static String path = FabricLoader.getInstance().getConfigDir() + "/hide-n-seek-events.json";
    public static void serializeData() {
        if (!Files.exists(Path.of(path))) {
            List<Event> examples = new ArrayList<>();
            //Announcement
            final Event example_announce = new Event();
            example_announce.setEventType("announcement");
            example_announce.setData(new String[]{"This is an example announcement at 2000 ticks!"});
            example_announce.setTime(2000);
            examples.add(example_announce);

            //GiveItemHider
            final Event example_item = new Event();
            example_item.setEventType("item-hider");
            example_item.setData(new String[]{"diamond_sword1", "golden_carrot64"});
            example_item.setTime(2000);
            examples.add(example_item);

            //GiveItemSeeker
            final Event example_item1 = new Event();
            example_item1.setEventType("item-seeker");
            example_item1.setData(new String[]{"diamond_sword1", "golden_carrot64"});
            example_item1.setTime(2000);
            examples.add(example_item1);

            //ReleaseSeeker
            final Event example_release = new Event();
            example_release.setEventType("release");
            example_release.setTime(400);
            examples.add(example_release);

            //GiveCompass
            final Event example_compass = new Event();
            example_compass.setEventType("compass");
            example_compass.setTime(2000);
            examples.add(example_compass);

            //HiderPvP
            final Event example_hider_pvp = new Event();
            example_hider_pvp.setEventType("hider-pvp");
            example_hider_pvp.setTime(2000);
            examples.add(example_hider_pvp);

            String json = gson.toJson(examples);
            try (FileWriter writer = new FileWriter(path)) {
                writer.write(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Event> deserializeData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            Type listType = new TypeToken<List<Event>>(){}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
