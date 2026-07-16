package nl.patrick.carve_it_up;

// File Location from project root:
// fabric/src/main/java/nl/patrick/carve_it_up/FabricModClient.java

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import nl.patrick.carve_it_up.carving.CarvingKeyBinds;


public class FabricModClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient() {
        // Safe to call here—ClientModInitializer never executes on dedicated servers
        KeyMappingHelper.registerKeyMapping(CarvingKeyBinds.CATEGORY_KEY);
        KeyMappingHelper.registerKeyMapping(CarvingKeyBinds.SUBMENU_KEY);
    }
}
