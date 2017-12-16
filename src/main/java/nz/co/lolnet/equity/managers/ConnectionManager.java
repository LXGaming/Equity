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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.configuration.Messages;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Protocol;
import nz.co.lolnet.equity.entries.ProxyMessage;
import nz.co.lolnet.equity.entries.ServerMessage;
import nz.co.lolnet.equity.packets.SPacketDisconnect;
import nz.co.lolnet.equity.packets.SPacketServerInfo;
import nz.co.lolnet.equity.text.Text;
import nz.co.lolnet.equity.util.EquityUtil;
import nz.co.lolnet.equity.util.PacketUtil;
import org.apache.commons.lang3.StringUtils;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ConnectionManager {
    
    private static final List<Connection> CONNECTIONS = Collections.synchronizedList(EquityUtil.newArrayList());
    
    public static void addConnection(Connection connection) throws NullPointerException {
        Objects.requireNonNull(connection, "Connection cannot be null");
        getConnections().add(connection);
        connection.getIdentity().ifPresent(identity -> {
            Equity.getInstance().getLogger().info("{} -> Connected", identity);
        });
    }
    
    public static void addPacketQueue(Connection connection, Object object) throws NullPointerException {
        Objects.requireNonNull(connection, "Connection cannot be null");
        Objects.requireNonNull(object, "Object cannot be null");
        if (connection.getPacketQueue().size() >= 5) {
            connection.getIdentity().ifPresent(identity -> {
                Equity.getInstance().getLogger().warn("{} -> Attempted to queue over 5 packets, Assuming malicious client!", identity);
            });
            
            disconnect(connection, Equity.getInstance().getMessages().map(Messages::getError).orElse(null));
            return;
        }
        
        connection.getPacketQueue().add(object);
    }
    
    public static void setSocketAddress(Connection connection, SocketAddress socketAddress) throws NullPointerException {
        Objects.requireNonNull(connection, "Connection cannot be null");
        Objects.requireNonNull(socketAddress, "SocketAddress cannot be null");
        connection.setSocketAddress(socketAddress);
        connection.getAddress().ifPresent(address -> {
            Equity.getInstance().getLogger().info("{} -> PROXY {}", EquityUtil.getAddress(connection.getClientChannel().remoteAddress()), EquityUtil.getAddress(address));
        });
    }
    
    public static boolean disconnect(Connection connection, Text description) throws NullPointerException {
        Objects.requireNonNull(connection, "Connection cannot be null");
        if (description == null || connection.getClientChannel() == null) {
            return removeConnection(connection);
        }
        
        ServerMessage serverMessage = new ServerMessage();
        serverMessage.getVersion().setProtocol(connection.getVersion());
        serverMessage.setDescription(description);
        
        if (Objects.equals(connection.getState(), Protocol.State.STATUS)) {
            SPacketServerInfo serverInfo = new SPacketServerInfo();
            serverInfo.setServerPing(serverMessage);
            serverInfo.write(new ProxyMessage(Unpooled.buffer(), connection, Protocol.Direction.CLIENTBOUND));
        }
        
        if (Objects.equals(connection.getState(), Protocol.State.LOGIN)) {
            SPacketDisconnect disconnect = new SPacketDisconnect();
            disconnect.setReason(serverMessage.getDescription());
            disconnect.write(new ProxyMessage(Unpooled.buffer(), connection, Protocol.Direction.CLIENTBOUND));
        }
        
        return removeConnection(connection);
    }
    
    public static boolean removeConnection(Connection connection) throws NullPointerException {
        Objects.requireNonNull(connection, "Connection cannot be null");
        if (!getConnections().contains(connection) || !getConnections().remove(connection)) {
            return false;
        }
        
        connection.setActive(false);
        closeChannel(connection.getClientChannel());
        closeChannel(connection.getServerChannel());
        clearPacketQueue(connection);
        connection.getIdentity().ifPresent(identity -> {
            Equity.getInstance().getLogger().info("{} -> Disconnected", identity);
        });
        
        return true;
    }
    
    private static void clearPacketQueue(Connection connection) throws NullPointerException {
        Objects.requireNonNull(connection, "Connection cannot be null");
        connection.getPacketQueue().forEach(object -> {
            if (object instanceof ByteBuf) {
                PacketUtil.safeRelease(((ByteBuf) object));
            }
            
            if (object instanceof ProxyMessage) {
                PacketUtil.safeRelease(((ProxyMessage) object).getByteBuf());
            }
        });
        
        connection.getPacketQueue().clear();
    }
    
    private static void closeChannel(Channel channel) {
        if (channel == null) {
            return;
        }
        
        channel.config().setAutoRead(false);
        if (channel.hasAttr(EquityUtil.getConnectionKey())) {
            channel.attr(EquityUtil.getConnectionKey()).set(null);
        }
        
        if (channel.hasAttr(EquityUtil.getSideKey())) {
            channel.attr(EquityUtil.getSideKey()).set(null);
        }
        
        channel.close();
    }
    
    public static Optional<Connection> getConnection(String identity) {
        synchronized (getConnections()) {
            return getConnections().stream().filter(connection -> connection.getIdentity().isPresent() && StringUtils.equals(connection.getIdentity().get(), identity)).findFirst();
        }
    }
    
    public static List<Connection> getConnections() {
        return CONNECTIONS;
    }
}