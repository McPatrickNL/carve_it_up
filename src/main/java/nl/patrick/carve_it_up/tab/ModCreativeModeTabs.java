package nl.patrick.carve_it_up.tab;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import nl.patrick.carve_it_up.item.ModItems;

import static nl.patrick.carve_it_up.registry.ModRegistries.*;


public class ModCreativeModeTabs
{
    
    // Creates a creative tab with the id "carve_it_up:ciu_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CIU_TAB = CREATIVE_MODE_TABS.register(
            "ciu_tab", () -> CreativeModeTab.builder()
                                                //The language key for the title of your CreativeModeTab
                                                .title(Component.translatable("itemGroup.ciu"))
                                                .withTabsBefore(net.minecraft.world.item.CreativeModeTabs.COMBAT)
                                                .icon(ModItems.EXAMPLE_ITEM.get()::getDefaultInstance)
                                                // And for a vanilla item icon:
                                                //.icon(Items.STONE::getDefaultInstance)
                                                .displayItems((parameters, output) -> {
                                                    // Add the example item to the tab. For your own tabs, this method is preferred over the event
                                                    output.accept(ModItems.EXAMPLE_ITEM.get());
                                                    // And for a vanilla item
                                                    //output.accept(Items.STONE);
                                                }).build());
    
    public static void init(){}
}
