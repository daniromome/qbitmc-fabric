package me.daniromo.qbitmc;

import me.daniromo.qbitmc.common.Config;
import me.daniromo.qbitmc.modules.Statistics;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class Qbitmc implements ModInitializer {

    private Statistics statistics;
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        Config.init();
        ServerLifecycleEvents.SERVER_STARTED.register(this::initServerModules);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            statistics.registerPlayer(player.getUuid(), player.getName().getString());
        });
    }

    private void initServerModules(MinecraftServer server) {
        statistics = new Statistics(server);
    }
}
