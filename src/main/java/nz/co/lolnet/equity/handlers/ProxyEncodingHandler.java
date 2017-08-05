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
import io.netty.handler.codec.MessageToByteEncoder;
import nz.co.lolnet.equity.entries.Connection.ConnectionSide;
import nz.co.lolnet.equity.entries.Packet;
import nz.co.lolnet.equity.entries.ProxyMessage;

public class ProxyEncodingHandler extends MessageToByteEncoder<ProxyMessage> {
	
	private final ConnectionSide connectionSide;
	
	public ProxyEncodingHandler(ConnectionSide connectionSide) {
		this.connectionSide = connectionSide;
	}
	
	@Override
	protected void encode(ChannelHandlerContext ctx, ProxyMessage msg, ByteBuf out) throws Exception {
		Packet packet = new Packet(out);
		int length = msg.getPacket().getByteBuf().readableBytes();
		int varIntSize = packet.getVarIntSize(length);
		if (varIntSize > 3) {
			throw new IllegalArgumentException("Unable to fit " + length + " into " + 3);
		}
		
		packet.getByteBuf().ensureWritable(varIntSize + length);
		packet.writeVarInt(length);
		packet.getByteBuf().writeBytes(msg.getPacket().getByteBuf());
		msg.getPacket().getByteBuf().release();
		ctx.read();
	}
	
	public ConnectionSide getConnectionSide() {
		return connectionSide;
	}
}