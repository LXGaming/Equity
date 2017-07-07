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

public class CPacketPing extends AbstractPacket {
	
	private long clientTime;
	
	@Override
	public void read(Connection connection, Packet packet) {
		setClientTime(packet.getByteBuf().readLong());
	}
	
	public long getClientTime() {
		return clientTime;
	}
	
	public void setClientTime(long clientTime) {
		this.clientTime = clientTime;
	}
}