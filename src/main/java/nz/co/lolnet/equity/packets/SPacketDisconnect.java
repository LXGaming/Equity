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

package nz.co.lolnet.equity.packets;

import nz.co.lolnet.equity.entries.AbstractPacket;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Packet;
import io.netty.channel.ChannelFutureListener;

public class SPacketDisconnect extends AbstractPacket {
	
	private String reason;
	
	@Override
	public void read(Connection connection, Packet packet) {
		setReason(packet.readString());
	}
	
	@Override
	public void write(Connection connection, Packet packet) {
		if (connection.isEncrypted()) {
			throw new IllegalStateException("Cannot disconnect encrypted connection!");
		}
		
		packet.clearByteBuf();
		packet.writeVarInt(0);
		packet.writeString(getReason());
		connection.getClientChannel().writeAndFlush(packet.getByteBuf()).addListener(ChannelFutureListener.CLOSE);
	}
	
	public String getReason() {
		return reason;
	}
	
	public void setReason(String reason) {
		this.reason = reason;
	}
}