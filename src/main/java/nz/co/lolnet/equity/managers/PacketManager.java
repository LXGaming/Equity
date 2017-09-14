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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;

import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.AbstractPacket;
import nz.co.lolnet.equity.entries.Connection.ConnectionState;
import nz.co.lolnet.equity.entries.Packet.PacketDirection;
import nz.co.lolnet.equity.entries.PacketData;
import nz.co.lolnet.equity.entries.ProxyMessage;
import nz.co.lolnet.equity.packets.CPacketEncryptionResponse;
import nz.co.lolnet.equity.packets.CPacketHandshake;
import nz.co.lolnet.equity.packets.CPacketLoginStart;
import nz.co.lolnet.equity.packets.CPacketPing;
import nz.co.lolnet.equity.packets.CPacketServerInfo;
import nz.co.lolnet.equity.packets.SPacketDisconnect;
import nz.co.lolnet.equity.packets.SPacketEncryptionRequest;
import nz.co.lolnet.equity.packets.SPacketLoginSuccess;
import nz.co.lolnet.equity.packets.SPacketPong;
import nz.co.lolnet.equity.packets.SPacketServerInfo;

public class PacketManager {
	
	private final Map<PacketData, Class<? extends AbstractPacket>> registeredPackets;
	
	public PacketManager() {
		registeredPackets = new HashMap<PacketData, Class<? extends AbstractPacket>>();
	}
	
	public void registerPackets() {
		getRegisteredPackets().put(new PacketData(0, 0, ConnectionState.HANDSHAKE, PacketDirection.SERVERBOUND), CPacketHandshake.class);
		getRegisteredPackets().put(new PacketData(0, 0, ConnectionState.STATUS, PacketDirection.SERVERBOUND), CPacketServerInfo.class);
		getRegisteredPackets().put(new PacketData(0, 0, ConnectionState.STATUS, PacketDirection.CLIENTBOUND), SPacketServerInfo.class);
		getRegisteredPackets().put(new PacketData(1, 0, ConnectionState.STATUS, PacketDirection.SERVERBOUND), CPacketPing.class);
		getRegisteredPackets().put(new PacketData(1, 0, ConnectionState.STATUS, PacketDirection.CLIENTBOUND), SPacketPong.class);
		getRegisteredPackets().put(new PacketData(0, 0, ConnectionState.LOGIN, PacketDirection.CLIENTBOUND), SPacketDisconnect.class);
		getRegisteredPackets().put(new PacketData(0, 0, ConnectionState.LOGIN, PacketDirection.SERVERBOUND), CPacketLoginStart.class);
		getRegisteredPackets().put(new PacketData(1, 0, ConnectionState.LOGIN, PacketDirection.CLIENTBOUND), SPacketEncryptionRequest.class);
		getRegisteredPackets().put(new PacketData(1, 0, ConnectionState.LOGIN, PacketDirection.SERVERBOUND), CPacketEncryptionResponse.class);
		getRegisteredPackets().put(new PacketData(2, 0, ConnectionState.LOGIN, PacketDirection.CLIENTBOUND), SPacketLoginSuccess.class);
	}
	
	public void processProxyMessage(ProxyMessage proxyMessage, PacketDirection packetDirection) {
		try {
			if (proxyMessage == null || packetDirection == null || !proxyMessage.isValid()) {
				throw new IllegalArgumentException("Provided arguments are invalid!");
			}
			
			if (Objects.equals(proxyMessage.getConnection().getConnectionState(), ConnectionState.PLAY)) {
				throw new IllegalStateException("Cannot process encrypted packets!");
			}
			
			proxyMessage.getPacket().getByteBuf().markReaderIndex();
			int packetId = proxyMessage.getPacket().readVarInt();
			
			AbstractPacket abstractPacket = getPacket(
					new PacketData(packetId, proxyMessage.getConnection().getProtocolVersion(), proxyMessage.getConnection().getConnectionState(), packetDirection));
			
			if (abstractPacket == null) {
				proxyMessage.getPacket().getByteBuf().resetReaderIndex();
				return;
			}
			
			abstractPacket.read(proxyMessage);
			proxyMessage.getPacket().getByteBuf().resetReaderIndex();
		} catch (RuntimeException ex) {
			Equity.getInstance().getLogger().error("Encountered an error processing {}::process", getClass().getSimpleName(), ex);
		}
	}
	
	private AbstractPacket getPacket(PacketData packetData) {
		try {
			if (packetData == null || getRegisteredPackets() == null) {
				throw new IllegalArgumentException("Provided arguments are invalid!");
			}
			
			Class<? extends AbstractPacket> abstractPacket = getRegisteredPackets().get(packetData);
			if (abstractPacket == null && packetData.getProtocolVersion() != 0) {
				return getPacket(new PacketData(packetData.getPacketId(), 0, packetData.getConnectionState(), packetData.getPacketDirection()));
			}
			
			return abstractPacket.newInstance();
		} catch (ExceptionInInitializerError | IllegalAccessException | InstantiationException | RuntimeException ex) {
			Equity.getInstance().getLogger().error("Encountered an error processing {}::getPacket", getClass().getSimpleName(), ex);
			return null;
		}
	}
	
	public Optional<Integer> getPacketId(Class<? extends AbstractPacket> abstractPacketClass, int protocolVersion) {
		Map<Integer, Integer> packetMap = getRegisteredPackets().entrySet().stream()
				.filter(entry -> ObjectUtils.allNotNull(entry, entry.getKey(), entry.getValue()))
				.filter(entry -> Objects.equals(entry.getValue(), abstractPacketClass))
				.collect(Collectors.toMap(entry -> entry.getKey().getProtocolVersion(), entry -> entry.getKey().getPacketId()));
		return Optional.ofNullable(packetMap.getOrDefault(protocolVersion, packetMap.get(0)));
	}
	
	public Map<PacketData, Class<? extends AbstractPacket>> getRegisteredPackets() {
		return registeredPackets;
	}
}