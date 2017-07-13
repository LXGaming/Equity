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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nz.co.lolnet.equity.entries.AbstractPacket;
import nz.co.lolnet.equity.entries.Connection.ConnectionState;
import nz.co.lolnet.equity.entries.Packet.PacketDirection;
import nz.co.lolnet.equity.entries.PacketData;
import nz.co.lolnet.equity.entries.ProxyMessage;
import nz.co.lolnet.equity.packets.CPacketHandshake;
import nz.co.lolnet.equity.packets.CPacketLoginStart;
import nz.co.lolnet.equity.packets.CPacketPing;
import nz.co.lolnet.equity.packets.CPacketServerInfo;
import nz.co.lolnet.equity.packets.SPacketPong;
import nz.co.lolnet.equity.packets.SPacketServerInfo;
import nz.co.lolnet.equity.util.LogHelper;

public class PacketManager {
	
	private final Map<Class<? extends AbstractPacket>, List<PacketData>> registeredPackets;
	
	public PacketManager() {
		registeredPackets = new HashMap<Class<? extends AbstractPacket>, List<PacketData>>();
	}
	
	public void registerPackets() {
		getRegisteredPackets().put(CPacketHandshake.class, Arrays.asList(
				new PacketData(0, 0, ConnectionState.HANDSHAKE, PacketDirection.SERVERBOUND)));
		getRegisteredPackets().put(CPacketLoginStart.class, Arrays.asList(
				new PacketData(0, 0, ConnectionState.LOGIN, PacketDirection.SERVERBOUND)));
		getRegisteredPackets().put(CPacketPing.class, Arrays.asList(
				new PacketData(1, 0, ConnectionState.STATUS, PacketDirection.SERVERBOUND)));
		getRegisteredPackets().put(CPacketServerInfo.class, Arrays.asList(
				new PacketData(0, 0, ConnectionState.STATUS, PacketDirection.SERVERBOUND)));
		getRegisteredPackets().put(SPacketPong.class, Arrays.asList(
				new PacketData(1, 0, ConnectionState.STATUS, PacketDirection.CLIENTBOUND)));
		getRegisteredPackets().put(SPacketServerInfo.class, Arrays.asList(
				new PacketData(0, 0, ConnectionState.STATUS, PacketDirection.CLIENTBOUND)));
	}
	
	public void process(ProxyMessage proxyMessage, PacketDirection packetDirection) {
		try {
			if (proxyMessage == null || packetDirection == null || proxyMessage.getConnection() == null || proxyMessage.getPacket() == null) {
				return;
			}
			
			if (proxyMessage.getConnection().getConnectionState() == null || proxyMessage.getConnection().getConnectionState().equals(ConnectionState.PLAY)) {
				throw new IllegalStateException("Cannot process encrypted packets!");
			}
			
			proxyMessage.getPacket().getByteBuf().markReaderIndex();
			int packetId = proxyMessage.getPacket().readVarInt();
			for (Iterator<Entry<Class<? extends AbstractPacket>, List<PacketData>>> iterator = getRegisteredPackets().entrySet().iterator(); iterator.hasNext();) {
				Entry<Class<? extends AbstractPacket>, List<PacketData>> entry = iterator.next();
				PacketData targetPacketData = new PacketData(packetId, proxyMessage.getConnection().getProtocolVersion(), proxyMessage.getConnection().getConnectionState(), packetDirection);
				if (entry == null || !checkPacketData(targetPacketData, entry.getValue())) {
					continue;
				}
				
				AbstractPacket abstractPacket = entry.getKey().newInstance();
				abstractPacket.read(proxyMessage);
				proxyMessage.getPacket().getByteBuf().resetReaderIndex();
				return;
			}
		} catch (ExceptionInInitializerError | IllegalAccessException | InstantiationException | RuntimeException ex) {
			LogHelper.error("Encountered an error processing 'process' in '" + getClass().getSimpleName() + "' - " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	private boolean checkPacketData(PacketData targetPacketData, List<PacketData> packetDatas) {
		if (targetPacketData == null || targetPacketData.getConnectionState() == null || targetPacketData.getPacketDirection() == null || packetDatas == null) {
			return false;
		}
		
		for (Iterator<PacketData> iterator = packetDatas.iterator(); iterator.hasNext();) {
			PacketData packetData = iterator.next();
			if (packetData == null || packetData.getConnectionState() == null || packetData.getPacketDirection() == null) {
				continue;
			}
			
			if (!packetData.getConnectionState().equals(targetPacketData.getConnectionState()) || !packetData.getPacketDirection().equals(targetPacketData.getPacketDirection())) {
				continue;
			}
			
			if (packetData.getProtocolVersion() <= targetPacketData.getProtocolVersion() && packetData.getPacketId() == targetPacketData.getPacketId()) {
				return true;
			}
		}
		
		return false;
	}
	
	public Map<Class<? extends AbstractPacket>, List<PacketData>> getRegisteredPackets() {
		return registeredPackets;
	}
}