// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/NeoForgeModClient.java
package nl.patrick.carve_it_up;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import nl.patrick.carve_it_up.carving.CarvingKeyBinds;
import nl.patrick.carve_it_up.network.ClientCarvingNetworkHandlers;
import nl.patrick.carve_it_up.network.SyncCarvedDataPayload;

/**
 * Client-specific event handlers and initialization for NeoForge.
 */
@EventBusSubscriber(modid = CommonMod.MOD_ID, value = Dist.CLIENT)
public class NeoForgeModClient { // Converted from Allman style brace

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CarvingKeyBinds.CATEGORY_KEY);
        event.register(CarvingKeyBinds.SUBMENU_KEY);
    }

    // Client-only registration of the S2C sync payload receive handler
    @SubscribeEvent
    public static void registerClientPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
            SyncCarvedDataPayload.TYPE,
            SyncCarvedDataPayload.STREAM_CODEC,
            (payload, context) -> context.enqueueWork(() -> ClientCarvingNetworkHandlers.handleSyncCarvedData(payload))
        );
    }
}