package nl.patrick.carve_it_up.registry;

// File Location from project root:
// neoforge/src/main/java/nl/patrick/carve_it_up/registry/NeoForgeItemRegistry.java

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import nl.patrick.carve_it_up.CommonMod;

import java.util.function.Function;


public class NeoForgeItemRegistry implements ItemRegistry
{
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, CommonMod.MOD_ID);
    
    @Override
    public RegistryObject<Item> register(String name, Function<Item.Properties, Item> factory) {
        return new NeoForgeRegistryObject<>(ITEMS.register(name, () -> {
            // Automatically pre-configure the ID on the item properties
            Item.Properties properties = new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CommonMod.MOD_ID, name)));
            return factory.apply(properties);
        }));
    }
}