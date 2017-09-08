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
import java.util.Iterator;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Packet;
import nz.co.lolnet.equity.entries.ProxyMessage;
import nz.co.lolnet.equity.util.EquityUtil;

public class ConnectionManager {
	
	private final List<Connection> connections;
	
	public ConnectionManager() {
		this.connections = new ArrayList<Connection>();
	}
	
	public void addConnection(Connection connection) {
		if (getConnections() == null || connection == null) {
			return;
		}
		
		if (Equity.getInstance().getProxyManager() == null || !Equity.getInstance().isRunning()) {
			return;
		}
		
		getConnections().add(connection);
		Equity.getInstance().getLogger().info("{} -> Connected", connection.getIdentity());
	}
	
	public void addPacket(Connection connection, Packet packet) {
		if (connection == null || packet == null || connection.getPacketQueue() == null) {
			return;
		}
		
		if (connection.getPacketQueue().size() > 10) {
			Equity.getInstance().getLogger().warn("{} -> Queued over 10 packets, Assuming malicious client!", connection.getIdentity());
			removeConnection(connection);
			return;
		}
		
		connection.getPacketQueue().add(packet);
	}
	
	public void setSocketAddress(Connection connection, SocketAddress socketAddress) {
		if (connection == null || socketAddress == null) {
			return;
		}
		
		connection.setSocketAddress(socketAddress);
		Equity.getInstance().getLogger().info("{} -> PROXY {}", EquityUtil.getAddress(connection.getClientChannel().localAddress()), EquityUtil.getAddress(connection.getAddress()));
	}
	
	public void removeConnection(Connection connection) {
		if (getConnections() == null || connection == null || !getConnections().remove(connection)) {
			return;
		}
		
		connection.setActive(false);
		closeChannel(connection.getClientChannel());
		closeChannel(connection.getServerChannel());
		
		if (connection.getPacketQueue() != null && !connection.getPacketQueue().isEmpty()) {
			for (Iterator<Object> iterator = connection.getPacketQueue().iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				iterator.remove();
				if (object instanceof ByteBuf) {
					EquityUtil.safeRelease(((ByteBuf) object));
				}
				
				if (object instanceof ProxyMessage) {
					EquityUtil.safeRelease(((ProxyMessage) object).getPacket().getByteBuf());
				}
			}
		}
		
		Equity.getInstance().getLogger().info("{} -> Disconnected", connection.getIdentity());
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