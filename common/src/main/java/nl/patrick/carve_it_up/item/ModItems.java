package nl.patrick.carve_it_up.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import nl.patrick.carve_it_up.registry.RegistryObject;
import nl.patrick.carve_it_up.services.Services;

import static nl.patrick.carve_it_up.registry.ModCommonRegistries.*;


public class ModItems
{
//    // Creates a new food item with the id "carve_it_up:example_id", nutrition 1 and saturation 2
//    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem(
//            "example_item",
//            p -> p.food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));
    
    
    public static final RegistryObject<Item> EXAMPLE_ITEM =
            Services.REGISTRY.items().register(
                    "example_item",
                    () -> new Item(
                            new Item.Properties()));
    
    public static void init(){}
}
