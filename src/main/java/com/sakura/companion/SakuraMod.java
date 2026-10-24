package com.sakura.companion;

import net.fabricmc.api.ModInitializer;

public class SakuraMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ModEntities.init();
        ModItems.init();
    }
}
