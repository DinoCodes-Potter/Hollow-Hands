package dev.hollowhands.mixin;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import dev.hollowhands.HollowHandsData;
import dev.hollowhands.HollowHandsItems;
import dev.hollowhands.HollowHandsRenderData;
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
 @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",at=@At("TAIL"))
 private void hollowhands$extractGear(Avatar avatar,AvatarRenderState state,float partialTick,CallbackInfo ci){
  if(!(avatar instanceof Player player)){state.setData(HollowHandsRenderData.GEAR,List.of());state.setData(HollowHandsRenderData.CONTENTS,List.of());return;}
  List<ItemStackRenderState> gear=List.of(resolve(HollowHandsItems.BELT.getDefaultInstance(),avatar),resolve(HollowHandsItems.BACK.getDefaultInstance(),avatar),resolve(HollowHandsItems.SATCHEL.getDefaultInstance(),avatar),resolve(HollowHandsItems.BANDOLIER.getDefaultInstance(),avatar));
  List<ItemStackRenderState> contents=new ArrayList<>(HollowHandsData.TOTAL_SLOTS);
  for(ItemStack stack:HollowHandsData.get(player)) contents.add(stack.isEmpty()?new ItemStackRenderState():resolve(stack,avatar));
  state.setData(HollowHandsRenderData.GEAR,gear);
  state.setData(HollowHandsRenderData.CONTENTS,List.copyOf(contents));
 }
 private static ItemStackRenderState resolve(ItemStack stack,Avatar avatar){
  ItemStackRenderState renderState=new ItemStackRenderState();
  Minecraft.getInstance().getItemModelResolver().updateForNonLiving(renderState,stack,ItemDisplayContext.FIXED,avatar);
  return renderState;
 }
}