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

package nz.co.lolnet.equity.managers;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Connection.ConnectionState;
import nz.co.lolnet.equity.entries.ProxyMessage;
import nz.co.lolnet.equity.entries.ServerMessage;
import nz.co.lolnet.equity.packets.SPacketDisconnect;
import nz.co.lolnet.equity.packets.SPacketServerInfo;
import nz.co.lolnet.equity.text.Text;
import nz.co.lolnet.equity.util.EquityUtil;

public class ConnectionManager {
	
	private final List<Connection> connections;
	
	public ConnectionManager() {
		this.connections = new ArrayList<Connection>();
	}
	
	public void addConnection(Connection connection) {
		if (connection == null || getConnections() == null || !Equity.getInstance().isRunning()) {
			return;
		}
		
		getConnections().add(connection);
		Equity.getInstance().getLogger().info("{} -> Connected", connection.getIdentity());
	}
	
	public void addPacketQueue(Connection connection, Object object) {
		if (connection == null || object == null || connection.getPacketQueue() == null) {
			return;
		}
		
		if (connection.getPacketQueue().size() > 10) {
			Equity.getInstance().getLogger().warn("{} -> Queued over 10 packets, Assuming malicious client!", connection.getIdentity());
			removeConnection(connection);
			return;
		}
		
		connection.getPacketQueue().add(object);
	}
	
	public void setSocketAddress(Connection connection, SocketAddress socketAddress) {
		if (connection == null || socketAddress == null) {
			return;
		}
		
		connection.setSocketAddress(socketAddress);
		Equity.getInstance().getLogger().info("{} -> PROXY {}", EquityUtil.getAddress(connection.getClientChannel().localAddress()), EquityUtil.getAddress(connection.getAddress()));
	}
	
	public void disconnect(Connection connection, Text description) {
		if (connection == null || description == null || connection.getClientChannel() == null) {
			removeConnection(connection);
			return;
		}
		
		ServerMessage serverMessage = new ServerMessage();
		serverMessage.getVersion().setProtocol(connection.getProtocolVersion());
		serverMessage.setDescription(description);
		
		if (Objects.equals(connection.getConnectionState(), ConnectionState.STATUS)) {
			SPacketServerInfo serverInfo = new SPacketServerInfo();
			serverInfo.setServerPing(serverMessage);
			serverInfo.write(new ProxyMessage(connection));
		}
		
		if (Objects.equals(connection.getConnectionState(), ConnectionState.LOGIN)) {
			SPacketDisconnect disconnect = new SPacketDisconnect();
			disconnect.setReason(serverMessage.getDescription());
			disconnect.write(new ProxyMessage(connection));
		}
		
		removeConnection(connection);
	}
	
	public void removeConnection(Connection connection) {
		if (connection == null || getConnections() == null || !getConnections().remove(connection)) {
			return;
		}
		
		connection.setActive(false);
		closeChannel(connection.getClientChannel());
		closeChannel(connection.getServerChannel());
		clearPacketQueue(connection);
		Equity.getInstance().getLogger().info("{} -> Disconnected", connection.getIdentity());
	}
	
	public void clearPacketQueue(Connection connection) {
		if (connection == null || connection.getPacketQueue() == null) {
			return;
		}
		
		for (Object object : connection.getPacketQueue()) {
			if (object instanceof ByteBuf) {
				EquityUtil.safeRelease(((ByteBuf) object));
			}
			
			if (object instanceof ProxyMessage) {
				EquityUtil.safeRelease(((ProxyMessage) object).getPacket().getByteBuf());
			}
		}
		
		connection.getPacketQueue().clear();
	}
	
	private void closeChannel(Channel channel) {
		if (channel == null) {
			return;
		}
		
		if (channel.hasAttr(EquityUtil.getAttributeKey())) {
			channel.attr(EquityUtil.getAttributeKey()).set(null);
		}
		
		if (channel.isActive()) {
			channel.close();
		}
	}
	
	public List<Connection> getConnections() {
		return connections;
	}
}