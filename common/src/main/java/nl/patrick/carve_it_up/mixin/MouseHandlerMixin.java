package nl.patrick.carve_it_up.mixin;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/MouseHandlerMixin.java

import net.minecraft.client.MouseHandler;
import nl.patrick.carve_it_up.carving.CarvingToolClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(MouseHandler.class)
public class MouseHandlerMixin
{
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onScroll(long window, double xoffset, double yoffset, CallbackInfo ci) {
        // yoffset captures standard mouse wheel inputs
        if (CarvingToolClientState.handleScroll(yoffset)) {
            ci.cancel();
        }
    }
}
