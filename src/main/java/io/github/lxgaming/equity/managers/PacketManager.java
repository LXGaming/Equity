/*
 * Copyright 2017 Alex Thomson
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

package io.github.lxgaming.equity.managers;

import io.github.lxgaming.equity.Equity;
import io.github.lxgaming.equity.entries.Protocol;
import io.github.lxgaming.equity.entries.ProxyMessage;
import io.github.lxgaming.equity.packets.AbstractPacket;
import io.github.lxgaming.equity.packets.CPacketEncryptionResponse;
import io.github.lxgaming.equity.packets.CPacketHandshake;
import io.github.lxgaming.equity.packets.CPacketLoginStart;
import io.github.lxgaming.equity.packets.CPacketPing;
import io.github.lxgaming.equity.packets.CPacketServerInfo;
import io.github.lxgaming.equity.packets.SPacketDisconnect;
import io.github.lxgaming.equity.packets.SPacketEncryptionRequest;
import io.github.lxgaming.equity.packets.SPacketLoginSuccess;
import io.github.lxgaming.equity.packets.SPacketPong;
import io.github.lxgaming.equity.packets.SPacketServerInfo;
import io.github.lxgaming.equity.util.PacketUtil;
import io.github.lxgaming.equity.util.Toolbox;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PacketManager {
    
    private static final Map<Protocol, Class<? extends AbstractPacket>> REGISTERED_PACKETS = Toolbox.newHashMap();
    
    public static void buildPackets() {
        getRegisteredPackets().put(new Protocol(0, 0, Protocol.State.HANDSHAKE, Protocol.Direction.SERVERBOUND), CPacketHandshake.class);
        getRegisteredPackets().put(new Protocol(0, 0, Protocol.State.STATUS, Protocol.Direction.SERVERBOUND), CPacketServerInfo.class);
        getRegisteredPackets().put(new Protocol(0, 0, Protocol.State.STATUS, Protocol.Direction.CLIENTBOUND), SPacketServerInfo.class);
        getRegisteredPackets().put(new Protocol(1, 0, Protocol.State.STATUS, Protocol.Direction.SERVERBOUND), CPacketPing.class);
        getRegisteredPackets().put(new Protocol(1, 0, Protocol.State.STATUS, Protocol.Direction.CLIENTBOUND), SPacketPong.class);
        getRegisteredPackets().put(new Protocol(0, 0, Protocol.State.LOGIN, Protocol.Direction.CLIENTBOUND), SPacketDisconnect.class);
        getRegisteredPackets().put(new Protocol(0, 0, Protocol.State.LOGIN, Protocol.Direction.SERVERBOUND), CPacketLoginStart.class);
        getRegisteredPackets().put(new Protocol(1, 0, Protocol.State.LOGIN, Protocol.Direction.CLIENTBOUND), SPacketEncryptionRequest.class);
        getRegisteredPackets().put(new Protocol(1, 0, Protocol.State.LOGIN, Protocol.Direction.SERVERBOUND), CPacketEncryptionResponse.class);
        getRegisteredPackets().put(new Protocol(2, 0, Protocol.State.LOGIN, Protocol.Direction.CLIENTBOUND), SPacketLoginSuccess.class);
    }
    
    public static void process(ProxyMessage proxyMessage) {
        try {
            Objects.requireNonNull(proxyMessage);
            if (!proxyMessage.isValid()) {
                throw new IllegalArgumentException("ProxyMessage is invalid");
            }
            
            if (Objects.equals(proxyMessage.getConnection().getState(), Protocol.State.PLAY)) {
                throw new IllegalStateException("Cannot process encrypted packets");
            }
            
            proxyMessage.getByteBuf().markReaderIndex();
            Optional<? extends AbstractPacket> packet = getPacket(proxyMessage.createProtocol(PacketUtil.readVarInt(proxyMessage.getByteBuf())));
            if (!packet.isPresent()) {
                proxyMessage.getByteBuf().resetReaderIndex();
                return;
            }
            
            packet.get().read(proxyMessage);
            proxyMessage.getByteBuf().resetReaderIndex();
        } catch (RuntimeException ex) {
            Equity.getInstance().getLogger().error("Encountered an error processing {}::process", "PacketManager", ex);
        }
    }
    
    private static Optional<? extends AbstractPacket> getPacket(Protocol protocol) {
        try {
            Objects.requireNonNull(protocol, "Protocol cannot be null");
            Class<? extends AbstractPacket> abstractPacket = getRegisteredPackets().get(protocol);
            if (abstractPacket != null) {
                return Optional.of(abstractPacket.newInstance());
            }
            
            if (protocol.getVersion() != 0) {
                return getPacket(new Protocol(protocol.getId(), 0, protocol.getState(), protocol.getDirection()));
            }
            
            return Optional.empty();
        } catch (ExceptionInInitializerError | IllegalAccessException | InstantiationException | RuntimeException ex) {
            Equity.getInstance().getLogger().error("Encountered an error processing {}::getPacket", "PacketManager", ex);
            return Optional.empty();
        }
    }
    
    public static Optional<Integer> getPacketId(Class<? extends AbstractPacket> abstractPacketClass, int version) {
        try {
            Objects.requireNonNull(abstractPacketClass);
            Map<Integer, Integer> packets = Toolbox.newHashMap();
            getRegisteredPackets().forEach((key, value) -> {
                if (value.equals(abstractPacketClass)) {
                    packets.put(key.getVersion(), key.getId());
                }
            });
            
            return Optional.ofNullable(packets.getOrDefault(version, packets.get(0)));
        } catch (RuntimeException ex) {
            Equity.getInstance().getLogger().error("Encountered an error processing {}::getPacketId", "PacketManager", ex);
            return Optional.empty();
        }
    }
    
    private static Map<Protocol, Class<? extends AbstractPacket>> getRegisteredPackets() {
        return REGISTERED_PACKETS;
    }
}