package com.sakura.companion.client;

import com.sakura.companion.ModIds;
import com.sakura.companion.entity.SakuraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SakuraRenderer extends GeoEntityRenderer<SakuraEntity, EntityRenderState> {
    public SakuraRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ModIds.id("sakura")));
    }
}
