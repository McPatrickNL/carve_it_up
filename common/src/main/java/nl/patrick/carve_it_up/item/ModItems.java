package nl.patrick.carve_it_up.item;

import net.minecraft.world.item.Item;
import nl.patrick.carve_it_up.registry.ItemRegistry;
import nl.patrick.carve_it_up.registry.RegistryObject;
import nl.patrick.carve_it_up.services.Services;

import static nl.patrick.carve_it_up.CommonMod.LOGGER;


// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/item/ModItems.java
public class ModItems
{
    public static final ItemRegistry ITEMS = Services.REGISTRY.items();
    
    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", Item::new);
    public static final RegistryObject<Item> CARVING_TOOL = ITEMS.register("carving_tool", CarvingToolItem::new);
    
    public static void init(){
        LOGGER.info("Registering Items for Carve It Up.");
    }
}
