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

package io.github.lxgaming.equity.entries;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.ObjectUtils;

public class ProxyMessage {
    
    private final ByteBuf byteBuf;
    private final Connection connection;
    private Protocol.Direction direction;
    
    public ProxyMessage(ByteBuf byteBuf, Connection connection) {
        this(byteBuf, connection, null);
    }
    
    public ProxyMessage(ByteBuf byteBuf, Connection connection, Protocol.Direction direction) {
        this.byteBuf = byteBuf;
        this.connection = connection;
        this.direction = direction;
    }
    
    public boolean isValid() {
        return ObjectUtils.allNotNull(getByteBuf(), getConnection(), getDirection());
    }
    
    public Protocol createProtocol(int id) {
        return new Protocol(id, getConnection().getVersion(), getConnection().getState(), getDirection());
    }
    
    public ByteBuf getByteBuf() {
        return byteBuf;
    }
    
    public Connection getConnection() {
        return connection;
    }
    
    public Protocol.Direction getDirection() {
        return direction;
    }
    
    public void setDirection(Protocol.Direction direction) {
        this.direction = direction;
    }
}