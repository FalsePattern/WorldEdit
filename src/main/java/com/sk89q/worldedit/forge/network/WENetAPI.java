package com.sk89q.worldedit.forge.network;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.sk89q.worldedit.forge.network.WENetWrapper.*;

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

    public static void requestCUIHandshake(int clientAPIVersion) {
        if (!ALLOW_CUI)
            return;

        if (!CUI_HANDLER_SETUP) {
            LOG.warn("Requested CUI Handshake with no WECUI handler set", new Throwable());
            return;
        }

        if (LOG_VERBOSE)
            LOG.debug("Requested CUI Handshake with version: [{}]", clientAPIVersion);
        WENetCUIHandshake.sendCUIHandshakeC2S(clientAPIVersion);
    }

    public static void requestCUISetup() {
        if (!ALLOW_CUI)
            return;

        if (!CUI_HANDLER_SETUP) {
            LOG.warn("Requested CUI Update with no WECUI handler set", new Throwable());
            return;
        }

        if (LOG_VERBOSE)
            LOG.debug("Requested CUI Update");
        WENetCUIEvent.requestCUIUpdateC2S();
    }

    private static void nullCheck(Object val, String name) {
        Objects.requireNonNull(val, name + " must not be null");
    }
}
