package com.github.tatercertified.hide_n_seek;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Properties;

public class Config {
    public static Properties properties = new Properties();
    private static final Path config = FabricLoader.getInstance().getConfigDir().resolve("hide-n-seek.properties");

    final static String CONFIG_VERSION_KEY = "config-version";
    final static String DURATION_KEY = "duration";
    final static String LOBBY_KEY = "lobby-pos";
    final static String MAP_KEY = "map-pos";

    public static String cfgver = "1.0";
    public static int duration;
    public static BlockPos lobby;
    public static BlockPos map;

    public static void config() {
        //Create Config
        if (Files.notExists(config)) {
            try {
                storecfg();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                loadcfg();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!(Objects.equals(properties.getProperty(CONFIG_VERSION_KEY), cfgver))) {
                properties.setProperty(CONFIG_VERSION_KEY, cfgver);
                try {
                    storecfg();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                parse();
            }
        }
    }


    /**
     * Save the config
     */
    public static void storecfg() throws IOException {
        try (OutputStream output = Files.newOutputStream(config, StandardOpenOption.CREATE)) {
            fillDefaults();
            properties.store(output, null);
        }
        parse();
    }

    /**
     * If the config value doesn't exist, set it to default
     */
    private static void fillDefaults() {
        if (!properties.containsKey(CONFIG_VERSION_KEY)) {
            properties.setProperty(CONFIG_VERSION_KEY, cfgver);
        }
        if (!properties.containsKey(DURATION_KEY)) {
            properties.setProperty(DURATION_KEY, "6000");
        }
        if (!properties.containsKey(LOBBY_KEY)) {
            properties.setProperty(LOBBY_KEY, "0,0,0");
        }
        if (!properties.containsKey(MAP_KEY)) {
            properties.setProperty(MAP_KEY, "0,0,0");
        }
    }

    /**
     * Loads the config
     */
    public static void loadcfg() throws IOException {
        try (InputStream input = Files.newInputStream(config)) {
            properties.load(input);
        }
    }

    /**
     * Parses the config to convert into Objects
     */
    public static void parse() {
        fillDefaults();
        duration = Integer.parseInt(properties.getProperty(DURATION_KEY));

        String lobby_string = properties.getProperty(LOBBY_KEY);
        int[] numbers = data2Coords(lobby_string);
        lobby = new BlockPos(numbers[0], numbers[1], numbers[2]);

        String map_string = properties.getProperty(MAP_KEY);
        int[] numbers1 = data2Coords(map_string);
        map = new BlockPos(numbers1[0], numbers1[1], numbers1[2]);
    }

    public static void saveConfig(MinecraftServer server) {
        properties.setProperty(DURATION_KEY, String.valueOf(Hide_n_Seek.getDuration()));
        properties.setProperty(LOBBY_KEY, blockPos2data(Hide_n_Seek.getLobbyPos()));
        properties.setProperty(MAP_KEY, blockPos2data((Hide_n_Seek.getMapPos())));
        try {
            storecfg();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int[] data2Coords(String string) {
        String[] positions = string.split(",");
        int[] numbers = new int[3];
        for (int i = 0; i < 3; i++) {
            numbers[i] = Integer.parseInt(positions[i].trim());
        }
        return numbers;
    }

    private static String blockPos2data(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }
}
