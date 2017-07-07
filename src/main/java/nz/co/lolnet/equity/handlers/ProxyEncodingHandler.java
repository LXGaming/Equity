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

import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Connection.ConnectionSide;
import nz.co.lolnet.equity.entries.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProxyEncodingHandler extends MessageToByteEncoder<ByteBuf> {
	
	private final ConnectionSide connectionSide;
	
	public ProxyEncodingHandler(ConnectionSide connectionSide) {
		this.connectionSide = connectionSide;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
		Packet packet = new Packet(out);
		Connection connection = Equity.getInstance().getConnectionManager().getConnection(ctx.channel(), getConnectionSide());
		if (connection == null || connection.getConnectionState() == null) {
			throw new IllegalStateException(getConnectionSide() + " Connection error!");
		}
		
		if (connection.isEncrypted()) {
			packet.getByteBuf().writeBytes(msg);
			return;
		}
		
		int length = msg.readableBytes();
		int varIntSize = packet.getVarIntSize(length);
		if (varIntSize > 3) {
			throw new IllegalArgumentException("Unable to fit " + length + " into " + 3);
		}
		
		packet.getByteBuf().ensureWritable(varIntSize + length);
		packet.writeVarInt(length);
		packet.getByteBuf().writeBytes(msg);
	}
	
	public ConnectionSide getConnectionSide() {
		return connectionSide;
	}
}