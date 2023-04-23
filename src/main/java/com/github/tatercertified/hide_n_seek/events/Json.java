package com.github.tatercertified.hide_n_seek.events;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Json {
    static Gson gson = new Gson();
    public static void serializeData() {
        // Serialize a list of MyGsonClass instances to a JSON file
        List<Event> myObjects = new ArrayList<>();
        myObjects.add(new Event());
        myObjects.add(new Event());
        String json = gson.toJson(myObjects);
        try (FileWriter writer = new FileWriter("hide-n-seek-events.json")) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Event> deserializeData() {
        // Deserialize a list of MyGsonClass instances from a JSON file
        try (BufferedReader reader = new BufferedReader(new FileReader("hide-n-seek-events.json"))) {
            Type listType = new TypeToken<List<Event>>(){}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
