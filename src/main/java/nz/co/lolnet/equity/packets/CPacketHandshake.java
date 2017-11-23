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

package nz.co.lolnet.equity.packets;

import nz.co.lolnet.equity.entries.Protocol;
import nz.co.lolnet.equity.entries.ProxyMessage;
import nz.co.lolnet.equity.managers.ProxyManager;
import nz.co.lolnet.equity.util.PacketUtil;

public class CPacketHandshake extends AbstractPacket {
    
    private int protocolVersion;
    private String serverAddress;
    private int serverPort;
    private int state;
    
    @Override
    public void read(ProxyMessage proxyMessage) {
        setProtocolVersion(PacketUtil.readVarInt(proxyMessage.getByteBuf()));
        setServerAddress(PacketUtil.readString(proxyMessage.getByteBuf()));
        setServerPort(proxyMessage.getByteBuf().readUnsignedShort());
        setState(PacketUtil.readVarInt(proxyMessage.getByteBuf()));
        proxyMessage.getConnection().setVersion(getProtocolVersion());
        if (getState() == 1) {
            proxyMessage.getConnection().setState(Protocol.State.STATUS);
        } else if (getState() == 2) {
            proxyMessage.getConnection().setState(Protocol.State.LOGIN);
        }
        
        ProxyManager.createServerConnection(proxyMessage.getConnection());
    }
    
    public int getProtocolVersion() {
        return protocolVersion;
    }
    
    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    
    public String getServerAddress() {
        return serverAddress;
    }
    
    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
    
    public int getState() {
        return state;
    }
    
    public void setState(int state) {
        this.state = state;
    }
}