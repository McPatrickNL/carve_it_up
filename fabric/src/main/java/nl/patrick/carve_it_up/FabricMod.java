// File Location from project root:
// fabric/src/main/java/nl/patrick/carve_it_up/FabricMod.java
package nl.patrick.carve_it_up;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import nl.patrick.carve_it_up.block.ModBlocks;
import nl.patrick.carve_it_up.component.ModComponents;
import nl.patrick.carve_it_up.item.ModItems;
import nl.patrick.carve_it_up.network.CarvingNetworkHandlers;
import nl.patrick.carve_it_up.network.RequestCarveActionPayload;
import nl.patrick.carve_it_up.network.SyncCarvedDataPayload;

public class FabricMod implements ModInitializer {
    private static final Identifier myComponent = Identifier.fromNamespaceAndPath("mymod", "my_component");
    public static final DataComponentType<ModComponents.MyCustomData> MY_COMPONENT = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        myComponent,
        DataComponentType.<ModComponents.MyCustomData>builder()
                         .persistent(ModComponents.MyCustomData.CODEC)
                         .networkSynchronized(ModComponents.MyCustomData.STREAM_CODEC)
                         .build()
    );

    @Override
    public void onInitialize() {
        // 1. Trigger the common registration block (Registers blocks/items/tabs)
        CommonMod.init();

        ResourceKey<CreativeModeTab> tabKey = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(CommonMod.MOD_ID, "carve_it_up_tab")
        );

        // 2. Populate your multiplatform creative tab on Fabric
        CreativeModeTabEvents.modifyOutputEvent(tabKey).register(content -> {
            content.accept(ModItems.EXAMPLE_ITEM.get());
            content.accept(ModBlocks.EXAMPLE_BLOCK_ITEM.get());
            content.accept(ModItems.CARVING_TOOL.get());
            content.accept(ModItems.IRON_CARVING_TOOL.get());
        });

        // NewStart Register payload types for C2S and S2C communication on Fabric
        PayloadTypeRegistry.serverboundPlay().register(RequestCarveActionPayload.TYPE, RequestCarveActionPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(nl.patrick.carve_it_up.network.RequestChangeBaseMaterialPayload.TYPE, nl.patrick.carve_it_up.network.RequestChangeBaseMaterialPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(nl.patrick.carve_it_up.network.RequestPasteCarvingDataPayload.TYPE, nl.patrick.carve_it_up.network.RequestPasteCarvingDataPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncCarvedDataPayload.TYPE, SyncCarvedDataPayload.STREAM_CODEC);

        // Server-side receiver for the carve action request
        ServerPlayNetworking.registerGlobalReceiver(RequestCarveActionPayload.TYPE, (payload, context) ->
            context.server().execute(() -> CarvingNetworkHandlers.handleCarveActionRequest(payload, context.player()))
        );
        ServerPlayNetworking.registerGlobalReceiver(nl.patrick.carve_it_up.network.RequestChangeBaseMaterialPayload.TYPE, (payload, context) ->
            context.server().execute(() -> CarvingNetworkHandlers.handleChangeBaseMaterialRequest(payload, context.player()))
        );
        ServerPlayNetworking.registerGlobalReceiver(nl.patrick.carve_it_up.network.RequestPasteCarvingDataPayload.TYPE, (payload, context) ->
            context.server().execute(() -> CarvingNetworkHandlers.handlePasteCarvingDataRequest(payload, context.player()))
        );
        // NewEnd
    }
}