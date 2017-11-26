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
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.configuration.Config;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.managers.ConnectionManager;
import nz.co.lolnet.equity.util.EquityUtil;
import nz.co.lolnet.equity.util.PacketUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

public class HAProxyDecodingHandler extends ByteToMessageDecoder {
    
    private final HAProxyMessageDecoder haProxyMessageDecoder;
    private Method decode;
    private Field finished;
    
    public HAProxyDecodingHandler() {
        haProxyMessageDecoder = new HAProxyMessageDecoder();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        try {
            setDecode(HAProxyMessageDecoder.class.getDeclaredMethod("decode", ChannelHandlerContext.class, ByteBuf.class, List.class));
            getDecode().setAccessible(true);
            setFinished(HAProxyMessageDecoder.class.getDeclaredField("finished"));
            getFinished().setAccessible(true);
            super.channelActive(ctx);
        } catch (Exception ex) {
            Equity.getInstance().getLogger().error("Encountered an error processing {}::channelActive", getClass().getSimpleName(), ex);
        }
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        if (isFinished()) {
            ctx.pipeline().remove(this);
        }
    }
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Connection connection = ctx.channel().attr(EquityUtil.getConnectionKey()).get();
        if (connection == null || isFinished() || !invokeDecode(ctx, in, out)) {
            return;
        }
        
        if (out.isEmpty()) {
            return;
        }
        
        Object object = out.get(0);
        out.clear();
        if (!(object instanceof HAProxyMessage)) {
            return;
        }
        
        ConnectionManager.setSocketAddress(connection, new InetSocketAddress(((HAProxyMessage) object).sourceAddress(), ((HAProxyMessage) object).sourcePort()));
        Equity.getInstance().getConfig().map(Config::isIpForward).ifPresent(ipForward -> {
            if (ipForward) {
                int length = in.readerIndex();
                in.readerIndex(0);
                ConnectionManager.addPacketQueue(connection, PacketUtil.getByteBuf(ctx.alloc(), in, length));
                in.readerIndex(length);
            }
        });
    }
    
    @Override
    public boolean isSingleDecode() {
        return getHaProxyMessageDecoder().isSingleDecode();
    }
    
    private boolean invokeDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            Objects.requireNonNull(getDecode());
            Objects.requireNonNull(getHaProxyMessageDecoder());
            getDecode().invoke(getHaProxyMessageDecoder(), ctx, in, out);
            return true;
        } catch (ExceptionInInitializerError | IllegalAccessException | InvocationTargetException | RuntimeException ex) {
            Equity.getInstance().getLogger().error("Encountered an error processing {}::invokeDecode", getClass().getSimpleName(), ex);
            return false;
        }
    }
    
    private boolean isFinished() {
        try {
            Objects.requireNonNull(getFinished());
            Objects.requireNonNull(getHaProxyMessageDecoder());
            return (boolean) getFinished().get(getHaProxyMessageDecoder());
        } catch (ExceptionInInitializerError | IllegalAccessException | RuntimeException ex) {
            Equity.getInstance().getLogger().error("Encountered an error processing {}::isFinished", getClass().getSimpleName(), ex);
            return true;
        }
    }
    
    private HAProxyMessageDecoder getHaProxyMessageDecoder() {
        return haProxyMessageDecoder;
    }
    
    public Method getDecode() {
        return decode;
    }
    
    public void setDecode(Method decode) {
        this.decode = decode;
    }
    
    public Field getFinished() {
        return finished;
    }
    
    public void setFinished(Field finished) {
        this.finished = finished;
    }
    
    public static String getName() {
        return "proxy_haproxy";
    }
}