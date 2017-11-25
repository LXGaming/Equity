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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Protocol;
import nz.co.lolnet.equity.entries.ProxyMessage;
import nz.co.lolnet.equity.managers.ConnectionManager;
import nz.co.lolnet.equity.managers.PacketManager;
import nz.co.lolnet.equity.util.EquityUtil;

public class ProxyClientHandler extends ChannelInboundHandlerAdapter {
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Connection connection = new Connection();
        connection.setClientChannel(ctx.channel());
        connection.setState(Protocol.State.HANDSHAKE);
        connection.setActive(true);
        connection.setServer("Unknown");
        ConnectionManager.addConnection(connection);
        ctx.channel().attr(EquityUtil.getConnectionKey()).set(connection);
        ctx.read();
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Connection connection = ctx.channel().attr(EquityUtil.getConnectionKey()).get();
        if (connection == null || connection.getState() == null || !connection.isActive()) {
            return;
        }
        
        if (msg instanceof ByteBuf) {
            connection.getServerChannel().writeAndFlush(msg).addListener(EquityUtil.getFutureListener(ctx.channel()));
            return;
        }
        
        if (msg instanceof ProxyMessage) {
            ProxyMessage proxyMessage = (ProxyMessage) msg;
            proxyMessage.setDirection(Protocol.Direction.SERVERBOUND);
            PacketManager.process(proxyMessage);
            if (connection.getServerChannel() == null) {
                ConnectionManager.addPacketQueue(connection, proxyMessage);
                ctx.read();
                return;
            }
            
            connection.getServerChannel().writeAndFlush(proxyMessage).addListener(EquityUtil.getFutureListener(ctx.channel()));
            return;
        }
        
        throw new UnsupportedOperationException("Unsupported message received!");
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Connection connection = ctx.channel().attr(EquityUtil.getConnectionKey()).get();
        if (connection == null) {
            return;
        }
        
        ConnectionManager.removeConnection(connection);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        Equity.getInstance().getLogger().error("Exception caught in {}", getClass().getSimpleName(), throwable);
    }
    
    public static String getName() {
        return "proxy_client";
    }
}