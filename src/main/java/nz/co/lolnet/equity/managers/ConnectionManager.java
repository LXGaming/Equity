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
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Packet;
import nz.co.lolnet.equity.entries.ProxyMessage;
import nz.co.lolnet.equity.util.EquityUtil;
import nz.co.lolnet.equity.util.LogHelper;

public class ConnectionManager {
	
	private final List<Connection> connections;
	
	public ConnectionManager() {
		this.connections = new ArrayList<Connection>();
	}
	
	public void addConnection(Connection connection) {
		if (getConnections() == null || connection == null) {
			return;
		}
		
		if (Equity.getInstance().getProxyManager() == null || !Equity.getInstance().getProxyManager().isRunning()) {
			return;
		}
		
		getConnections().add(connection);
		LogHelper.info(EquityUtil.getAddress(connection.getAddress()) + " -> Connected.");
	}
	
	public void addPacket(Connection connection, Packet packet) {
		if (connection == null || packet == null || connection.getPacketQueue() == null) {
			return;
		}
		
		if (connection.getPacketQueue().size() > 10) {
			LogHelper.warn(connection.getIdentity() + " -> Queued over 10 packets, Assuming malicious client!");
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
		LogHelper.info(EquityUtil.getAddress(connection.getClientChannel().localAddress()) + " -> " + EquityUtil.getAddress(connection.getAddress()));
	}
	
	public void removeConnection(Connection connection) {
		if (getConnections() == null || connection == null || !getConnections().remove(connection)) {
			return;
		}
		
		if (connection.getClientChannel() != null && connection.getClientChannel().isActive()) {
			connection.getClientChannel().close();
		}
		
		if (connection.getServerChannel() != null && connection.getServerChannel().isActive()) {
			connection.getServerChannel().close();
		}
		
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
		
		LogHelper.info(connection.getIdentity() + " -> Disconnected.");
	}
	
	public List<Connection> getConnections() {
		return connections;
	}
}