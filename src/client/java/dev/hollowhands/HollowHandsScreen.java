package dev.hollowhands;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** A compact, styled storage screen that keeps Minecraft's normal widget handling. */
public final class HollowHandsScreen extends Screen {
    private StorageCompartment selected = StorageCompartment.BELT;
    private int selectedSlot;
    private Button[] slotButtons;
    private Button[] tabButtons;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public HollowHandsScreen() {
        super(Component.literal("Hollow Hands Storage"));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(380, this.width - 20);
        panelHeight = Math.min(286, this.height - 20);
        panelLeft = (this.width - panelWidth) / 2;
        panelTop = (this.height - panelHeight) / 2;

        tabButtons = new Button[StorageCompartment.values().length];
        int tabY = panelTop + 76;
        for (StorageCompartment compartment : StorageCompartment.values()) {
            final StorageCompartment target = compartment;
            tabButtons[compartment.ordinal()] = addRenderableWidget(
                    Button.builder(tabLabel(compartment), button -> {
                        selected = target;
                        selectedSlot = 0;
                        rebuildWidgets();
                    }).bounds(panelLeft + 12, tabY, 96, 25).build());
            tabY += 32;
        }

        int gridX = panelLeft + 120;
        int gridY = panelTop + 91;
        int slotWidth = (panelWidth - 146) / 2;
        slotButtons = new Button[selected.capacity];
        for (int i = 0; i < selected.capacity; i++) {
            final int slot = i;
            int x = gridX + (i % 2) * (slotWidth + 8);
            int y = gridY + (i / 2) * 24;
            slotButtons[i] = addRenderableWidget(Button.builder(slotLabel(i), button -> {
                selectedSlot = slot;
                refreshLabels();
            }).bounds(x, y, slotWidth, 20).build());
        }

        int actionY = panelTop + panelHeight - 37;
        int actionWidth = (panelWidth - 48) / 3;
        addRenderableWidget(Button.builder(Component.literal("STORE HELD"), button -> deposit())
                .bounds(panelLeft + 12, actionY, actionWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("TAKE ITEM"), button -> withdraw())
                .bounds(panelLeft + 18 + actionWidth, actionY, actionWidth, 20).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"), button -> onClose())
                .bounds(panelLeft + 24 + actionWidth * 2, actionY, actionWidth, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xB8000000);
        int x = panelLeft;
        int y = panelTop;
        int right = x + panelWidth;
        int bottom = y + panelHeight;

        graphics.fill(x, y, right, bottom, 0xF00B0D12);
        graphics.fill(x + 1, y + 1, right - 1, y + 48, 0xFF191D25);
        graphics.fill(x + 10, y + 54, x + 110, bottom - 47, 0xFF15181F);
        graphics.fill(x + 116, y + 54, right - 10, bottom - 47, 0xFF12151B);
        graphics.fill(x + 10, y + 49, right - 10, y + 51, 0xFF9B7043);
        graphics.outline(x, y, panelWidth, panelHeight, 0xFF8C6842);
        graphics.outline(x + 9, y + 53, 102, panelHeight - 101, 0xFF39302A);
        graphics.outline(x + 115, y + 53, panelWidth - 125, panelHeight - 101, 0xFF39302A);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int x = panelLeft;
        int y = panelTop;
        int right = x + panelWidth;
        int bottom = y + panelHeight;
        graphics.text(font, "HOLLOW HANDS", x + 16, y + 12, 0xFFFFE4B7, false);
        graphics.text(font, "FIELD STORAGE  /  GEAR MANAGEMENT", x + 17, y + 29,
                0xFFB5A28B, false);
        graphics.text(font, "21 SLOTS", right - 63, y + 15, 0xFFD3B17B, false);
        graphics.text(font, "COMPARTMENTS", x + 17, y + 61, 0xFFB89A70, false);
        graphics.text(font, selected.label.toUpperCase(), x + 127, y + 60,
                0xFFFFDDA8, false);
        graphics.text(font, "SELECT A SLOT  /  STORE OR RETRIEVE ITEMS", x + 127, y + 73,
                0xFF988E82, false);

        ItemStack stack = currentStack(selectedSlot);
        String status = stack.isEmpty() ? "SELECTED SLOT: EMPTY"
                : "SELECTED: " + stack.getHoverName().getString() + "  x" + stack.getCount();
        if (status.length() > 48) status = status.substring(0, 47) + "…";
        int infoY = bottom - 61;
        graphics.fill(x + 118, infoY, right - 10, infoY + 18, 0xFF201D19);
        graphics.text(font, status, x + 125, infoY + 5, 0xFFD4C5AF, false);
        graphics.text(font, "G  OPEN/CLOSE     ESC  RETURN TO GAME", x + 15, bottom - 12,
                0xFF776D62, false);
    }

    private Component tabLabel(StorageCompartment compartment) {
        int used = minecraft == null || minecraft.player == null
                ? 0 : HollowHandsStorage.used(minecraft.player, compartment);
        return Component.literal(compartment.label.toUpperCase() + "  "
                + used + "/" + compartment.capacity);
    }

    private Component slotLabel(int slot) {
        ItemStack stack = currentStack(slot);
        String content = stack.isEmpty() ? "EMPTY" : stack.getHoverName().getString();
        if (content.length() > 12) content = content.substring(0, 11) + "…";
        if (!stack.isEmpty()) content += " x" + stack.getCount();
        return Component.literal((selectedSlot == slot ? "◆ " : "◇ ")
                + String.format("%02d", slot + 1) + "  " + content);
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
            for (StorageCompartment compartment : StorageCompartment.values()) {
                tabButtons[compartment.ordinal()].setMessage(tabLabel(compartment));
            }
        }
    }

    private void deposit() {
        if (minecraft != null && minecraft.player != null) {
            ClientPlayNetworking.send(new StorageActionPayload(selected.ordinal(), selectedSlot,
                    StorageActionPayload.STORE));
        }
    }

    private void withdraw() {
        if (minecraft != null && minecraft.player != null) {
            ClientPlayNetworking.send(new StorageActionPayload(selected.ordinal(), selectedSlot,
                    StorageActionPayload.TAKE));
        }
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
