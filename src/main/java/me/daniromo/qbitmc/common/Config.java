package me.daniromo.qbitmc.common;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class Config {

    public static final String DATABASE_URL;
    public static final String DATABASE_USER;
    public static final String DATABASE_PASSWORD;

    static {
        final Properties props = new Properties();
        final Path path = FabricLoader.getInstance().getConfigDir().resolve("qbitmc.properties");
        if (Files.isRegularFile(path)) {
            try (InputStream stream = Files.newInputStream(path, StandardOpenOption.CREATE)) {
                props.load(stream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        DATABASE_URL = props.getProperty("db.url");
        DATABASE_USER = props.getProperty("db.user");
        DATABASE_PASSWORD = props.getProperty("db.password");
    }

    public static void init() {}

}
