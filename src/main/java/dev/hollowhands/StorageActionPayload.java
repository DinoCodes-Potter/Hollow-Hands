package dev.hollowhands;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StorageActionPayload(int compartment, int slot, int action) implements CustomPacketPayload {
    public static final int STORE = 0;
    public static final int TAKE = 1;

    public static final Type<StorageActionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("hollowhands", "storage_action")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageActionPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, StorageActionPayload::compartment,
                    ByteBufCodecs.VAR_INT, StorageActionPayload::slot,
                    ByteBufCodecs.VAR_INT, StorageActionPayload::action,
                    StorageActionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
