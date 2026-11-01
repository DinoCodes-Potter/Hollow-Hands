package dev.hollowhands;
import java.util.List;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
public final class HollowHandsRenderData {
 public static final RenderStateDataKey<List<ItemStackRenderState>> GEAR=RenderStateDataKey.create(()->"hollowhands:wearable_gear");
 public static final RenderStateDataKey<List<ItemStackRenderState>> CONTENTS=RenderStateDataKey.create(()->"hollowhands:visible_stored_items");
 private HollowHandsRenderData(){}
}