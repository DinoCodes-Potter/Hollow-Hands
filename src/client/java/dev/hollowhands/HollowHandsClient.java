package dev.hollowhands;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
public final class HollowHandsClient implements ClientModInitializer{
 private static final KeyMapping.Category CATEGORY=KeyMapping.Category.register(Identifier.fromNamespaceAndPath("hollowhands","main"));
 private static final KeyMapping OPEN_KEY=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.hollowhands.open",InputConstants.Type.KEYBOARD,InputConstants.KEY_G,CATEGORY));
 @Override public void onInitializeClient(){
  LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType,entityRenderer,helper,context)->{
   if(entityType==EntityTypes.PLAYER&&entityRenderer instanceof AvatarRenderer<?> avatarRenderer)helper.register(new HollowHandsRenderLayer(avatarRenderer));
  });
  ClientTickEvents.END_CLIENT_TICK.register(client->{while(OPEN_KEY.consumeClick()){if(client.player!=null&&client.gui.screen()==null)client.gui.setScreen(new HollowHandsScreen());}});
 }
}