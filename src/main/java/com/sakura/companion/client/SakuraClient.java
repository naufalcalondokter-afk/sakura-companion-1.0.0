package com.sakura.companion.client;

import com.sakura.companion.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class SakuraClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRenderers.register(ModEntities.SAKURA, SakuraRenderer::new);
        EntityRenderers.register(ModEntities.CHILD, SakuraChildRenderer::new);
    }
}
