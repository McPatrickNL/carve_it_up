package nl.patrick.carve_it_up.component;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/component/ModComponents.java

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import nl.patrick.carve_it_up.registry.RegistryObject;
import nl.patrick.carve_it_up.services.Services;

import java.util.ArrayList;
import java.util.List;

import static nl.patrick.carve_it_up.CommonMod.LOGGER;


public class ModComponents
{
    public static final RegistryObject<DataComponentType<MyCustomData>> MY_COMPONENT =
            Services.REGISTRY.components().register("my_component", (DataComponentType.Builder<MyCustomData> builder) -> builder
                                                            .persistent(MyCustomData.CODEC)
                                                            .networkSynchronized(MyCustomData.STREAM_CODEC)
                                                   );
    
    public static void init() {
        LOGGER.info("Registering Data Components for Carve It Up.");
    }
    
    // Your actual Data Component record
    public record MyCustomData(String message, int count) {
        public static final Codec<MyCustomData> CODEC = RecordCodecBuilder.create(instance ->
                                                                                          instance.group(
                                                                                                  Codec.STRING.fieldOf("message").forGetter(MyCustomData::message),
                                                                                                  Codec.INT.fieldOf("count").forGetter(MyCustomData::count)
                                                                                                        ).apply(instance, MyCustomData::new)
                                                                                 );
        
        public static final StreamCodec<FriendlyByteBuf, MyCustomData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, MyCustomData::message,
                ByteBufCodecs.INT, MyCustomData::count,
                MyCustomData::new
                                                                                                           );
    }
}
