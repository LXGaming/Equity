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

public class Connection {
	
	private final List<Packet> packetQueue;
	private Channel clientChannel;
	private Channel serverChannel;
	private ConnectionState connectionState;
	private SocketAddress socketAddress;
	private int protocolVersion;
	private String username;
	private boolean encrypted;
	
	public Connection() {
		packetQueue = new ArrayList<Packet>();
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
	
	public boolean isChannel(Channel targetChannel, ConnectionSide connectionSide) {
		if (targetChannel == null || targetChannel.id() == null || connectionSide == null) {
			return false;
		}
		
		Channel baseChannel = getChannel(connectionSide);
		if (baseChannel == null || baseChannel.id() == null) {
			return false;
		}
		
		if (StringUtils.isAnyBlank(baseChannel.id().asLongText(), baseChannel.id().asShortText(), targetChannel.id().asLongText(), targetChannel.id().asShortText())) {
			return false;
		}
		
		if (StringUtils.equals(baseChannel.id().asLongText(), targetChannel.id().asLongText()) && StringUtils.equals(baseChannel.id().asShortText(), targetChannel.id().asShortText())) {
			return true;
		}
		return false;
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
	
	public List<Packet> getPacketQueue() {
		return packetQueue;
	}
	
	public Channel getClientChannel() {
		return clientChannel;
	}
	
	public void setClientChannel(Channel clientChannel) {
		this.clientChannel = clientChannel;
	}
	
	public Channel getServerChannel() {
		return serverChannel;
	}
	
	public void setServerChannel(Channel serverChannel) {
		this.serverChannel = serverChannel;
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
	
	public boolean isEncrypted() {
		return encrypted;
	}
	
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}
	
	public enum ConnectionState {
		
		HANDSHAKE, STATUS, LOGIN, PLAY;
		
		@Override
		public String toString() {
			return name().toUpperCase();
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
			return name().toUpperCase();
		}
	}
}