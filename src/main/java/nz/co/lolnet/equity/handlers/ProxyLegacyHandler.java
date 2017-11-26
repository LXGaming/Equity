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
import io.netty.handler.codec.ByteToMessageDecoder;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.managers.ConnectionManager;
import nz.co.lolnet.equity.util.EquityUtil;

import java.util.List;

public class ProxyLegacyHandler extends ByteToMessageDecoder {
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!in.isReadable()) {
            return;
        }
        
        Connection connection = ctx.channel().attr(EquityUtil.getConnectionKey()).get();
        if (connection == null || connection.getState() == null || !connection.isActive()) {
            in.skipBytes(in.readableBytes());
            return;
        }
        
        in.markReaderIndex();
        short packetId = in.readUnsignedByte();
        if (packetId == 254 && in.isReadable() && in.readUnsignedByte() == 1) {
            in.skipBytes(in.readableBytes());
            ConnectionManager.removeConnection(connection);
            return;
        }
        
        if (packetId == 2 && in.isReadable()) {
            in.skipBytes(in.readableBytes());
            ConnectionManager.removeConnection(connection);
            return;
        }
        
        in.resetReaderIndex();
        ctx.pipeline().remove(this);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception {
        Equity.getInstance().getLogger().error("Exception caught in {}", getClass().getSimpleName(), throwable);
    }
    
    public static String getName() {
        return "proxy_legacy";
    }
}