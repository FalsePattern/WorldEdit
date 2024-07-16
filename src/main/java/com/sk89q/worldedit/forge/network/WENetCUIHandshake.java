package com.sk89q.worldedit.forge.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import lombok.val;

import static com.sk89q.worldedit.forge.network.WENetHandler.*;

@UtilityClass
final class WENetCUIHandshake {
    static void register(SimpleNetworkWrapper netWrapper, int c2sId, int s2cId) {
        netWrapper.registerMessage(new C2SHandler(), C2SMessage.class, c2sId, Side.SERVER);
        netWrapper.registerMessage(new S2CHandler(), S2CMessage.class, s2cId, Side.CLIENT);
    }

    static void sendCUIHandshakeC2S(int clientApiVersion) {
        WENetHandler.sendC2SRequest(new C2SMessage(clientApiVersion));
    }

    @NoArgsConstructor
    static class C2SHandler implements SafeMessageHandler.WithReply<C2SMessage, S2CMessage> {
        @Override
        public S2CMessage onMessage(MessageContext ctx, C2SMessage msg) {
            checks:
            {
                val session = sessionFromContext(ctx);
                if (session == null)
                    break checks;
                if (msg.clientApiVersion != WECUI_API_VERSION) {
                    session.setCUISupport(false);
                    session.setCUIVersion(-1);
                    break checks;
                }

                session.setCUISupport(true);
                session.setCUIVersion(msg.clientApiVersion);
                return new S2CMessage(true, WECUI_API_VERSION);
            }
            return new S2CMessage(false, WECUI_API_VERSION);
        }
    }

    @NoArgsConstructor
    static class S2CHandler implements SafeMessageHandler.NoReply<S2CMessage> {
        @Override
        public void onMessage(MessageContext ctx, S2CMessage msg) {
            CUI_HANDSHAKE_CALLBACK.accept(msg.isValid, msg.serverApiVersion);
        }
    }

    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class C2SMessage implements IMessage {
        int clientApiVersion = -1;

        @Override
        public void fromBytes(ByteBuf buf) {
            clientApiVersion = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(clientApiVersion);
        }
    }

    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class S2CMessage implements IMessage {
        boolean isValid = false;
        int serverApiVersion = -1;

        @Override
        public void fromBytes(ByteBuf buf) {
            isValid = buf.readBoolean();
            serverApiVersion = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeBoolean(isValid);
            buf.writeInt(WECUI_API_VERSION);
        }
    }
}
