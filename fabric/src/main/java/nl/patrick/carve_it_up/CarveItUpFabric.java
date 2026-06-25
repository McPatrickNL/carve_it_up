package nl.patrick.carve_it_up;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import nl.patrick.carve_it_up.block.ModBlocks;
import nl.patrick.carve_it_up.item.ModItems;

// File Location from project root:
// fabric/src/main/java/nl/patrick/carve_it_up/CarveItUpFabric.java

public class CarveItUpFabric implements ModInitializer
{
    // todo in fabric.mod.json:
    //  I removed "icon": "assets/carve_it_up/icon.png", after the license line.
    //  I removed (before "depends"):
    //  "mixins": [
    //    "carve_it_up.mixins.json"
    //  ],
    
    @Override
    public void onInitialize() {
        // 1. Trigger the common registration block (Registers blocks/items/tabs)
        CarveItUpCommon.init();
        
        ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(CarveItUpCommon.MOD_ID, "carve_it_up_tab")
        );
        
        // 2. Populate your multiplatform creative tab on Fabric
        CreativeModeTabEvents.modifyOutputEvent(tabKey).register(content -> {
            content.accept(ModItems.EXAMPLE_ITEM.get());
            content.accept(ModBlocks.EXAMPLE_BLOCK_ITEM.get());
        });
    }
}
