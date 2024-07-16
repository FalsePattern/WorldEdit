package com.sk89q.worldedit.forge.network;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.forge.ForgeWorldEdit;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.sk89q.worldedit.forge.Tags.MOD_ID;
import static com.sk89q.worldedit.forge.Tags.MOD_NAME;

@UtilityClass
public final class WENetHandler {
    static final Logger LOG = LogManager.getLogger(MOD_NAME + "|Network");

    static final int WECUI_API_VERSION = 4;

    private static final int CUI_HANDSHAKE_C2S_ID = 0;
    private static final int CUI_HANDSHAKE_S2C_ID = 1;
    private static final int CUI_EVENT_C2S_ID = 2;
    private static final int CUI_EVENT_S2C_ID = 3;

    static boolean ALLOW_CUI;
    static boolean LOG_ERRORS;
    static boolean LOG_VERBOSE;

    static boolean CUI_HANDLER_SETUP = false;

    static String CUI_IMPL_NAME;
    static BiConsumer<Boolean, Integer> CUI_HANDSHAKE_CALLBACK;
    static Consumer<String> CUI_EVENT_CALLBACK;

    private static SimpleNetworkWrapper NET_WRAPPER;

    // region Init
    public static void init(LocalConfiguration cfg) {
        ALLOW_CUI = cfg.netAllowCUI;
        LOG_ERRORS = cfg.netLogErrors || cfg.netLogVerbose;
        LOG_VERBOSE = cfg.netLogVerbose;

        NET_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

        if (ALLOW_CUI) {
            WENetCUIHandshake.register(NET_WRAPPER, CUI_HANDSHAKE_C2S_ID, CUI_HANDSHAKE_S2C_ID);
            WENetCUIEvent.register(NET_WRAPPER, CUI_EVENT_C2S_ID, CUI_EVENT_S2C_ID);
        } else {
            LOG.debug("CUI is Disabled, skipping packet registration");
        }
    }

    public static void initCUIHandler(@NotNull String implName,
                                      @NotNull BiConsumer<Boolean, Integer> handshakeCallback,
                                      @NotNull Consumer<String> eventCallback) {
        if (CUI_HANDLER_SETUP) {
            LOG.warn("Potentially conflicting WECUI implementations, {} is setup but {} will be ignored.",
                     CUI_IMPL_NAME,
                     implName,
                     new Throwable());
            return;
        }

        try {
            nullCheck(implName, "Implementation name");
            nullCheck(handshakeCallback, "Handshake callback");
            nullCheck(eventCallback, "Event callback");

            if (implName.isEmpty())
                throw new IllegalArgumentException("Implementation name must not be empty");
        } catch (RuntimeException e) {
            LOG.error("Failed to setup WECUI handler:", e);
            return;
        }
        CUI_HANDLER_SETUP = true;
        CUI_HANDSHAKE_CALLBACK = handshakeCallback;
        CUI_EVENT_CALLBACK = eventCallback;

        LOG.info("Setup CUI Handler for: {}", implName);
    }
    // endregion

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

    public static void requestCUIUpdate() {
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

    public static void sendCUIEvent(EntityPlayerMP player, String evt) {
        if (ALLOW_CUI)
            WENetCUIEvent.sendCUIUpdateS2C(player, evt);
    }

    // region Raw Requests
    static void sendC2SRequest(IMessage msg) {
        NET_WRAPPER.sendToServer(msg);
        if (LOG_VERBOSE)
            LOG.debug("Sent request: [{}] to server", msg);
    }

    static void sendS2CRequest(EntityPlayerMP player, IMessage msg) {
        NET_WRAPPER.sendTo(msg, player);
        if (LOG_VERBOSE)
            LOG.debug("Sent request: [{}] to player: [{}]", msg, safePlayerName(player));
    }
    // endregion

    // region Logging
    static void logRequestReceived(MessageContext ctx, IMessage msg) {
        if (!LOG_VERBOSE)
            return;

        if (ctx.side.isServer()) {
            LOG.debug("Received request [{}] from player [{}]", msg, nameFromContext(ctx));
        } else {
            LOG.debug("Received request [{}] from server", msg);
        }
    }

    static void logRequestProcessed(MessageContext ctx, IMessage msg) {
        if (!LOG_VERBOSE)
            return;

        if (ctx.side.isServer()) {
            LOG.debug("Processed request: [{}] from player: [{}]", msg, nameFromContext(ctx));
        } else {
            LOG.debug("Processed request: [{}] from server", msg);
        }
    }

    static void logReplySent(MessageContext ctx, IMessage msg) {
        if (!LOG_VERBOSE)
            return;

        if (!ctx.side.isClient()) {
            LOG.debug("Sent reply: [{}] to player: [{}]", msg, nameFromContext(ctx));
        } else {
            LOG.debug("Sent reply: [{}] to server", msg);
        }
    }

    static void logMessageError(MessageContext ctx, IMessage msg, Exception e) {
        if (!LOG_ERRORS)
            return;

        if (ctx.side.isServer()) {
            LOG.error("Failed to process message {} from player {}", msg, nameFromContext(ctx));
        } else {
            LOG.error("Failed to process message {} from server", msg);
        }
        LOG.error("Trace: ", e);
    }
    // endregion

    // region Util
    static String nameFromContext(MessageContext ctx) {
        EntityPlayer player = null;
        try {
            // Only called from server-side code anyway.
            if (ctx.side.isServer())
                player = ctx.getServerHandler().playerEntity;
        } catch (Exception ignored) {
        }
        return safePlayerName(player);
    }

    static String safePlayerName(EntityPlayer player) {
        try {
            checks:
            {
                if (player == null)
                    break checks;
                val name = player.getCommandSenderName();
                if (name == null)
                    break checks;
                if (!name.isEmpty())
                    return name;
            }
        } catch (Exception ignored) {
        }
        return "!UNKNOWN!";
    }

    static @Nullable LocalSession sessionFromContext(MessageContext ctx) {
        try {
            return ForgeWorldEdit.inst.getSession(ctx.getServerHandler().playerEntity);
        } catch (Exception e) {
            if (LOG_ERRORS) {
                LOG.error("Failed to get session for player: [{}]", nameFromContext(ctx));
                LOG.error("Trace: ", e);
            }
            return null;
        }
    }

    static @Nullable Actor actorFromContext(MessageContext ctx) {
        try {
            return ForgeWorldEdit.inst.wrap(ctx.getServerHandler().playerEntity);
        } catch (Exception e) {
            if (LOG_ERRORS) {
                LOG.error("Failed to get actor for player: [{}]", nameFromContext(ctx));
                LOG.error("Trace: ", e);
            }
            return null;
        }
    }

    private static void nullCheck(Object val, String name) {
        Objects.requireNonNull(val, name + " must not be null");
    }
    // endregion
}
