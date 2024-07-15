package com.sk89q.worldedit.forge.network;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.forge.Tags;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.sk89q.worldedit.forge.Tags.MOD_ID;

@UtilityClass
public final class WENetWrapper {
    static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME + "|Networking");

    private static final int CUI_HANDSHAKE_C2S_ID = 0;
    private static final int CUI_HANDSHAKE_S2C_ID = 1;
    private static final int CUI_EVENT_S2C_ID     = 2;

    static boolean ALLOW_CUI;
    static boolean LOG_ERRORS;
    static boolean LOG_VERBOSE;

    private static SimpleNetworkWrapper NET_WRAPPER;

    public static void init(LocalConfiguration cfg) {
        ALLOW_CUI = cfg.netAllowCUI;
        LOG_ERRORS = cfg.netLogErrors || cfg.netLogVerbose;
        LOG_VERBOSE = cfg.netLogVerbose;

        NET_WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

        if (ALLOW_CUI) {
            WENetCUIHandshake.register(NET_WRAPPER, CUI_HANDSHAKE_C2S_ID, CUI_HANDSHAKE_S2C_ID);
            WENetCUIEvent.register(NET_WRAPPER, CUI_EVENT_S2C_ID);
        }
    }


    public static void sendCUIEvent(EntityPlayerMP player, String evt) {
        if (ALLOW_CUI)
            WENetCUIEvent.execute(player, evt);
    }

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

    static String nameFromContext(MessageContext ctx) {
        EntityPlayer player = null;
        try {
            if (ctx.side.isClient()) {
                player = Minecraft.getMinecraft().thePlayer;
            } else {
                player = ctx.getServerHandler().playerEntity;
            }
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
        } catch (Exception e) {
        }
        return "!UNKNOWN!";
    }
}
