package dev.hollowhands;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
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
        super(Component.literal("Hollow Hands"));
    }

    private int panelTop() { return Math.max(14, this.height / 2 - 132); }

    @Override
    protected void init() {
        int center = this.width / 2;
        int top = panelTop() + 42;
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

        int actionY = top + 39 + ((selected.capacity + 2) / 3) * 27 + 7;
        addRenderableWidget(Button.builder(Component.literal("＋  Store held item"), b -> deposit())
                .bounds(center - 156, actionY, 151, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Take selected  →"), b -> withdraw())
                .bounds(center + 5, actionY, 151, 24).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(center - 45, actionY + 32, 90, 20).build());
    }

    @Override
    protected void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        int cx = this.width / 2;
        int top = panelTop();
        int left = cx - 176;
        int bottom = Math.min(this.height - 12, top + 238);

        g.fill(0, 0, this.width, this.height, 0xB8101218);
        g.fill(left - 2, top - 2, cx + 178, bottom + 2, 0xFF090B10);
        g.fill(left, top, cx + 176, bottom, 0xFF242832);
        g.fill(left + 2, top + 2, cx + 174, top + 35, 0xFF343B48);
        g.fill(left + 2, top + 36, cx + 174, top + 37, 0xFFB18A50);
        g.fill(left + 10, top + 80, cx + 166, top + 81, 0xFF414957);
        g.fill(left + 10, bottom - 34, cx + 166, bottom - 33, 0xFF414957);
        g.fill(left + 4, top + 4, left + 7, top + 32, 0xFFC59A5A);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int cx = this.width / 2;
        int top = panelTop();
        g.drawString(this.font, "HOLLOW HANDS", cx - 158, top + 10, 0xFFF2E8D7, false);
        g.drawString(this.font, "CARRYING GEAR  /  STORAGE", cx + 22, top + 11, 0xFFB9A98F, false);
        g.drawString(this.font, selected.label.toUpperCase() + " COMPARTMENT", cx - 156, top + 70, 0xFFE4C28C, false);
        int used = minecraft == null || minecraft.player == null ? 0 : HollowHandsStorage.used(minecraft.player, selected);
        g.drawString(this.font, used + " / " + selected.capacity + " SLOTS USED", cx + 42, top + 70, 0xFFBFC7D2, false);
        g.drawCenteredString(this.font, "Select a slot, then store the item in your hand or retrieve it.",
                cx, Math.min(this.height - 22, top + 216), 0xFFAEB6C2);
    }

    private Component tabLabel(StorageCompartment compartment) {
        int used = minecraft == null || minecraft.player == null ? 0
                : HollowHandsStorage.used(minecraft.player, compartment);
        return Component.literal(compartment.label + "  " + used + "/" + compartment.capacity);
    }

    private Component slotLabel(int slot) {
        ItemStack stack = currentStack(slot);
        String label = stack.isEmpty() ? "Empty slot" :
                stack.getHoverName().getString() + " ×" + stack.getCount();
        return Component.literal((selectedSlot == slot ? "◆ " : "◇ ") + (slot + 1) + "  " + label);
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
