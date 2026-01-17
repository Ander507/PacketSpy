package org.ander507.packetspy.mixin.client;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import org.ander507.packetspy.client.PacketSpyHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "channelActive", at = @At("TAIL"))
    private void onChannelActive(ChannelHandlerContext context, CallbackInfo ci) {
        if (context.pipeline().get("packet_spy") == null) {
            context.pipeline().addBefore(context.name(), "packet_spy", new PacketSpyHandler());
        }
    }
}

