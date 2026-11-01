package dev.hollowhands;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class HollowHandsStorage {
    public enum Compartment {
        BELT("Belt", 2),
        BACK("Back", 6),
        SATCHEL("Satchel", 9),
        BANDOLIER("Bandolier", 4);

        public final String label;
        public final int capacity;
        Compartment(String label, int capacity) {
            this.label = label;
            this.capacity = capacity;
        }
    }

    public record StoredItem(String itemId, int count) {
        public boolean empty() { return itemId == null || itemId.isBlank() || count <= 0; }
        public static StoredItem emptySlot() { return new StoredItem("", 0); }
    }

    private static final StoredItem[][] ITEMS = new StoredItem[Compartment.values().length][];
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("hollowhands-storage.properties");

    static {
        for (Compartment c : Compartment.values()) {
            ITEMS[c.ordinal()] = new StoredItem[c.capacity];
            for (int i = 0; i < c.capacity; i++) ITEMS[c.ordinal()][i] = StoredItem.emptySlot();
        }
    }

    private HollowHandsStorage() {}

    public static synchronized void load() {
        Properties p = new Properties();
        if (Files.exists(FILE)) {
            try (var in = Files.newInputStream(FILE)) { p.load(in); }
            catch (IOException e) { System.err.println("[Hollow Hands] Could not load storage: " + e); }
        }
        for (Compartment c : Compartment.values()) {
            for (int i = 0; i < c.capacity; i++) {
                String raw = p.getProperty(key(c, i), "");
                String[] parts = raw.split("\\|", 2);
                if (parts.length == 2) {
                    try { ITEMS[c.ordinal()][i] = new StoredItem(parts[0], Integer.parseInt(parts[1])); }
                    catch (NumberFormatException ignored) { ITEMS[c.ordinal()][i] = StoredItem.emptySlot(); }
                }
            }
        }
    }

    public static synchronized StoredItem get(Compartment c, int slot) {
        return ITEMS[c.ordinal()][slot];
    }

    public static synchronized boolean deposit(Compartment c, int slot, ItemStack held) {
        if (held == null || held.isEmpty() || !get(c, slot).empty()) return false;
        ITEMS[c.ordinal()][slot] = new StoredItem(BuiltInRegistries.ITEM.getKey(held.getItem()).toString(), held.getCount());
        save();
        return true;
    }

    public static synchronized ItemStack take(Compartment c, int slot) {
        StoredItem stored = get(c, slot);
        if (stored.empty()) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(stored.itemId()));
        if (item == null) return ItemStack.EMPTY;
        ItemStack result = new ItemStack(item, stored.count());
        ITEMS[c.ordinal()][slot] = StoredItem.emptySlot();
        save();
        return result;
    }

    public static synchronized int used(Compartment c) {
        int used = 0;
        for (StoredItem item : ITEMS[c.ordinal()]) if (!item.empty()) used++;
        return used;
    }

    private static String key(Compartment c, int slot) { return c.name().toLowerCase() + "." + slot; }

    private static synchronized void save() {
        Properties p = new Properties();
        for (Compartment c : Compartment.values()) {
            for (int i = 0; i < c.capacity; i++) {
                StoredItem item = ITEMS[c.ordinal()][i];
                if (!item.empty()) p.setProperty(key(c, i), item.itemId() + "|" + item.count());
            }
        }
        try {
            Files.createDirectories(FILE.getParent());
            try (var out = Files.newOutputStream(FILE)) { p.store(out, "Hollow Hands local storage"); }
        } catch (IOException e) {
            System.err.println("[Hollow Hands] Could not save storage: " + e);
        }
    }
}
