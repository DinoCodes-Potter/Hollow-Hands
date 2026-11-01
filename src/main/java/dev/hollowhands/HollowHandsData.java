package dev.hollowhands;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;

public final class HollowHandsData {
    public static final int TOTAL_SLOTS = 21;

    public static final AttachmentType<List<ItemStack>> STORAGE = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath("hollowhands", "player_storage"),
            builder -> builder
                    .initializer(HollowHandsData::empty)
                    .persistent(Codec.list(ItemStack.OPTIONAL_CODEC))
                    .syncWith(
                            ByteBufCodecs.fromCodec(Codec.list(ItemStack.OPTIONAL_CODEC)),
                            AttachmentSyncPredicate.targetOnly()
                    )
    );

    private HollowHandsData() {}

    public static void register() {
        // Referencing STORAGE triggers its static registration.
    }

    public static List<ItemStack> empty() {
        return Collections.nCopies(TOTAL_SLOTS, ItemStack.EMPTY);
    }

    public static List<ItemStack> get(Player player) {
        List<ItemStack> value = player.getAttachedOrElse(STORAGE, empty());
        return value.size() == TOTAL_SLOTS ? value : empty();
    }
}
