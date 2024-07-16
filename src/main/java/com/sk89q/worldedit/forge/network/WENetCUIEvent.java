package com.sk89q.worldedit.forge.network;

import cpw.mods.fml.common.network.ByteBufUtils;
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
import net.minecraft.entity.player.EntityPlayerMP;

import static com.sk89q.worldedit.forge.network.WENetHandler.*;

@UtilityClass
final class WENetCUIEvent {
    static void register(SimpleNetworkWrapper netWrapper, int c2sId, int s2cId) {
        netWrapper.registerMessage(new C2SHandler(), C2SMessage.class, c2sId, Side.SERVER);
        netWrapper.registerMessage(new S2CHandler(), S2CMessage.class, s2cId, Side.CLIENT);
    }

    static void requestCUIUpdateC2S() {
        WENetHandler.sendC2SRequest(new WENetCUIEvent.C2SMessage());
    }

    static void sendCUIUpdateS2C(EntityPlayerMP player, String evt) {
        WENetHandler.sendS2CRequest(player, new WENetCUIEvent.S2CMessage(evt));
    }

    @NoArgsConstructor
    static class C2SHandler implements SafeMessageHandler.NoReply<C2SMessage> {
        @Override
        public void onMessage(MessageContext ctx, C2SMessage msg) {
            val playerName = nameFromContext(ctx);

            val session = sessionFromContext(ctx);
            if (session == null) {
                if (LOG_ERRORS)
                    LOG.error("Cannot update CUI for player: [{}] with no session", playerName);
                return;
            }
            if (!session.hasCUISupport()) {
                if (LOG_ERRORS)
                    LOG.error("Cannot update CUI for player: [{}] with no CUI support", playerName);
                return;
            }
            val actor = actorFromContext(ctx);
            if (actor == null) {
                if (LOG_ERRORS)
                    LOG.error("Cannot update CUI for player: [{}] with no actor", playerName);
                return;
            }

            session.dispatchCUISetup(actor);
            if (LOG_VERBOSE)
                LOG.debug("Updated CUI for player: [{}]", playerName);
        }
    }

    @NoArgsConstructor
    static class S2CHandler implements SafeMessageHandler.NoReply<S2CMessage> {
        @Override
        public void onMessage(MessageContext ctx, S2CMessage msg) {
            CUI_EVENT_CALLBACK.accept(msg.evt);
        }
    }

    @ToString
    @NoArgsConstructor
//    @AllArgsConstructor
    public static class C2SMessage implements IMessage {
        @Override
        public void fromBytes(ByteBuf buf) {
            // No-Op
        }

        @Override
        public void toBytes(ByteBuf buf) {
            // No-Op
        }
    }

    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class S2CMessage implements IMessage {
        String evt = "";

        @Override
        public void fromBytes(ByteBuf buf) {
            evt = ByteBufUtils.readUTF8String(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            ByteBufUtils.writeUTF8String(buf, evt);
        }
    }
}
