package org.ander507.packetspy.client;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.protocol.Packet;

/**
 * Sits in the client's Netty pipeline right before Minecraft's own packet handler.
 * Runs on the network thread, so it only hands packets off; serializing happens on PacketSpy's own thread.
 */
@Environment(EnvType.CLIENT)
public class PacketSpyHandler extends ChannelDuplexHandler {
    public static final String NAME = "packetspy";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Packet<?> packet) {
            capture("IN", packet);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Packet<?> packet) {
            capture("OUT", packet);
        }
        super.write(ctx, msg, promise);
    }

    private static void capture(String direction, Packet<?> packet) {
        // Never let PacketSpy break the connection: any exception here would disconnect the player.
        try {
            PacketspyClient.capture(direction, packet);
        } catch (Throwable t) {
            PacketspyClient.LOGGER.error("PacketSpy failed to capture a packet", t);
        }
    }
}
