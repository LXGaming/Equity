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

package io.github.lxgaming.equity.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.github.lxgaming.equity.entries.Connection;
import io.github.lxgaming.equity.entries.Protocol;
import io.github.lxgaming.equity.entries.ProxyMessage;
import io.github.lxgaming.equity.util.PacketUtil;
import io.github.lxgaming.equity.util.Toolbox;

import java.util.List;
import java.util.Objects;

public class ProxyDecodingHandler extends ByteToMessageDecoder {
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Connection connection = ctx.channel().attr(Toolbox.getConnectionKey()).get();
        if (connection == null || connection.getState() == null || !connection.isActive()) {
            in.skipBytes(in.readableBytes());
            return;
        }
        
        if (Objects.equals(connection.getState(), Protocol.State.PLAY)) {
            out.add(PacketUtil.getByteBuf(ctx.alloc(), in, in.readableBytes()));
            return;
        }
        
        in.markReaderIndex();
        byte[] bytes = new byte[3];
        for (int index = 0; index < bytes.length; index++) {
            if (!in.isReadable()) {
                in.resetReaderIndex();
                return;
            }
            
            bytes[index] = in.readByte();
            if (bytes[index] >= 0) {
                ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
                
                try {
                    int length = PacketUtil.readVarInt(byteBuf);
                    if (length == 0) {
                        throw new CorruptedFrameException("Empty Packet!");
                    }
                    
                    if (in.readableBytes() < length) {
                        in.resetReaderIndex();
                        return;
                    }
                    
                    out.add(new ProxyMessage(PacketUtil.getByteBuf(ctx.alloc(), in, length), connection));
                } finally {
                    byteBuf.release();
                }
                
                return;
            }
        }
        
        throw new CorruptedFrameException("Length wider than 21-bit");
    }
    
    public static String getName() {
        return "proxy_decoder";
    }
}