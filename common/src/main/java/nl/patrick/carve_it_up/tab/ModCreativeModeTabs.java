package nl.patrick.carve_it_up.tab;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import net.minecraft.world.level.ItemLike;
import nl.patrick.carve_it_up.block.ModBlocks;
import nl.patrick.carve_it_up.item.ModItems;
import nl.patrick.carve_it_up.registry.RegistryObject;
import nl.patrick.carve_it_up.services.Services;


public class ModCreativeModeTabs
{
    public static final RegistryObject<CreativeModeTab> CIU_TAB =
            Services.REGISTRY.creativeModeTabs().register(
                    "ciu_tab",
                    () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                                         .title(Component.translatable("itemGroup.ciu"))
                                         .icon(ModItems.EXAMPLE_ITEM.get()::getDefaultInstance)
                                         // FIX: Wrap the item in a default instance (ItemStack)
                                         .displayItems((parameters, output) -> {
                                             output.accept(ModItems.EXAMPLE_ITEM.get().getDefaultInstance());
                                             output.accept(ModBlocks.EXAMPLE_BLOCK_ITEM.get().getDefaultInstance()); // Added your block too!
                                         })
                                         .build());
    
    public static void init(){}
}
}
