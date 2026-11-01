package dev.hollowhands;

import java.util.List;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Physical gear is attached to the torso model, not the entity bounding box.
 * Contents use separate hanging transforms so long tools visibly dangle and
 * sway with the player's walk cycle.
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
        float walk = Math.min(1.0F, Math.max(0.0F, limbDistance));
        float gait = limbAngle * 0.6662F;

        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);

        // The model JSON now uses neutral display scale. These are the only
        // scale factors, preventing the old accidental double-shrink.
        // Belt wraps the waist; backpack sits behind the shoulders; satchel
        // hangs at the right hip; bandolier crosses the chest.
        draw(gear.get(0), poseStack, collector, light, state,
                0.0F, 0.655F, -0.035F, 0.82F);
        draw(gear.get(1), poseStack, collector, light, state,
                0.0F, 0.285F, 0.145F, 0.92F);
        drawRotated(gear.get(2), poseStack, collector, light, state,
                0.245F, 0.585F, -0.015F, 0.88F, 0.0F, 0.48F, 0.0F);
        draw(gear.get(3), poseStack, collector, light, state,
                0.0F, 0.34F, -0.145F, 0.90F);

        if (contents != null && contents.size() == HollowHandsData.TOTAL_SLOTS) {
            // Belt tools hang from two separate loops, handles up and heads
            // down. The pendulum rotation is driven by the player's gait.
            for (int i = 0; i < 2; i++) {
                float side = i == 0 ? -1.0F : 1.0F;
                float swing = (float) Math.sin(gait + i * 1.7F) * walk * 0.24F;
                drawHanging(contents.get(i), poseStack, collector, light, state,
                        side * 0.145F, 0.675F, -0.155F, 0.39F,
                        (i == 0 ? -0.12F : 0.12F) + swing);
            }

            // Backpack contents sit in two rows, slightly proud of the pack.
            for (int i = 0; i < 6; i++) {
                float x = -0.18F + (i % 3) * 0.18F;
                float y = 0.19F + (i / 3) * 0.19F;
                float swing = (float) Math.sin(gait + i * 0.8F) * walk * 0.055F;
                drawHanging(contents.get(2 + i), poseStack, collector, light, state,
                        x, y, 0.235F, 0.27F, swing);
            }

            // Satchel contents are displayed on the outward-facing flap.
            for (int i = 0; i < 9; i++) {
                float x = 0.315F;
                float y = 0.50F - (i / 3) * 0.105F;
                float z = -0.12F + (i % 3) * 0.055F;
                draw(contents.get(8 + i), poseStack, collector, light, state,
                        x, y, z, 0.19F);
            }

            // Bandolier tools hang from the diagonal strap in a spaced row.
            for (int i = 0; i < 4; i++) {
                float x = -0.17F + i * 0.105F;
                float y = 0.13F + i * 0.13F;
                float swing = (float) Math.sin(gait + i * 1.1F) * walk * 0.18F;
                drawHanging(contents.get(17 + i), poseStack, collector, light, state,
                        x, y, -0.17F, 0.29F, swing);
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

    private static void drawRotated(ItemStackRenderState item, PoseStack poseStack,
                                    SubmitNodeCollector collector, int light,
                                    AvatarRenderState state, float x, float y, float z,
                                    float scale, float xRot, float yRot, float zRot) {
        if (item == null || item.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(new Quaternionf().rotationY(yRot).rotateX(xRot).rotateZ(zRot));
        poseStack.scale(scale, scale, scale);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    private static void drawHanging(ItemStackRenderState item, PoseStack poseStack,
                                    SubmitNodeCollector collector, int light,
                                    AvatarRenderState state, float x, float y, float z,
                                    float scale, float swing) {
        if (item == null || item.isEmpty()) return;
        poseStack.pushPose();
        // Pivot at the attachment loop, so the lower end swings like a tool
        // hanging from a belt instead of rotating around its centre.
        poseStack.translate(x, y, z);
        poseStack.mulPose(new Quaternionf().rotationZ((float) Math.PI + swing));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0F, -0.10F, 0.0F);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}