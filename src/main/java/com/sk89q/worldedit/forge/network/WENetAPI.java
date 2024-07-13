package com.sk89q.worldedit.forge.network;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.sk89q.worldedit.forge.network.WENetWrapper.ALLOW_CUI;
import static com.sk89q.worldedit.forge.network.WENetWrapper.LOG;

@UtilityClass
public final class WENetAPI {
    public static final int WECUI_API_VERSION = 4;

    static boolean CUI_HANDLER_SETUP = false;

    static String CUI_IMPL_NAME;
    static BiConsumer<Boolean, Integer> CUI_HANDSHAKE_CALLBACK;
    static Consumer<String> CUI_STATE_CALLBACK;

    public static void setupCUIHandler(@NotNull String implName,
                                       @NotNull BiConsumer<Boolean, Integer> handshakeCallback,
                                       @NotNull Consumer<String> stateCallback) {
        if (!ALLOW_CUI)
            return;

        if (CUI_HANDLER_SETUP) {
            LOG.warn("Potentially conflicting WECUI implementations, {} is setup but {} will be ignored.",
                     CUI_IMPL_NAME,
                     implName);
            LOG.warn("Trace:", new Throwable());
            return;
        }

        try {
           nullCheck(implName, "Implementation name");
           nullCheck(handshakeCallback, "Handshake callback");
           nullCheck(stateCallback, "State callback");

            if (implName.isEmpty())
                throw new IllegalArgumentException("Implementation name must not be empty");
        } catch (RuntimeException e) {
            LOG.error("Failed to setup WECUI handler:", e);
            return;
        }
        CUI_HANDLER_SETUP = true;
        CUI_HANDSHAKE_CALLBACK = handshakeCallback;
        CUI_STATE_CALLBACK = stateCallback;

        LOG.info("Setup CUI Handler for: {}", implName);
        LOG.trace("Trace: ", new Throwable());
    }

    public static void sendCUIHandshake(int clientAPIVersion) {
        if (!ALLOW_CUI)
            return;

        if (!CUI_HANDLER_SETUP) {
            LOG.warn("Handshake requested with no WECUI handler set", new Throwable());
            return;
        }
        WENetCUIHandshake.execute(clientAPIVersion);
    }

    private static void nullCheck(Object val, String name) {
        Objects.requireNonNull(val, name + " must not be null");
    }
}
