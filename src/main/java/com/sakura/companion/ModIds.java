package com.sakura.companion;

import net.minecraft.resources.Identifier;

/**
 * Semua pembuatan ID lewat sini. Kalau Mojang mengganti nama kelas ID lagi
 * (ResourceLocation / Identifier), cukup ubah file ini.
 */
public final class ModIds {
    public static final String MOD_ID = "sakura_companion";

    private ModIds() {}

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
