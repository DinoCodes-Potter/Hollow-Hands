package dev.hollowhands;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class HollowHandsScreen extends Screen {
    private StorageCompartment selected = StorageCompartment.BELT;
    private int selectedSlot = 0;
    private Button[] slotButtons;
    private Button[] tabButtons;

    public HollowHandsScreen() {
        super(Component.literal("HOLLOW HANDS  |  CARRYING GEAR"));
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int top = Math.max(20, this.height / 2 - 118);

        tabButtons = new Button[StorageCompartment.values().length];
        int tabX = center - 162;
        for (StorageCompartment compartment : StorageCompartment.values()) {
            final StorageCompartment target = compartment;
            tabButtons[compartment.ordinal()] = addRenderableWidget(
                    Button.builder(tabLabel(compartment), b -> {
                        selected = target;
                        selectedSlot = 0;
                        rebuildWidgets();
                    }).bounds(tabX, top, 78, 22).build());
            tabX += 82;
        }

        slotButtons = new Button[selected.capacity];
        for (int i = 0; i < selected.capacity; i++) {
            final int slot = i;
            int x = center - 156 + (i % 3) * 106;
            int y = top + 39 + (i / 3) * 27;
            slotButtons[i] = addRenderableWidget(Button.builder(slotLabel(i), b -> {
                selectedSlot = slot;
                refreshLabels();
            }).bounds(x, y, 102, 23).build());
        }

        int actionY = top + 39 + ((selected.capacity + 2) / 3) * 27 + 9;
        addRenderableWidget(Button.builder(Component.literal("STORE HELD ITEM"), b -> deposit())
                .bounds(center - 156, actionY, 151, 25).build());
        addRenderableWidget(Button.builder(Component.literal("TAKE SELECTED ITEM"), b -> withdraw())
                .bounds(center + 5, actionY, 151, 25).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"), b -> onClose())
                .bounds(center - 45, actionY + 34, 90, 20).build());
    }

    private Component tabLabel(StorageCompartment compartment) {
        int used = minecraft == null || minecraft.player == null ? 0
                : HollowHandsStorage.used(minecraft.player, compartment);
        return Component.literal(compartment.label.toUpperCase() + "  " + used + "/" + compartment.capacity);
    }

    private Component slotLabel(int slot) {
        ItemStack stack = currentStack(slot);
        String label = stack.isEmpty() ? "EMPTY" :
                stack.getHoverName().getString() + "  x" + stack.getCount();
        return Component.literal((selectedSlot == slot ? "◆ " : "◇ ")
                + String.format("%02d", slot + 1) + "  " + label);
    }

    private ItemStack currentStack(int slot) {
        if (minecraft == null || minecraft.player == null) return ItemStack.EMPTY;
        return HollowHandsStorage.get(minecraft.player, selected, slot);
    }

    private void refreshLabels() {
        if (slotButtons != null)
            for (int i = 0; i < slotButtons.length; i++) slotButtons[i].setMessage(slotLabel(i));
        if (tabButtons != null)
            for (StorageCompartment c : StorageCompartment.values())
                tabButtons[c.ordinal()].setMessage(tabLabel(c));
    }

    private void deposit() {
        if (minecraft != null && minecraft.player != null)
            ClientPlayNetworking.send(new StorageActionPayload(selected.ordinal(), selectedSlot,
                    StorageActionPayload.STORE));
    }

    private void withdraw() {
        if (minecraft != null && minecraft.player != null)
            ClientPlayNetworking.send(new StorageActionPayload(selected.ordinal(), selectedSlot,
                    StorageActionPayload.TAKE));
    }

    @Override public void tick() { super.tick(); refreshLabels(); }
    @Override public void onClose() { if (minecraft != null) minecraft.gui.setScreen(null); }
}
