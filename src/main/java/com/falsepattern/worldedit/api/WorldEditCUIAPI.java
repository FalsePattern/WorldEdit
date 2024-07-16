package com.falsepattern.worldedit.api;

import com.sk89q.worldedit.forge.network.WENetHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Public API Class provided for external implementations of the WorldEditCUI.
 * <p>
 * The current expected API version is {@code 4}.
 */
@SideOnly(Side.CLIENT)
public final class WorldEditCUIAPI {
    private WorldEditCUIAPI() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void initCUIHandler(@NotNull String implName,
                                      @NotNull BiConsumer<Boolean, Integer> handshakeCallback,
                                      @NotNull Consumer<String> eventCallback) {
        WENetHandler.initCUIHandler(implName, handshakeCallback, eventCallback);
    }

    public static void requestCUIHandshake(int clientAPIVersion) {
        WENetHandler.requestCUIHandshake(clientAPIVersion);
    }

    public static void requestCUIUpdate() {
        WENetHandler.requestCUIUpdate();
    }
}
