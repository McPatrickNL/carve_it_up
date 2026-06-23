package nl.patrick.carve_it_up;

import net.fabricmc.api.ModInitializer;
import nl.patrick.carve_it_up.block.ModBlocks;
import nl.patrick.carve_it_up.item.ModItems;
import nl.patrick.carve_it_up.tab.ModCreativeModeTabs;

// File Location from project root:
// fabric/src/main/java/nl/patrick/carve_it_up/CarveItUpFabric.java

public class CarveItUpFabric implements ModInitializer
{
    @Override
    public void onInitialize() {
        // 1. Trigger the common registration block (Registers blocks/items/tabs)
        CarveItUpCommon.init();
        
        // 2. Populate your multiplatform creative tab on Fabric
        ItemGroupEvents.modifyEntriesEvent(ModCreativeModeTabs.CARVE_IT_UP_TAB.getKey()).register(content -> {
            content.accept(ModItems.EXAMPLE_ITEM.get());
            content.accept(ModBlocks.EXAMPLE_BLOCK_ITEM.get());
        });
    }
}
