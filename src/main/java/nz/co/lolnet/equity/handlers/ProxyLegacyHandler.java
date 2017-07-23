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

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Connection.ConnectionSide;
import nz.co.lolnet.equity.util.EquityUtil;

public class ProxyLegacyHandler extends ByteToMessageDecoder {
	
	private final ConnectionSide connectionSide;
	
	public ProxyLegacyHandler() {
		this.connectionSide = ConnectionSide.CLIENT;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		Connection connection = ctx.channel().attr(EquityUtil.getAttributeKey()).get();
		if (connection == null || connection.getConnectionState() == null) {
			throw new IllegalStateException(getConnectionSide().toString() + " Connection error!");
		}
		
		if (!in.isReadable()) {
			return;
		}
		
		if (!connection.isActive()) {
			in.skipBytes(in.readableBytes());
			return;
		}
		
		in.markReaderIndex();
		short packetId = in.readUnsignedByte();
		if (packetId == 254 && in.isReadable() && in.readUnsignedByte() == 1) {
			in.skipBytes(in.readableBytes());
			return;
		}
		
		if (packetId == 2 && in.isReadable()) {
			in.skipBytes(in.readableBytes());
			connection.getClientChannel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
			return;
		}
		
		in.resetReaderIndex();
		ctx.pipeline().remove(this);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		Equity.getInstance().getLogger().error("Exception caught in {}", getClass().getSimpleName(), throwable);
	}
	
	public ConnectionSide getConnectionSide() {
		return connectionSide;
	}
}