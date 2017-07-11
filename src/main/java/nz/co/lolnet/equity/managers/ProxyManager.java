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

package nz.co.lolnet.equity.managers;

import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Connection.ConnectionSide;
import nz.co.lolnet.equity.entries.Packet;
import nz.co.lolnet.equity.entries.Server;
import nz.co.lolnet.equity.handlers.ProxyChannelHandler;
import nz.co.lolnet.equity.util.EquityUtil;
import nz.co.lolnet.equity.util.LogHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ProxyManager {
	
	private ServerBootstrap serverBootstrap;
	
	public void startProxy() {
		try {
			EventLoopGroup eventLoopGroup;
			Class<? extends ServerSocketChannel> eventLoopGroupClass;
			if (Epoll.isAvailable() && Equity.getInstance().getConfig().isNativeTransport()) {
				eventLoopGroup = new EpollEventLoopGroup(Equity.getInstance().getConfig().getMaxConnections(), EquityUtil.getThreadFactory("Netty Epoll Thread #%d"));
				eventLoopGroupClass = EpollServerSocketChannel.class;
				LogHelper.info("Using Epoll Transport.");
			} else {
				eventLoopGroup = new NioEventLoopGroup(Equity.getInstance().getConfig().getMaxConnections(), EquityUtil.getThreadFactory("Netty IO Thread #%d"));
				eventLoopGroupClass = NioServerSocketChannel.class;
				LogHelper.info("Using NIO Transport.");
			}
			
			serverBootstrap = new ServerBootstrap()
					.group(eventLoopGroup)
					.channel(eventLoopGroupClass)
					.option(ChannelOption.SO_BACKLOG, Equity.getInstance().getConfig().getSocketBacklog())
					.option(ChannelOption.SO_REUSEADDR, true)
					.childOption(ChannelOption.AUTO_READ, false)
					.childOption(ChannelOption.TCP_NODELAY, true)
					.childHandler(new ProxyChannelHandler(ConnectionSide.CLIENT));
			ChannelFuture channelFuture = getServerBootstrap().bind(Equity.getInstance().getConfig().getPort()).sync();
			
			LogHelper.info("Proxy listening on " + EquityUtil.getAddress(channelFuture.channel().localAddress()));
			
			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException | RuntimeException ex) {
			LogHelper.error("Encountered an error processing 'loadProxy' in '" + getClass().getSimpleName() + "' - " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	public void createServerConnection(Connection connection) {
		try {
			if (connection == null || connection.getClientChannel() == null || connection.getServerChannel() != null) {
				throw new IllegalArgumentException("Required arguments are invalid!");
			}
			
			Server server = Equity.getInstance().getServerManager().getServer(connection.getProtocolVersion());
			if (server == null) {
				LogHelper.warn("Failed to find server handling protocol " + connection.getProtocolVersion());
				Equity.getInstance().getConnectionManager().removeConnection(connection);
				return;
			}
			
			Bootstrap bootstrap = new Bootstrap()
					.group(connection.getClientChannel().eventLoop())
					.channel(connection.getClientChannel().getClass())
					.option(ChannelOption.AUTO_READ, false)
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Equity.getInstance().getConfig().getConnectTimeout())
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ProxyChannelHandler(ConnectionSide.SERVER));
			
			ChannelFuture channelFuture = bootstrap.connect(server.getHost(), server.getPort());
			channelFuture.addListener(future -> {
				if (future.isSuccess()) {
					connection.setServerChannel(channelFuture.channel());
					for (Packet packet : connection.getPacketQueue()) {
						connection.getServerChannel().writeAndFlush(packet.getByteBuf()).addListener(EquityUtil.getFutureListener(channelFuture.channel()));
					}
					
					connection.getClientChannel().read();
				} else {
					connection.getClientChannel().close();
				}
			});
		} catch (RuntimeException ex) {
			LogHelper.error("Encountered an error processing 'createServerConnection' in '" + getClass().getSimpleName() + "' - " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	public ServerBootstrap getServerBootstrap() {
		return serverBootstrap;
	}
}