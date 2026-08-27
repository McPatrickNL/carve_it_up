// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/item/ModItems.java
package nl.patrick.carve_it_up.item;

import net.minecraft.world.item.Item;
import nl.patrick.carve_it_up.registry.ItemRegistry;
import nl.patrick.carve_it_up.registry.RegistryObject;
import nl.patrick.carve_it_up.services.Services;

import static nl.patrick.carve_it_up.CommonMod.LOGGER;

public class ModItems {
    public static final ItemRegistry ITEMS = Services.REGISTRY.items();

    public static final RegistryObject<Item> STONE_CARVING_TOOLS = ITEMS.register(
        "stone_carving_tools",
        properties -> new CarvingToolItem(properties.durability(131).stacksTo(1))
    );

    public static final RegistryObject<Item> COPPER_CARVING_TOOLS = ITEMS.register(
        "copper_carving_tools",
        properties -> new CarvingToolItem(properties.durability(190).stacksTo(1))
    );

    public static final RegistryObject<Item> IRON_CARVING_TOOLS = ITEMS.register(
        "iron_carving_tools",
        properties -> new CarvingToolItem(properties.durability(250).stacksTo(1))
    );

    public static final RegistryObject<Item> GOLDEN_CARVING_TOOLS = ITEMS.register(
        "golden_carving_tools",
        properties -> new CarvingToolItem(properties.durability(32).stacksTo(1))
    );

    public static final RegistryObject<Item> DIAMOND_CARVING_TOOLS = ITEMS.register(
        "diamond_carving_tools",
        properties -> new CarvingToolItem(properties.durability(1561).stacksTo(1))
    );

    public static final RegistryObject<Item> NETHERITE_CARVING_TOOLS = ITEMS.register(
        "netherite_carving_tools",
        properties -> new CarvingToolItem(properties.durability(2031).stacksTo(1).fireResistant())
    );

    public static void init() {
        LOGGER.info("Registering Items for Carve It Up.");
    }
}
