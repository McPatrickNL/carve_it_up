// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/NeoForgeMod.java
package nl.patrick.carve_it_up;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import nl.patrick.carve_it_up.block.ModBlocks;
import nl.patrick.carve_it_up.item.ModItems;
import nl.patrick.carve_it_up.network.CarvingNetworkHandlers;
import nl.patrick.carve_it_up.network.RequestCarveActionPayload;
import nl.patrick.carve_it_up.registry.NeoForgeBlockRegistry;
import nl.patrick.carve_it_up.registry.NeoForgeComponentRegistry;
import nl.patrick.carve_it_up.registry.NeoForgeCreativeModeTabRegistry;
import nl.patrick.carve_it_up.registry.NeoForgeItemRegistry;
import nl.patrick.carve_it_up.tab.ModCreativeModeTabs;

/**
 * Main mod entrypoint for NeoForge.
 */
@Mod(CommonMod.MOD_ID)
public class NeoForgeMod { // Converted from Allman style brace

    public NeoForgeMod(IEventBus modEventBus, ModContainer modContainer) { // Converted from Allman style brace
        // 1. Tell NeoForge to watch our registries
        NeoForgeBlockRegistry.BLOCKS.register(modEventBus);
        NeoForgeItemRegistry.ITEMS.register(modEventBus);
        NeoForgeCreativeModeTabRegistry.TABS.register(modEventBus);
        NeoForgeComponentRegistry.COMPONENTS.register(modEventBus);

        // 2. Initialize the common code setup (which fills tabs, items, etc.)
        CommonMod.init();

        modEventBus.addListener(this::addCreativeTabs);

        // Register both payload types + the server-side handler
        modEventBus.addListener(this::registerPayloads);
    }

    private void addCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        // Check if the current tab being built matches your custom multiplatform tab
        if (event.getTab() == ModCreativeModeTabs.CARVE_IT_UP_TAB.get()) {
            event.accept(ModItems.EXAMPLE_ITEM.get());
            event.accept(ModBlocks.EXAMPLE_BLOCK_ITEM.get());
            event.accept(ModItems.CARVING_TOOL.get());
            event.accept(ModItems.IRON_CARVING_TOOL.get());
        }
    }

    // Registers the payload types shared by both physical sides
    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
            RequestCarveActionPayload.TYPE,
            RequestCarveActionPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() ->
                CarvingNetworkHandlers.handleCarveActionRequest(payload, (ServerPlayer) context.player())
            )
        );
    }
}