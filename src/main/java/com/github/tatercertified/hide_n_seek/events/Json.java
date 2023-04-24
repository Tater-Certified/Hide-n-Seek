package com.github.tatercertified.hide_n_seek.events;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
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
    static Gson gson = new Gson();
    static String path = FabricLoader.getInstance().getConfigDir() + "/hide-n-seek-events.json";
    public static void serializeData() {
        // Serialize a list of MyGsonClass instances to a JSON file
        if (!Files.exists(Path.of(path))) {
            List<Event> examples = new ArrayList<>();
            //Announcement
            Event example_announce = new Event();
            example_announce.setEventType("announce");
            example_announce.setData(new String[]{"This is an example announcement at 2000 ticks!"});
            example_announce.setTime(2000);
            examples.add(example_announce);

            //GiveItemHider
            Event example_item = new Event();
            example_item.setEventType("item-hider");
            example_item.setData(new String[]{"diamond_sword1", "golden_carrot64"});
            example_item.setTime(2000);
            examples.add(example_item);

            //GiveItemSeeker
            Event example_item1 = new Event();
            example_item1.setEventType("item-seeker");
            example_item1.setData(new String[]{"compass1"});
            example_item1.setTime(2000);
            examples.add(example_item1);

            //ReleaseSeeker
            Event example_release = new Event();
            example_release.setEventType("release");
            example_release.setTime(400);
            examples.add(example_release);

            String json = gson.toJson(examples);
            try (FileWriter writer = new FileWriter(path)) {
                writer.write(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Event> deserializeData() {
        // Deserialize a list of MyGsonClass instances from a JSON file
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            Type listType = new TypeToken<List<Event>>(){}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
