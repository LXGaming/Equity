/*
 * Copyright 2017 lolnet.co.nz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.co.lolnet.equity.handlers;

import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Connection.ConnectionSide;
import nz.co.lolnet.equity.entries.Packet;
import nz.co.lolnet.equity.entries.Packet.PacketDirection;
import nz.co.lolnet.equity.util.EquityUtil;
import nz.co.lolnet.equity.util.LogHelper;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProxyServerHandler extends ChannelInboundHandlerAdapter {
	
	private final ConnectionSide connectionSide;
	
	public ProxyServerHandler() {
		this.connectionSide = ConnectionSide.SERVER;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.read();
		ctx.write(Unpooled.EMPTY_BUFFER);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg == null || !(msg instanceof Packet)) {
			throw new IllegalArgumentException("Illegal message received!");
		}
		
		Packet packet = (Packet) msg;
		Connection connection = Equity.getInstance().getConnectionManager().getConnection(ctx.channel(), getConnectionSide());
		if (connection == null || connection.getConnectionState() == null) {
			throw new IllegalStateException(getConnectionSide() + " Connection error!");
		}
		
		Equity.getInstance().getPacketManager().process(connection, packet, PacketDirection.CLIENTBOUND);
		
		Channel channel = connection.getChannel(getConnectionSide().getChannelSide());
		if (channel == null) {
			throw new IllegalStateException(getConnectionSide().getChannelSide() + " Channel does not exist!");
		}
		
		channel.writeAndFlush(packet.getByteBuf()).addListener(EquityUtil.getFutureListener(ctx.channel()));
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		Connection connection = Equity.getInstance().getConnectionManager().getConnection(ctx.channel(), getConnectionSide());
		if (connection == null) {
			return;
		}
		
		Channel channel = connection.getChannel(getConnectionSide().getChannelSide());
		if (channel != null && channel.isActive()) {
			channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		if (ctx.channel() != null && ctx.channel().isActive()) {
			ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
		
		LogHelper.error("Exception caught in '" + getClass().getSimpleName() + "' - " + throwable.getMessage());
	}
	
	public ConnectionSide getConnectionSide() {
		return connectionSide;
	}
}