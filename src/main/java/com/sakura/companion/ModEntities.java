package com.sakura.companion;

import com.sakura.companion.entity.SakuraChildEntity;
import com.sakura.companion.entity.SakuraEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final EntityType<SakuraEntity> SAKURA = register(
            "sakura",
            EntityType.Builder.of(SakuraEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(8));

    public static final EntityType<SakuraChildEntity> CHILD = register(
            "sakura_child",
            EntityType.Builder.of(SakuraChildEntity::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.9f)
                    .clientTrackingRange(8));

    private ModEntities() {}

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ModIds.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void init() {
        FabricDefaultAttributeRegistry.register(SAKURA, SakuraEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CHILD, SakuraChildEntity.createAttributes());
    }
}
