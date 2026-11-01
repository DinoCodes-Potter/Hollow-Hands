package dev.hollowhands;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Renders equipment in the player's torso coordinate space.
 *
 * The old implementation positioned items relative to the entity's bounding-box
 * height. That is not the same coordinate space as a model render layer, which
 * caused the equipment to drift up beside the head. Anchoring to the torso also
 * makes the gear follow body animation and rotation.
 */
public final class HollowHandsRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    public HollowHandsRenderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                       AvatarRenderState state, float limbAngle, float limbDistance) {
        List<ItemStackRenderState> gear = state.getData(HollowHandsRenderData.GEAR);
        if (gear == null || gear.size() != 4) return;

        List<ItemStackRenderState> contents = state.getData(HollowHandsRenderData.CONTENTS);

        poseStack.pushPose();
        // All coordinates below are local to the torso: +Y moves down,
        // -Z faces the front of the player, and +Z faces the player's back.
        getParentModel().body.translateAndRotate(poseStack);

        // Wearables: waist, back, hip, then diagonal chest strap.
        draw(gear.get(0), poseStack, collector, light, state, 0.0F, 0.66F, -0.035F, 0.65F);
        draw(gear.get(1), poseStack, collector, light, state, 0.0F, 0.36F, 0.105F, 0.82F);
        draw(gear.get(2), poseStack, collector, light, state, 0.245F, 0.67F, -0.005F, 0.72F);
        draw(gear.get(3), poseStack, collector, light, state, 0.0F, 0.34F, -0.045F, 0.72F);

        if (contents != null && contents.size() == HollowHandsData.TOTAL_SLOTS) {
            // Belt: two small items across the front of the waist.
            for (int i = 0; i < 2; i++) {
                draw(contents.get(i), poseStack, collector, light, state,
                        -0.105F + i * 0.21F, 0.65F, -0.105F, 0.19F);
            }

            // Backpack: six items in two rows, sitting just off the back panel.
            for (int i = 0; i < 6; i++) {
                draw(contents.get(2 + i), poseStack, collector, light, state,
                        -0.17F + (i % 3) * 0.17F,
                        0.25F + (i / 3) * 0.22F, 0.18F, 0.17F);
            }

            // Satchel: a compact 3x3 arrangement on the outer face of the hip pouch.
            for (int i = 0; i < 9; i++) {
                draw(contents.get(8 + i), poseStack, collector, light, state,
                        0.245F, 0.55F - (i / 3) * 0.105F,
                        -0.075F + (i % 3) * 0.055F, 0.12F);
            }

            // Bandolier: four quick-access items following the diagonal strap.
            for (int i = 0; i < 4; i++) {
                draw(contents.get(17 + i), poseStack, collector, light, state,
                        -0.17F + i * 0.105F, 0.18F + i * 0.13F,
                        -0.105F, 0.15F);
            }
        }
        poseStack.popPose();
    }

    private static void draw(ItemStackRenderState item, PoseStack poseStack,
                             SubmitNodeCollector collector, int light,
                             AvatarRenderState state, float x, float y, float z,
                             float scale) {
        if (item == null || item.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(scale, scale, scale);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
