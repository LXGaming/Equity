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

import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.AbstractPacket;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Connection.ConnectionState;
import nz.co.lolnet.equity.entries.Packet;

public class CPacketHandshake extends AbstractPacket {
	
	private int protocolVersion;
	private String serverAddress;
	private int serverPort;
	private int state;
	
	@Override
	public void read(Connection connection, Packet packet) {
		setProtocolVersion(packet.readVarInt());
		setServerAddress(packet.readString());
		setServerPort(packet.getByteBuf().readUnsignedShort());
		setState(packet.readVarInt());
		
		connection.setProtocolVersion(getProtocolVersion());
		if (getState() == 1) {
			connection.setConnectionState(ConnectionState.STATUS);
		} else if (getState() == 2) {
			connection.setConnectionState(ConnectionState.LOGIN);
		}
		
		Equity.getInstance().getProxyManager().createServerConnection(connection);
	}
	
	public int getProtocolVersion() {
		return protocolVersion;
	}
	
	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}
	
	public String getServerAddress() {
		return serverAddress;
	}
	
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	
	public int getServerPort() {
		return serverPort;
	}
	
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = state;
	}
}