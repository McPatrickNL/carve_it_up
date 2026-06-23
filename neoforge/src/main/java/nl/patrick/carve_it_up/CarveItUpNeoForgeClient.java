package nl.patrick.carve_it_up;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/CarveItUpNeoForgeClient.java
public class CarveItUpNeoForgeClient {
    
    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        // Register client-specific lifecycle events
        modEventBus.addListener(CarveItUpNeoForgeClient::onClientSetup);
        
        // Allows NeoForge to create a config screen for this mod's configs
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
    
    private static void onClientSetup(FMLClientSetupEvent event) {
        CarveItUpCommon.LOGGER.info("HELLO FROM NEOFORGE CLIENT SETUP");
        CarveItUpCommon.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}