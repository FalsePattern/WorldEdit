package com.sk89q.worldedit.forge.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import lombok.val;

import static com.sk89q.worldedit.forge.network.WENetWrapper.*;

interface SafeMessageHandler {
    @FunctionalInterface
    interface WithReply<IN extends IMessage, OUT extends IMessage> extends IMessageHandler<IN, OUT> {
        OUT onMessage(MessageContext ctx, IN msg);

        @Override
        default OUT onMessage(IN msg, MessageContext ctx) {
            try {
                logRequestReceived(ctx, msg);
                val out = onMessage(ctx, msg);
                logRequestProcessed(ctx, msg);
                logReplySent(ctx, msg);
                return out;
            } catch (Exception e) {
                logMessageError(ctx, msg, e);
                return null;
            }
        }
    }

    @FunctionalInterface
    interface NoReply<IN extends IMessage> extends IMessageHandler<IN, IMessage> {
        void onMessage(MessageContext ctx, IN msg);

        @Override
        default IMessage onMessage(IN msg, MessageContext ctx) {
            try {
                logRequestReceived(ctx, msg);
                onMessage(ctx, msg);
                logRequestProcessed(ctx, msg);
            } catch (Exception e) {
                logMessageError(ctx, msg, e);
            }
            return null;
        }
    }
}
