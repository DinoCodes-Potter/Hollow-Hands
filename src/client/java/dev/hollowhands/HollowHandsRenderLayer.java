package dev.hollowhands;

import java.util.List;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Renders worn storage gear in the player's torso coordinate space. */
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
        getParentModel().body.translateAndRotate(poseStack);

        // The belt sits just outside the waist instead of intersecting the torso.
        drawScaled(gear.get(0), poseStack, collector, light, state,
                0.0F, 0.655F, -0.19F, 0.50F, 0.50F, 0.50F);
        drawBackpack(gear.get(1), poseStack, collector, light, state);
        drawSatchel(gear.get(2), poseStack, collector, light, state);
        drawScaled(gear.get(3), poseStack, collector, light, state,
                0.0F, 0.34F, -0.155F, 0.50F, 0.75F, 0.50F);

        if (contents != null && contents.size() == HollowHandsData.TOTAL_SLOTS) {
            for (int i = 0; i < 2; i++) {
                float side = i == 0 ? -1.0F : 1.0F;
                drawHanging(contents.get(i), poseStack, collector, light, state,
                        side * 0.145F, 0.675F, -0.145F, 0.16F,
                        i == 0 ? -0.08F : 0.08F);
            }
            drawBackpackContents(contents, poseStack, collector, light, state);
            drawSatchelContents(contents, poseStack, collector, light, state);
            for (int i = 0; i < 4; i++) {
                float x = -0.12F + i * 0.08F;
                float y = 0.12F + i * 0.12F;
                drawHanging(contents.get(17 + i), poseStack, collector, light, state,
                        x, y, -0.225F, 0.13F, 0.0F);
            }
        }
        poseStack.popPose();
    }

    private static void drawBackpack(ItemStackRenderState item, PoseStack poseStack,
                                     SubmitNodeCollector collector, int light,
                                     AvatarRenderState state) {
        if (item == null || item.isEmpty()) return;
        poseStack.pushPose();
        poseStack.mulPose(new Matrix4f().rotateY((float) Math.PI));
        poseStack.mulPose(new Matrix4f().rotateZ((float) Math.PI));
        // Restored the rear-body offset. The previous -0.02 value buried the pack in the torso.
        poseStack.translate(0.0F, -0.25F, -0.30F);
        poseStack.scale(0.50F, 0.75F, 0.50F);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    private static void drawBackpackContents(List<ItemStackRenderState> contents,
                                             PoseStack poseStack, SubmitNodeCollector collector,
                                             int light, AvatarRenderState state) {
        poseStack.pushPose();
        poseStack.mulPose(new Matrix4f().rotateY((float) Math.PI));
        poseStack.mulPose(new Matrix4f().rotateZ((float) Math.PI));
        poseStack.translate(0.0F, -0.25F, -0.30F);
        for (int i = 0; i < 6; i++) {
            float x = -0.105F + (i % 3) * 0.105F;
            float y = -0.11F + (i / 3) * 0.15F;
            drawHanging(contents.get(2 + i), poseStack, collector, light, state,
                    x, y, -0.13F, 0.12F, 0.0F);
        }
        poseStack.popPose();
    }

    private static void drawSatchel(ItemStackRenderState item, PoseStack poseStack,
                                    SubmitNodeCollector collector, int light,
                                    AvatarRenderState state) {
        if (item == null || item.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(0.245F, 0.585F, -0.17F);
        poseStack.mulPose(new Matrix4f().rotateY(0.48F));
        poseStack.scale(0.50F, 0.50F, 0.50F);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    private static void drawSatchelContents(List<ItemStackRenderState> contents,
                                             PoseStack poseStack, SubmitNodeCollector collector,
                                             int light, AvatarRenderState state) {
        poseStack.pushPose();
        poseStack.translate(0.245F, 0.585F, -0.17F);
        poseStack.mulPose(new Matrix4f().rotateY(0.48F));
        for (int i = 0; i < 9; i++) {
            float x = -0.075F + (i % 3) * 0.075F;
            float y = -0.075F + (i / 3) * 0.075F;
            draw(contents.get(8 + i), poseStack, collector, light, state,
                    x, y, -0.145F, 0.105F);
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

    private static void drawScaled(ItemStackRenderState item, PoseStack poseStack,
                                   SubmitNodeCollector collector, int light,
                                   AvatarRenderState state, float x, float y, float z,
                                   float sx, float sy, float sz) {
        if (item == null || item.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(sx, sy, sz);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }

    private static void drawHanging(ItemStackRenderState item, PoseStack poseStack,
                                    SubmitNodeCollector collector, int light,
                                    AvatarRenderState state, float x, float y, float z,
                                    float scale, float swing) {
        if (item == null || item.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(new Matrix4f().rotateZ((float) Math.PI + swing));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0F, -0.10F, 0.0F);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
