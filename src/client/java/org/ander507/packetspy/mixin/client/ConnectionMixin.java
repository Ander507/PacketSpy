package org.ander507.packetspy.mixin.client;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import org.ander507.packetspy.client.PacketSpyHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method = "channelActive", at = @At("TAIL"))
    private void packetspy$onChannelActive(ChannelHandlerContext context, CallbackInfo ci) {
        // Connection is used on both sides. In singleplayer the built-in server has its own
        // (SERVERBOUND) connection to us; spying on it too would log every packet twice.
        if (((Connection) (Object) this).getReceiving() != PacketFlow.CLIENTBOUND) {
            return;
        }
        if (context.pipeline().get(PacketSpyHandler.NAME) == null) {
            context.pipeline().addBefore(context.name(), PacketSpyHandler.NAME, new PacketSpyHandler());
        }
    }
}
