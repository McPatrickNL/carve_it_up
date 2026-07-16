package nl.patrick.carve_it_up;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import nl.patrick.carve_it_up.carving.CarvingKeyBinds;


// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/NeoForgeModClient.java

@EventBusSubscriber(modid = CommonMod.MOD_ID, value = Dist.CLIENT)
public class NeoForgeModClient
{
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(CarvingKeyBinds.CATEGORY_KEY);
        event.register(CarvingKeyBinds.SUBMENU_KEY);
    }
}
