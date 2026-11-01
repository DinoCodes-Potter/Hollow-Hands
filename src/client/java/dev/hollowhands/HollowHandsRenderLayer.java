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
public final class HollowHandsRenderLayer extends RenderLayer<AvatarRenderState,PlayerModel>{
 public HollowHandsRenderLayer(RenderLayerParent<AvatarRenderState,PlayerModel> parent){super(parent);}
 @Override public void submit(PoseStack poseStack,SubmitNodeCollector collector,int light,AvatarRenderState state,float limbAngle,float limbDistance){
  List<ItemStackRenderState> gear=state.getData(HollowHandsRenderData.GEAR);
  List<ItemStackRenderState> contents=state.getData(HollowHandsRenderData.CONTENTS);
  if(gear==null||gear.size()!=4)return;
  draw(gear.get(0),poseStack,collector,light,state,-.50F,.91F,-.29F,.62F,0);
  draw(gear.get(1),poseStack,collector,light,state,-.50F,1.10F,.20F,.82F,180);
  draw(gear.get(2),poseStack,collector,light,state,-.27F,.93F,-.20F,.55F,-25);
  draw(gear.get(3),poseStack,collector,light,state,-.50F,1.10F,-.30F,.60F,0);
  if(contents==null||contents.size()!=HollowHandsData.TOTAL_SLOTS)return;
  for(int i=0;i<2;i++)draw(contents.get(i),poseStack,collector,light,state,-.58F+i*.16F,.93F,-.39F,.20F,0);
  for(int i=0;i<6;i++)draw(contents.get(2+i),poseStack,collector,light,state,-.62F+(i%3)*.12F,1.10F-(i/3)*.15F,.34F,.18F,180);
  for(int i=0;i<9;i++)draw(contents.get(8+i),poseStack,collector,light,state,-.13F+(i%3)*.07F,.98F-(i/3)*.12F,-.31F,.13F,-25);
  for(int i=0;i<4;i++)draw(contents.get(17+i),poseStack,collector,light,state,-.64F+i*.12F,1.43F-i*.15F,-.34F,.18F,0);
 }
 private static void draw(ItemStackRenderState item,PoseStack poseStack,SubmitNodeCollector collector,int light,AvatarRenderState state,float x,float y,float z,float scale,float yaw){
  if(item==null||item.isEmpty())return;
  poseStack.pushPose();poseStack.translate(x,-state.boundingBoxHeight+y,z);
  poseStack.scale(scale,scale,scale);
  item.submit(poseStack,collector,light,OverlayTexture.NO_OVERLAY,state.outlineColor);
  poseStack.popPose();
 }
}