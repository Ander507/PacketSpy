package org.ander507.packetspy.client.compat;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Minecraft 26.x: unobfuscated, Fabric API uses Mojang's names. */
public final class Compat {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("packetspy", "general"));

    private Compat() {
    }

    public static KeyMapping registerKey(String name, int key) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(name, key, CATEGORY));
    }

    public static void showOverlayMessage(Minecraft client, Component message) {
        if (client.player != null) {
            client.player.sendOverlayMessage(message);
        }
    }
}
