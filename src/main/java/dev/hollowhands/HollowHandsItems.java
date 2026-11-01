package dev.hollowhands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
public final class HollowHandsItems {
 public static final Item BELT=register("gear_belt");
 public static final Item BACK=register("gear_backpack");
 public static final Item SATCHEL=register("gear_satchel");
 public static final Item BANDOLIER=register("gear_bandolier");
 private HollowHandsItems(){}
 private static Item register(String path){
  Identifier id=Identifier.fromNamespaceAndPath("hollowhands",path);
  ResourceKey<Item> key=ResourceKey.create(Registries.ITEM,id);
  Item item=new Item(new Item.Properties().setId(key));
  return Registry.register(BuiltInRegistries.ITEM,key,item);
 }
 public static void register(){}
}