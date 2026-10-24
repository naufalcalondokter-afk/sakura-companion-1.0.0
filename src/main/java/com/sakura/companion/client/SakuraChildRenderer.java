package com.sakura.companion.client;

import com.sakura.companion.ModIds;
import com.sakura.companion.entity.SakuraChildEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Memakai model dan animasi Sakura, tapi diperkecil. */
public class SakuraChildRenderer extends GeoEntityRenderer<SakuraChildEntity, EntityRenderState> {
    public SakuraChildRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ModIds.id("sakura")));
        this.withScale(0.5f);
    }
}
