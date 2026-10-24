package com.sakura.companion;

import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
    public static final Item RING = register("cincin", Item::new, new Item.Properties().stacksTo(16));

    public static final Item SAKURA_EGG = register(
            "sakura_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.SAKURA));

    public static final Item BUILD_HOSPITAL = register("tongkat_rumah_sakit",
            props -> new BuilderItem(props, BuilderItem.Kind.HOSPITAL), new Item.Properties().stacksTo(1));
    public static final Item BUILD_TOWN = register("tongkat_kota",
            props -> new BuilderItem(props, BuilderItem.Kind.TOWN), new Item.Properties().stacksTo(1));

    private ModItems() {}

    private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ModIds.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(e -> e.accept(SAKURA_EGG));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(e -> {
            e.accept(RING);
            e.accept(BUILD_HOSPITAL);
            e.accept(BUILD_TOWN);
        });
    }
}
