package dev.hollowhands;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class HollowHandsScreen extends Screen {
    private HollowHandsStorage.Compartment selected = HollowHandsStorage.Compartment.BELT;
    private int selectedSlot = 0;
    private Button[] slotButtons;
    private Button[] tabButtons;

    public HollowHandsScreen() {
        super(Component.literal("Hollow Hands — Carrying Gear"));
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        int top = Math.max(24, this.height / 2 - 112);

        tabButtons = new Button[HollowHandsStorage.Compartment.values().length];
        int tabX = center - 154;
        int tabY = top;
        for (HollowHandsStorage.Compartment compartment : HollowHandsStorage.Compartment.values()) {
            final HollowHandsStorage.Compartment target = compartment;
            tabButtons[compartment.ordinal()] = addRenderableWidget(Button.builder(
                    tabLabel(compartment),
                    b -> { selected = target; selectedSlot = 0; rebuildWidgets(); }
            ).bounds(tabX, tabY, 76, 20).build());
            tabX += 78;
        }

        slotButtons = new Button[selected.capacity];
        for (int i = 0; i < selected.capacity; i++) {
            final int slot = i;
            int x = center - 150 + (i % 3) * 102;
            int y = top + 34 + (i / 3) * 25;
            slotButtons[i] = addRenderableWidget(Button.builder(slotLabel(i), b -> {
                selectedSlot = slot;
                refreshLabels();
            }).bounds(x, y, 98, 21).build());
        }

        addRenderableWidget(Button.builder(Component.literal("Store held stack"), b -> deposit())
                .bounds(center - 150, top + 112, 145, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Take selected slot"), b -> withdraw())
                .bounds(center + 5, top + 112, 145, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(center - 45, top + 142, 90, 20).build());
    }

    private Component tabLabel(HollowHandsStorage.Compartment compartment) {
        int used = minecraft == null || minecraft.player == null ? 0 : HollowHandsStorage.used(minecraft.player, compartment);
        return Component.literal(compartment.label + " (" + used + "/" + compartment.capacity + ")");
    }

    private Component slotLabel(int slot) {
        ItemStack stack = currentStack(slot);
        String label = stack.isEmpty() ? "Empty" : stack.getHoverName().getString() + " ×" + stack.getCount();
        return Component.literal((slot + 1) + "  " + label + (selectedSlot == slot ? "  ◀" : ""));
    }

    private ItemStack currentStack(int slot) {
        if (minecraft == null || minecraft.player == null) return ItemStack.EMPTY;
        return HollowHandsStorage.get(minecraft.player, selected, slot);
    }

    private void refreshLabels() {
        if (slotButtons != null) {
            for (int i = 0; i < slotButtons.length; i++) slotButtons[i].setMessage(slotLabel(i));
        }
        if (tabButtons != null) {
            for (HollowHandsStorage.Compartment c : HollowHandsStorage.Compartment.values()) {
                tabButtons[c.ordinal()].setMessage(tabLabel(c));
            }
        }
    }

    private void deposit() {
        if (minecraft == null || minecraft.player == null || currentStack(selectedSlot).isEmpty()) {
            ClientPlayNetworking.send(new StorageActionPayload(selected.ordinal(), selectedSlot, StorageActionPayload.STORE));
            return;
        }
        // The server validates the held stack and slot before changing either.
        ClientPlayNetworking.send(new StorageActionPayload(selected.ordinal(), selectedSlot, StorageActionPayload.STORE));
    }

    private void withdraw() {
        if (minecraft == null || minecraft.player == null) return;
        ClientPlayNetworking.send(new StorageActionPayload(selected.ordinal(), selectedSlot, StorageActionPayload.TAKE));
    }

    @Override
    public void tick() {
        super.tick();
        refreshLabels();
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.gui.setScreen(null);
    }
}
