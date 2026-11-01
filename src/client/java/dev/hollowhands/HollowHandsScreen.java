package dev.hollowhands;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class HollowHandsScreen extends Screen {
    private HollowHandsStorage.Compartment selected = HollowHandsStorage.Compartment.BELT;
    private int selectedSlot = 0;
    private Button[] slotButtons;

    public HollowHandsScreen() {
        super(Component.literal("Hollow Hands — Carrying Gear"));
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int top = Math.max(24, this.height / 2 - 112);

        int tabX = center - 154;
        int tabY = top;
        for (HollowHandsStorage.Compartment compartment : HollowHandsStorage.Compartment.values()) {
            addRenderableWidget(Button.builder(
                    Component.literal(compartment.label + " (" + HollowHandsStorage.used(compartment) + "/" + compartment.capacity + ")"),
                    b -> { selected = compartment; selectedSlot = 0; rebuildWidgets(); }
            ).bounds(tabX, tabY, 76, 20).build());
            tabX += 78;
        }

        slotButtons = new Button[selected.capacity];
        int columns = 3;
        for (int i = 0; i < selected.capacity; i++) {
            final int slot = i;
            int x = center - 150 + (i % columns) * 102;
            int y = top + 34 + (i / columns) * 25;
            slotButtons[i] = addRenderableWidget(Button.builder(slotLabel(i), b -> {
                selectedSlot = slot;
                refreshLabels();
            }).bounds(x, y, 98, 21).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Store held stack"), b -> deposit()).bounds(center - 150, top + 112, 145, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Take selected slot"), b -> withdraw()).bounds(center + 5, top + 112, 145, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).bounds(center - 45, top + 142, 90, 20).build());
    }

    private Component slotLabel(int slot) {
        HollowHandsStorage.StoredItem item = HollowHandsStorage.get(selected, slot);
        if (item.empty()) return Component.literal((slot + 1) + "  — Empty" + (selectedSlot == slot ? "  ◀" : ""));
        String id = item.itemId().substring(item.itemId().indexOf(':') + 1);
        return Component.literal((slot + 1) + "  " + id + " ×" + item.count() + (selectedSlot == slot ? "  ◀" : ""));
    }

    private void refreshLabels() {
        for (int i = 0; i < slotButtons.length; i++) slotButtons[i].setMessage(slotLabel(i));
    }

    private void deposit() {
        if (minecraft == null || minecraft.player == null) return;
        ItemStack held = minecraft.player.getMainHandItem();
        if (HollowHandsStorage.deposit(selected, selectedSlot, held)) {
            held.shrink(held.getCount());
            refreshLabels();
            rebuildWidgets();
        }
    }

    private void withdraw() {
        if (minecraft == null || minecraft.player == null) return;
        int hotbar = minecraft.player.getInventory().getSelectedSlot();
        if (!minecraft.player.getInventory().getItem(hotbar).isEmpty()) return;
        ItemStack stack = HollowHandsStorage.take(selected, selectedSlot);
        if (!stack.isEmpty()) {
            minecraft.player.getInventory().setItem(hotbar, stack);
            rebuildWidgets();
        }
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.gui.setScreen(null);
    }
}
