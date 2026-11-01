package dev.hollowhands;

public enum StorageCompartment {
    BELT("Belt", 2, 0), BACK("Back", 6, 2), SATCHEL("Satchel", 9, 8), BANDOLIER("Bandolier", 4, 17);
    public final String label;
    public final int capacity;
    public final int offset;
    StorageCompartment(String label, int capacity, int offset) { this.label = label; this.capacity = capacity; this.offset = offset; }
    public static StorageCompartment fromId(int id) {
        StorageCompartment[] values = values();
        return id >= 0 && id < values.length ? values[id] : null;
    }
}
