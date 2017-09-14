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

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Connection.ConnectionSide;
import nz.co.lolnet.equity.entries.Packet.PacketDirection;
import nz.co.lolnet.equity.entries.ProxyMessage;
import nz.co.lolnet.equity.util.EquityUtil;

public class ProxyServerHandler extends ChannelInboundHandlerAdapter {
	
	private final ConnectionSide connectionSide;
	
	public ProxyServerHandler() {
		this.connectionSide = ConnectionSide.SERVER;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		ctx.read();
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Connection connection = ctx.channel().attr(EquityUtil.getAttributeKey()).get();
		if (connection == null || connection.getConnectionState() == null || !connection.isActive()) {
			throw new IllegalStateException(getConnectionSide().toString() + " Connection error!");
		}
		
		if (msg instanceof ProxyMessage) {
			ProxyMessage proxyMessage = (ProxyMessage) msg;
			Equity.getInstance().getPacketManager().processProxyMessage(proxyMessage, PacketDirection.CLIENTBOUND);
			Channel channel = connection.getChannel(getConnectionSide().getChannelSide());
			if (channel == null) {
				throw new IllegalStateException(getConnectionSide().getChannelSide() + " Channel does not exist!");
			}
			
			channel.writeAndFlush(proxyMessage).addListener(EquityUtil.getFutureListener(ctx.channel()));
			return;
		}
		
		throw new UnsupportedOperationException("Unsupported message received!");
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		Connection connection = ctx.channel().attr(EquityUtil.getAttributeKey()).get();
		Equity.getInstance().getConnectionManager().removeConnection(connection);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		Equity.getInstance().getLogger().error("Exception caught in {}", getClass().getSimpleName(), throwable);
	}
	
	public ConnectionSide getConnectionSide() {
		return connectionSide;
	}
}