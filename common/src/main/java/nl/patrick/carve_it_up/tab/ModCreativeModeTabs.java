package nl.patrick.carve_it_up.tab;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import nl.patrick.carve_it_up.item.ModItems;
import nl.patrick.carve_it_up.registry.RegistryObject;
import nl.patrick.carve_it_up.services.Services;

import static nl.patrick.carve_it_up.CarveItUpCommon.LOGGER;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/tab/ModCreativeModeTabs.java
public class ModCreativeModeTabs
{
    public static final RegistryObject<CreativeModeTab> CARVE_IT_UP_TAB =
            Services.REGISTRY.creativeModeTabs().register(
                    "carve_it_up_tab",
                    () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                                         .title(Component.translatable("itemGroup.carve_it_up"))
                                         .icon(ModItems.EXAMPLE_ITEM.get()::getDefaultInstance)
                                         // We leave displayItems out of the shared code to avoid compiler bugs
                                         .build());
    
    public static void init(){
        LOGGER.info("Registering Creative Mode Tabs for Carve It Up.");
    }
}