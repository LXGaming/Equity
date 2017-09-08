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

package nz.co.lolnet.equity.entries;

import java.util.Objects;

import nz.co.lolnet.equity.entries.Connection.ConnectionState;
import nz.co.lolnet.equity.entries.Packet.PacketDirection;
import nz.co.lolnet.equity.util.EquityUtil;

public class PacketData {
	
	private final int packetId;
	private final int protocolVersion;
	private final ConnectionState connectionState;
	private final PacketDirection packetDirection;
	
	public PacketData(int packetId, int protocolVersion, ConnectionState connectionState, PacketDirection packetDirection) {
		this.packetId = packetId;
		this.protocolVersion = protocolVersion;
		this.connectionState = connectionState;
		this.packetDirection = packetDirection;
	}
	
	public int getPacketId() {
		return packetId;
	}
	
	public int getProtocolVersion() {
		return protocolVersion;
	}
	
	public ConnectionState getConnectionState() {
		return connectionState;
	}
	
	public PacketDirection getPacketDirection() {
		return packetDirection;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof PacketData)) {
			return false;
		}
		
		PacketData packetData = (PacketData) object;
		if (Objects.equals(getPacketId(), packetData.getPacketId())
				&& Objects.equals(getProtocolVersion(), packetData.getProtocolVersion())
				&& Objects.equals(getConnectionState(), packetData.getConnectionState())
				&& Objects.equals(getPacketDirection(), packetData.getPacketDirection())) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getPacketId(), getProtocolVersion(), getConnectionState(), getPacketDirection());
	}
	
	@Override
	public String toString() {
		return EquityUtil.getGsonWithTypeAdapter(getClass()).toJson(this, getClass());
	}
}