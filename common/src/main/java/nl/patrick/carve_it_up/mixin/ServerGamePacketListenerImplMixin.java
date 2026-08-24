// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/mixin/ServerGamePacketListenerImplMixin.java
package nl.patrick.carve_it_up.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import nl.patrick.carve_it_up.carving.CarvedData;
import nl.patrick.carve_it_up.carving.CarvedItemHelper;
import nl.patrick.carve_it_up.carving.CarvingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into ServerGamePacketListenerImpl to give and select the carved block on middle-click (Pick Block)
 * even if it is not already present in the player's inventory.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    // NewStart Handle middle-click pick block for carved blocks
    @Inject(
        method = "handlePickItemFromBlock(Lnet/minecraft/network/protocol/game/ServerboundPickItemFromBlockPacket;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void interceptPickItemFromBlock(ServerboundPickItemFromBlockPacket packet, CallbackInfo callbackInfo) {
        if (this.player == null) {
            return;
        }

        ServerLevel serverLevel = this.player.level();
        BlockPos targetPos = packet.pos();

        if (!this.player.isWithinBlockInteractionRange(targetPos, 1.0) || !serverLevel.isLoaded(targetPos)) {
            return;
        }

        if (CarvingManager.isCarved(serverLevel, targetPos)) {
            CarvedData carvedData = CarvingManager.getCarvedData(serverLevel, targetPos);
            if (carvedData != null) {
                ItemStack carvedDrop = CarvedItemHelper.createCarvedBlockDrop(carvedData);
                if (!carvedDrop.isEmpty()) {
                    Inventory inventory = this.player.getInventory();
                    int matchingSlot = inventory.findSlotMatchingItem(carvedDrop);

                    if (matchingSlot != -1) {
                        if (Inventory.isHotbarSlot(matchingSlot)) {
                            inventory.setSelectedSlot(matchingSlot);
                        } else {
                            inventory.pickSlot(matchingSlot);
                        }
                    } else {
                        // Give the carved block and select it in the player's hand even if not in inventory
                        inventory.addAndPickItem(carvedDrop);
                    }

                    this.player.connection.send(new ClientboundSetHeldSlotPacket(inventory.getSelectedSlot()));
                    this.player.inventoryMenu.broadcastChanges();
                    callbackInfo.cancel();
                }
            }
        }
    }
    // NewEnd
}
