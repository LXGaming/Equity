/*
 * Copyright 2017 Alex Thomson
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

package io.github.lxgaming.equity.managers;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.github.lxgaming.equity.Equity;
import io.github.lxgaming.equity.configuration.Config;
import io.github.lxgaming.equity.configuration.Messages;
import io.github.lxgaming.equity.entries.Connection;
import io.github.lxgaming.equity.entries.Server;
import io.github.lxgaming.equity.handlers.ProxyChannelHandler;
import io.github.lxgaming.equity.handlers.ProxyClientHandler;
import io.github.lxgaming.equity.handlers.ProxyServerHandler;
import io.github.lxgaming.equity.util.Toolbox;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProxyManager {
    
    private static EventLoopGroup eventLoopGroup;
    private static Class<? extends ServerSocketChannel> eventLoopGroupClass;
    
    public static void buildProxy() {
        Equity.getInstance().getConfig().ifPresent(config -> {
            if (config.isNativeTransport() && Epoll.isAvailable()) {
                setEventLoopGroup(new EpollEventLoopGroup(config.getMaxThreads(), Toolbox.buildThreadFactory("Netty Epoll Thread #%d")));
                setEventLoopGroupClass(EpollServerSocketChannel.class);
                Equity.getInstance().getLogger().info("Using Epoll Transport.");
            } else {
                setEventLoopGroup(new NioEventLoopGroup(config.getMaxThreads(), Toolbox.buildThreadFactory("Netty IO Thread #%d")));
                setEventLoopGroupClass(NioServerSocketChannel.class);
                Equity.getInstance().getLogger().info("Using NIO Transport.");
            }
            
            startProxy(config.getPort());
        });
    }
    
    private static void startProxy(int port) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(getEventLoopGroup());
        serverBootstrap.channel(getEventLoopGroupClass());
        serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
        serverBootstrap.childAttr(Toolbox.getSideKey(), ProxyClientHandler.getName());
        serverBootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, Toolbox.getWriteBufferWaterMark());
        serverBootstrap.childHandler(new ProxyChannelHandler());
        serverBootstrap.bind(port).addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                Equity.getInstance().getLogger().info("Listening on {}", Toolbox.getAddress(future.channel().localAddress()));
            } else {
                Equity.getInstance().getLogger().warn("Could not bind to {}", port, future.cause());
            }
        });
    }
    
    public static void createServerConnection(Connection connection) {
        try {
            Objects.requireNonNull(connection, "Connection cannot be null");
            Objects.requireNonNull(connection.getClientChannel(), "Channel cannot be null");
            
            Optional<List<Server>> servers = getServers(connection.getVersion());
            if (!servers.isPresent()) {
                Equity.getInstance().getLogger().error("Failed to get servers");
                ConnectionManager.disconnect(connection, Equity.getInstance().getMessages().map(Messages::getError).orElse(null));
                return;
            }
            
            if (servers.get().isEmpty()) {
                Equity.getInstance().getLogger().warn("Failed to find server handling protocol {}", connection.getVersion());
                ConnectionManager.disconnect(connection, Equity.getInstance().getMessages().map(Messages::getUnsupported).orElse(null));
                return;
            }
            
            filterServers(servers.get());
            
            if (servers.get().isEmpty()) {
                Equity.getInstance().getLogger().warn("Failed to find server handling protocol {}", connection.getVersion());
                ConnectionManager.disconnect(connection, Equity.getInstance().getMessages().map(Messages::getUnavailable).orElse(null));
                return;
            }
            
            Server server = servers.get().get(new SecureRandom().nextInt(servers.get().size()));
            
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(getEventLoopGroup());
            bootstrap.channel(connection.getClientChannel().getClass());
            bootstrap.attr(Toolbox.getSideKey(), ProxyServerHandler.getName());
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Equity.getInstance().getConfig().map(Config::getConnectTimeout).orElse(0));
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, Toolbox.getWriteBufferWaterMark());
            bootstrap.handler(new ProxyChannelHandler());
            
            bootstrap.connect(server.getHost(), server.getPort()).addListener((ChannelFuture future) -> {
                if (future.isSuccess()) {
                    future.channel().attr(Toolbox.getConnectionKey()).set(connection);
                    server.getIdentity().ifPresent(connection::setServer);
                    connection.setServerChannel(future.channel());
                    connection.getPacketQueue().forEach(object -> connection.getServerChannel().write(object));
                    connection.getServerChannel().flush();
                    connection.getPacketQueue().clear();
                } else {
                    ConnectionManager.disconnect(connection, Equity.getInstance().getMessages().map(Messages::getError).orElse(null));
                }
            });
        } catch (RuntimeException ex) {
            Equity.getInstance().getLogger().error("Encountered an error processing {}::createServerConnection", "ProxyManager", ex);
        }
    }
    
    private static Optional<List<Server>> getServers(int version) {
        Optional<List<Server>> servers = Equity.getInstance().getConfig().map(Config::getServers);
        if (!servers.isPresent() || servers.get().isEmpty()) {
            return Optional.empty();
        }
        
        List<Server> supportedServers = Toolbox.newArrayList();
        servers.get().forEach(server -> {
            if (server.getVersions().contains(version)) {
                supportedServers.add(server);
            }
        });
        
        return Optional.of(supportedServers);
    }
    
    private static void filterServers(List<Server> servers) {
        servers.removeIf(server -> !isAvailable(server.getName(), server.getHost(), server.getPort()));
    }
    
    private static boolean isAvailable(String name, String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), Equity.getInstance().getConfig().map(Config::getConnectTimeout).orElse(0));
            return true;
        } catch (IOException | RuntimeException ex) {
            Equity.getInstance().getLogger().warn("Server {} is not available!", name);
            return false;
        }
    }
    
    public static EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }
    
    private static void setEventLoopGroup(EventLoopGroup eventLoopGroup) {
        ProxyManager.eventLoopGroup = eventLoopGroup;
    }
    
    private static Class<? extends ServerSocketChannel> getEventLoopGroupClass() {
        return eventLoopGroupClass;
    }
    
    private static void setEventLoopGroupClass(Class<? extends ServerSocketChannel> eventLoopGroupClass) {
        ProxyManager.eventLoopGroupClass = eventLoopGroupClass;
    }
}