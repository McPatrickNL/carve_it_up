// File Location from project root:
// fabric/src/main/java/nl/patrick/carve_it_up/FabricModClient.java
package nl.patrick.carve_it_up;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import nl.patrick.carve_it_up.carving.CarvingKeyBinds;
import nl.patrick.carve_it_up.network.ClientCarvingNetworkHandlers;
import nl.patrick.carve_it_up.network.SyncCarvedDataPayload;

public class FabricModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Safe to call here—ClientModInitializer never executes on dedicated servers
        KeyMappingHelper.registerKeyMapping(CarvingKeyBinds.CATEGORY_KEY);
        KeyMappingHelper.registerKeyMapping(CarvingKeyBinds.SUBMENU_KEY);

        // NewStart Client-only receiver for the carved-data sync payload
        ClientPlayNetworking.registerGlobalReceiver(SyncCarvedDataPayload.TYPE, (payload, context) ->
            context.client().execute(() -> ClientCarvingNetworkHandlers.handleSyncCarvedData(payload))
        );
        // NewEnd
    }
}