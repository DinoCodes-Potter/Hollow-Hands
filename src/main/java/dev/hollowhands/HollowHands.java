package dev.hollowhands;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class HollowHands implements ModInitializer {
    @Override
    public void onInitialize() {
        // Force attachment registration before networking or players are loaded.
        HollowHandsData.register();

        PayloadTypeRegistry.serverboundPlay().register(
                StorageActionPayload.TYPE, StorageActionPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(StorageActionPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            HollowHandsStorage.Compartment compartment =
                    HollowHandsStorage.Compartment.fromId(payload.compartment());

            if (compartment == null || payload.slot() < 0 || payload.slot() >= compartment.capacity) {
                return;
            }

            int absoluteSlot = compartment.offset + payload.slot();
            List<ItemStack> slots = new ArrayList<>(HollowHandsData.get(player));
            if (slots.size() != HollowHandsData.TOTAL_SLOTS) {
                slots = new ArrayList<>(HollowHandsData.empty());
            }

            if (payload.action() == StorageActionPayload.STORE) {
                ItemStack held = player.getMainHandItem();
                if (held.isEmpty() || !slots.get(absoluteSlot).isEmpty()) {
                    return;
                }

                slots.set(absoluteSlot, held.copy());
                held.shrink(held.getCount());
                player.setAttached(HollowHandsData.STORAGE, List.copyOf(slots));
            } else if (payload.action() == StorageActionPayload.TAKE) {
                ItemStack stored = slots.get(absoluteSlot);
                if (stored.isEmpty()) {
                    return;
                }

                ItemStack remainder = stored.copy();
                player.getInventory().add(remainder);
                slots.set(absoluteSlot, remainder.isEmpty() ? ItemStack.EMPTY : remainder);
                player.setAttached(HollowHandsData.STORAGE, List.copyOf(slots));
            }
        });
    }
}
