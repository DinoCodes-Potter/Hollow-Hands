package dev.hollowhands;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class HollowHandsStorage {
    public enum Compartment {
        BELT("Belt", 2, 0),
        BACK("Back", 6, 2),
        SATCHEL("Satchel", 9, 8),
        BANDOLIER("Bandolier", 4, 17);

        public final String label;
        public final int capacity;
        public final int offset;

        Compartment(String label, int capacity, int offset) {
            this.label = label;
            this.capacity = capacity;
            this.offset = offset;
        }

        public static Compartment fromId(int id) {
            Compartment[] values = values();
            return id >= 0 && id < values.length ? values[id] : null;
        }
    }

    private HollowHandsStorage() {}

    public static ItemStack get(Player player, Compartment compartment, int slot) {
        if (slot < 0 || slot >= compartment.capacity) return ItemStack.EMPTY;
        return HollowHandsData.get(player).get(compartment.offset + slot);
    }

    public static int used(Player player, Compartment compartment) {
        int count = 0;
        for (int i = 0; i < compartment.capacity; i++) {
            if (!get(player, compartment, i).isEmpty()) count++;
        }
        return count;
    }
}
