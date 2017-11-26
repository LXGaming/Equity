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

import io.netty.channel.Channel;
import nz.co.lolnet.equity.util.EquityUtil;
import org.apache.commons.lang3.StringUtils;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Connection {
    
    private final List<Object> packetQueue;
    private Protocol.State state;
    private SocketAddress socketAddress;
    private Channel clientChannel;
    private Channel serverChannel;
    private int version;
    private String username;
    private String server;
    private boolean active;
    
    public Connection() {
        packetQueue = Collections.synchronizedList(EquityUtil.newArrayList());
    }
    
    public Optional<String> getIdentity() {
        if (StringUtils.isNotBlank(getUsername())) {
            return Optional.of(getUsername());
        }
        
        if (getAddress().isPresent()) {
            return Optional.of(EquityUtil.getAddress(getAddress().get()));
        }
        
        return Optional.empty();
    }
    
    public Optional<SocketAddress> getAddress() {
        if (getSocketAddress() != null) {
            return Optional.of(getSocketAddress());
        }
        
        if (getClientChannel() != null && getClientChannel().remoteAddress() != null) {
            return Optional.of(getClientChannel().remoteAddress());
        }
        
        return Optional.empty();
    }
    
    public List<Object> getPacketQueue() {
        return packetQueue;
    }
    
    public Protocol.State getState() {
        return state;
    }
    
    public void setState(Protocol.State state) {
        this.state = state;
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
    }
    
    public Channel getServerChannel() {
        return serverChannel;
    }
    
    public void setServerChannel(Channel serverChannel) {
        this.serverChannel = serverChannel;
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getServer() {
        return server;
    }
    
    public void setServer(String server) {
        this.server = server;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}