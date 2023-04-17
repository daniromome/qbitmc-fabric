package me.daniromo.qbitmc;

import me.daniromo.qbitmc.common.Config;
import me.daniromo.qbitmc.modules.Statistics;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class Qbitmc implements ModInitializer {
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        Config.init();
        ServerLifecycleEvents.SERVER_STARTED.register(this::initModules);
    }

    private void initModules(MinecraftServer server) {
        new Statistics(server);
    }
}
