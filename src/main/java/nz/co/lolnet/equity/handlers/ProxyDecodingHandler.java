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
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Connection.ConnectionSide;
import nz.co.lolnet.equity.entries.Connection.ConnectionState;
import nz.co.lolnet.equity.entries.Packet;

public class ProxyDecodingHandler extends ByteToMessageDecoder {
	
	private final ConnectionSide connectionSide;
	
	public ProxyDecodingHandler(ConnectionSide connectionSide) {
		this.connectionSide = connectionSide;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		Connection connection = Equity.getInstance().getConnectionManager().getConnection(ctx.channel(), getConnectionSide());
		if (connection == null || connection.getConnectionState() == null) {
			throw new IllegalStateException(getConnectionSide() + " Connection error!");
		}
		
		if (connection.getConnectionState().equals(ConnectionState.PLAY) && !connection.isEncrypted()) {
			connection.setEncrypted(true);
		}
		
		Channel channel = connection.getChannel(getConnectionSide().getChannelSide());
		if (channel != null && connection.isEncrypted()) {
			channel.writeAndFlush(getPacket(ctx, in, in.readableBytes()).getByteBuf());
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
				int length = new Packet(Unpooled.wrappedBuffer(bytes)).readVarInt();
				if (length == 0) {
					throw new CorruptedFrameException("Empty Packet!");
				}
				
				if (in.readableBytes() < length) {
					in.resetReaderIndex();
					return;
				}
				
				out.add(getPacket(ctx, in, length));
				return;
			}
		}
		
		throw new CorruptedFrameException("Length wider than 21-bit");
	}
	
	private Packet getPacket(ChannelHandlerContext ctx, ByteBuf in, int length) {
		Packet packet = new Packet(Unpooled.EMPTY_BUFFER);
		if (in.hasMemoryAddress()) {
			packet = new Packet(in.slice(in.readerIndex(), length).retain());
			in.skipBytes(length);
		} else {
			ByteBuf byteBuf = ctx.alloc().directBuffer(length);
			in.readBytes(byteBuf);
			packet = new Packet(byteBuf);
		}
		
		return packet;
	}
	
	public ConnectionSide getConnectionSide() {
		return connectionSide;
	}
}