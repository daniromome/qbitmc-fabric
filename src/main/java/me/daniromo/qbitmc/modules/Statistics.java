package me.daniromo.qbitmc.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.daniromo.qbitmc.Qbitmc;
import me.daniromo.qbitmc.common.Config;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Statistics {
    private final ObjectMapper objectMapper = new ObjectMapper();
    public Statistics(MinecraftServer server) {
        initializeTables();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        File statsDir = server.getSavePath(WorldSavePath.STATS).toFile();
        scheduler.scheduleAtFixedRate(() -> syncStatistics(statsDir), 0, 5, TimeUnit.MINUTES);
    }

    private void initializeTables() {
        try (Connection conn = DriverManager.getConnection(Config.DATABASE_URL, Config.DATABASE_USER, Config.DATABASE_PASSWORD);
             Statement query = conn.createStatement()) {
            Path migrations = Paths.get(Objects.requireNonNull(Qbitmc.class.getResource("/database/statistics.sql")).toURI());
            query.executeUpdate(Files.readString(migrations));
        } catch (SQLException | URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    private void syncStatistics(File statsDir) {
        File[] files = statsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;
        List<StatRecord> records = Arrays.stream(files).flatMap(file -> {
            try {
                StatFile stats = objectMapper.readValue(file, StatFile.class);
                UUID id = UUID.fromString(file.getName().substring(0, file.getName().lastIndexOf('.')));
                return stats.stats.entrySet().stream()
                        .flatMap(entry -> entry.getValue().entrySet().stream()
                                .map(statEntry -> new StatRecord(id, entry.getKey(), statEntry.getKey(), statEntry.getValue())));
            } catch (IOException e) {
                e.printStackTrace();
                return Stream.empty();
            }
        }).collect(Collectors.toList());
        upsertStatistics(records);
    }

    private void upsertStatistics(List<StatRecord> records) {
        try (Connection conn = DriverManager.getConnection(Config.DATABASE_URL, Config.DATABASE_USER, Config.DATABASE_PASSWORD);
             PreparedStatement query = conn.prepareCall("CALL upsert_statistics(?, ?, ?, ?)")) {
            for (StatRecord record : records) {
                query.setObject(1, record.id());
                query.setString(2, record.type());
                query.setString(3, record.name());
                query.setLong(4, record.value());
                query.addBatch();
            }
            query.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private record StatFile(
            Map<String, Map<String, Long>> stats,
            int DataVersion
    ) {}
    private record StatRecord(
            UUID id,
            String type,
            String name,
            Long value
    ) {}
}
