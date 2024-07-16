package com.falsepattern.worldedit.api;

import com.falsepattern.lib.StableAPI;
import com.sk89q.worldedit.forge.network.WENetHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Public API Class provided for external implementations of the WorldEditCUI.
 * <p>
 * The current expected API version is {@code 4}. You may hardcode this value in your **OWN** implementation.
 * <p>
 * Your code must call {@link WorldEditCUIAPI#initCUIHandler} to setup the appropriate callbacks during your mods pre-init stage.
 * <p>
 * If multiple implementations attempt to register a handler, the first one will be used.
 * <p>
 * On a world join or any subsequent world change, you should call {@link WorldEditCUIAPI#requestCUIHandshake} to request a handshake.
 * <p>
 * If this handshake succeeds, the client will now receive updates over the event callback on any CUI updates.
 * <p>
 * Note that on a session change (Generally triggered on a client world change), you may need to request a new handshake.
 * <p>
 * It is also possible to use {@link WorldEditCUIAPI#requestCUIUpdate} to request a full update of the current CUI state.
 */
//TODO: Include @StableAPI markers
@SideOnly(Side.CLIENT)
public final class WorldEditCUIAPI {
    private WorldEditCUIAPI() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Used to setup a CUI Handler.
     * <p>
     * If this is called with invalid arguments, or called more than once, a warning is logged and the init will fail with no exception.
     * <p>
     * The callbacks will receive events from the server and will be executed on the client thread.
     * <p>
     * The handshake callback will receive {@code boolean isValid} and {@code serverAPIVersion} as arguments.
     * <p>
     * The event callback will receive {@code String event}, with an event formatted using the standard WorldEditCUI formatting as its argument.
     *
     * @param implName          Name of the CUI Implementation
     * @param handshakeCallback Callback for the CUI Handshake from the server
     * @param eventCallback     Callback for CUI Events from the server
     */
    //TODO: Include @StableAPI markers
    public static void initCUIHandler(@NotNull String implName,
                                      @NotNull BiConsumer<Boolean, Integer> handshakeCallback,
                                      @NotNull Consumer<String> eventCallback) {
        WENetHandler.initCUIHandler(implName, handshakeCallback, eventCallback);
    }

    /**
     * Requests a CUI handshake from the server.
     * <p>
     * On success, the callback provided in {@link WorldEditCUIAPI#initCUIHandler} will receive {@code true} as it's first argument, or {@code false} on failure.
     * <p>
     * The server side CUI API version is always provided on reply.
     *
     * @param clientAPIVersion The Client API version of this implementation
     */
    //TODO: Include @StableAPI markers
    public static void requestCUIHandshake(int clientAPIVersion) {
        WENetHandler.requestCUIHandshake(clientAPIVersion);
    }

    /**
     * Requests an update of the entire CUI state from the server.
     * <p>
     * On success, the callback provided in {@link WorldEditCUIAPI#initCUIHandler} will be sent the full state of the CUI.
     * <p>
     * Otherwise, no reply will be sent back to the client.
     */
    //TODO: Include @StableAPI markers
    public static void requestCUIUpdate() {
        WENetHandler.requestCUIUpdate();
    }
}
