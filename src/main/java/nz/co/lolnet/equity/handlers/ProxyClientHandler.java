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

import java.net.InetSocketAddress;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Connection.ConnectionSide;
import nz.co.lolnet.equity.entries.Packet.PacketDirection;
import nz.co.lolnet.equity.entries.ProxyMessage;
import nz.co.lolnet.equity.util.EquityUtil;
import nz.co.lolnet.equity.util.LogHelper;

public class ProxyClientHandler extends ChannelInboundHandlerAdapter {
	
	private final ConnectionSide connectionSide;
	
	public ProxyClientHandler() {
		this.connectionSide = ConnectionSide.CLIENT;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		Connection connection = new Connection();
		connection.setClientChannel(ctx.channel());
		Equity.getInstance().getConnectionManager().addConnection(connection);
		ctx.read();
		ctx.write(Unpooled.EMPTY_BUFFER);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Connection connection = ctx.channel().attr(EquityUtil.getAttributeKey()).get();
		if (connection == null || connection.getConnectionState() == null) {
			throw new IllegalStateException(getConnectionSide().toString() + " Connection error!");
		}
		
		if (msg instanceof HAProxyMessage && Equity.getInstance().getConfig().isProxyProtocol()) {
			HAProxyMessage haProxyMessage = (HAProxyMessage) msg;
			Equity.getInstance().getConnectionManager().setSocketAddress(connection, new InetSocketAddress(haProxyMessage.sourceAddress(), haProxyMessage.sourcePort()));
			return;
		}
		
		if (msg instanceof ProxyMessage) {
			ProxyMessage proxyMessage = (ProxyMessage) msg;
			Equity.getInstance().getPacketManager().process(proxyMessage, PacketDirection.SERVERBOUND);
			Channel channel = connection.getChannel(getConnectionSide().getChannelSide());
			if (channel == null) {
				connection.getPacketQueue().add(proxyMessage);
				return;
			}
			
			channel.writeAndFlush(new ProxyMessage(proxyMessage.getPacket())).addListener(EquityUtil.getFutureListener(ctx.channel()));
			return;
		}
		
		throw new UnsupportedOperationException("Unsupported message received!");
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		Connection connection = ctx.channel().attr(EquityUtil.getAttributeKey()).get();
		if (Equity.getInstance() != null && Equity.getInstance().getConnectionManager() != null && connection != null) {
			Equity.getInstance().getConnectionManager().removeConnection(connection);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		LogHelper.error("Exception caught in '" + getClass().getSimpleName() + "' - " + throwable.getMessage());
		if (Equity.getInstance() != null && Equity.getInstance().getConfig() != null && Equity.getInstance().getConfig().isDebug()) {
			throwable.printStackTrace();
		}
	}
	
	public ConnectionSide getConnectionSide() {
		return connectionSide;
	}
}