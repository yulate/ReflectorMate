package com.yulate.reflectormate.core.config;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ConfigLoader {

    public static String loadJsonConfig() {
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream("plugin_config.json");
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
            return scanner.useDelimiter("\\A").next();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
