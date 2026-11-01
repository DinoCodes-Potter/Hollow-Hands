package dev.hollowhands;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class HollowHandsStorage {
    private HollowHandsStorage() {}
    public static ItemStack get(Player player, StorageCompartment compartment, int slot) {
        if (slot < 0 || slot >= compartment.capacity) return ItemStack.EMPTY;
        return HollowHandsData.get(player).get(compartment.offset + slot);
    }
    public static int used(Player player, StorageCompartment compartment) {
        int count = 0;
        for (int i = 0; i < compartment.capacity; i++) if (!get(player, compartment, i).isEmpty()) count++;
        return count;
    }
}
