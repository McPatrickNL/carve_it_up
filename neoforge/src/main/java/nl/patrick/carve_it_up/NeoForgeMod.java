package nl.patrick.carve_it_up;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import nl.patrick.carve_it_up.block.ModBlocks;
import nl.patrick.carve_it_up.carving.CarvingKeyBinds;
import nl.patrick.carve_it_up.item.ModItems;
import nl.patrick.carve_it_up.registry.NeoForgeBlockRegistry;
import nl.patrick.carve_it_up.registry.NeoForgeComponentRegistry;
import nl.patrick.carve_it_up.registry.NeoForgeCreativeModeTabRegistry;
import nl.patrick.carve_it_up.registry.NeoForgeItemRegistry;
import nl.patrick.carve_it_up.tab.ModCreativeModeTabs;


// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/CarveItUpNeoForge.java
@Mod(CommonMod.MOD_ID)
public class NeoForgeMod
{
//    @SubscribeEvent
//    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
//        event.register(CarvingKeyBinds.CATEGORY_KEY);
//        event.register(CarvingKeyBinds.SUBMENU_KEY);
//    }
    
    // todo add something like this to prevent permanent carving locks:
    //  Also for Fabric...
//    @SubscribeEvent
//    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
//        UUID playerUuid = event.getEntity().getUUID();
//
//        // Sweeps the lock map and removes any active locks held by this player
//        ACTIVE_LOCKS.values().removeIf(uuid -> uuid.equals(playerUuid));
//    }
    
    
    public NeoForgeMod(IEventBus modEventBus, ModContainer modContainer)
    {
        // 1. Tell NeoForge to watch our registries
        NeoForgeBlockRegistry.BLOCKS.register(modEventBus);
        NeoForgeItemRegistry.ITEMS.register(modEventBus);
        NeoForgeCreativeModeTabRegistry.TABS.register(modEventBus);
        NeoForgeComponentRegistry.COMPONENTS.register(modEventBus);
        
        // 2. Initialize the common code setup (which fills tabs, items, etc.)
        // This has to go after telling NeoForge about all registries in #1!
        CommonMod.init();
        
        modEventBus.addListener(this::addCreativeTabs);
        
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
    
    // todo keep here or keep in CarveItUpNeoForgeClient
//    private static void onClientSetup(FMLClientSetupEvent event) {
//        CarveItUpCommon.LOGGER.info("HELLO FROM NEOFORGE CLIENT SETUP");
//        CarveItUpCommon.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
//    }
}