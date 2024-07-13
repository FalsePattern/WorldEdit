package com.sk89q.worldedit.forge.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.EntityPlayerMP;

import static com.sk89q.worldedit.forge.network.WENetAPI.*;


@UtilityClass
final class WENetCUIEvent {
    static void register(SimpleNetworkWrapper netWrapper, int s2cId) {
        netWrapper.registerMessage(new S2CHandler(), S2CMessage.class, s2cId, Side.CLIENT);
    }

    static void execute(EntityPlayerMP player, String evt) {
        WENetWrapper.sendS2CRequest(player, new WENetCUIEvent.S2CMessage(evt));
    }

    @NoArgsConstructor
    static class S2CHandler implements SafeMessageHandler.NoReply<S2CMessage> {
        @Override
        public void onMessage(MessageContext ctx, S2CMessage msg) {
            CUI_STATE_CALLBACK.accept(msg.evt);
        }
    }

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
