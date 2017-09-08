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

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.netty.channel.Channel;
import nz.co.lolnet.equity.entries.Packet.PacketDirection;
import nz.co.lolnet.equity.util.EquityUtil;

public class Connection {
	
	private final List<Object> packetQueue;
	private ConnectionState connectionState;
	private SocketAddress socketAddress;
	private Channel clientChannel;
	private Channel serverChannel;
	private int protocolVersion;
	private String username;
	private boolean active;
	
	public Connection() {
		packetQueue = new ArrayList<Object>();
		connectionState = ConnectionState.HANDSHAKE;
	}
	
	public Channel getChannel(ConnectionSide connectionSide) {
		if (connectionSide != null && connectionSide.equals(ConnectionSide.CLIENT)) {
			return getClientChannel();
		}
		
		if (connectionSide != null && connectionSide.equals(ConnectionSide.SERVER)) {
			return getServerChannel();
		}
		
		return null;
	}
	
	public String getIdentity() {
		if (hasUsername()) {
			return getUsername();
		}
		
		return EquityUtil.getAddress(getAddress());
	}
	
	public SocketAddress getAddress() {
		if (getSocketAddress() != null) {
			return getSocketAddress();
		}
		
		if (getClientChannel() != null && getClientChannel().localAddress() != null) {
			return getClientChannel().localAddress();
		}
		
		return null;
	}
	
	public PacketData createPacketData(int packetId, PacketDirection packetDirection) {
		return new PacketData(packetId, getProtocolVersion(), getConnectionState(), packetDirection);
	}
	
	public List<Object> getPacketQueue() {
		return packetQueue;
	}
	
	public ConnectionState getConnectionState() {
		return connectionState;
	}
	
	public void setConnectionState(ConnectionState connectionState) {
		this.connectionState = connectionState;
	}
	
	public SocketAddress getSocketAddress() {
		return socketAddress;
	}
	
	public void setSocketAddress(SocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}
	
	public Channel getClientChannel() {
		return clientChannel;
	}
	
	public void setClientChannel(Channel clientChannel) {
		this.clientChannel = clientChannel;
		
		if (getClientChannel() != null) {
			getClientChannel().attr(EquityUtil.getAttributeKey()).set(this);
		}
	}
	
	public Channel getServerChannel() {
		return serverChannel;
	}
	
	public void setServerChannel(Channel serverChannel) {
		this.serverChannel = serverChannel;
		
		if (getServerChannel() != null) {
			getServerChannel().attr(EquityUtil.getAttributeKey()).set(this);
		}
	}
	
	public int getProtocolVersion() {
		return protocolVersion;
	}
	
	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}
	
	public String getUsername() {
		return username;
	}
	
	public boolean hasUsername() {
		if (StringUtils.isNotBlank(getUsername())) {
			return true;
		}
		
		return false;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public enum ConnectionState {
		
		HANDSHAKE, STATUS, LOGIN, PLAY;
		
		@Override
		public String toString() {
			return StringUtils.capitalize(name().toLowerCase());
		}
	}
	
	public enum ConnectionSide {
		
		CLIENT, SERVER;
		
		public ConnectionSide getChannelSide() {
			if (equals(ConnectionSide.CLIENT)) {
				return ConnectionSide.SERVER;
			}
			
			if (equals(ConnectionSide.SERVER)) {
				return ConnectionSide.CLIENT;
			}
			
			return null;
		}
		
		@Override
		public String toString() {
			return StringUtils.capitalize(name().toLowerCase());
		}
	}
}