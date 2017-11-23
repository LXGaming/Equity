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

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class Protocol {
    
    private final int id;
    private final int version;
    private final Protocol.State state;
    private final Protocol.Direction direction;
    
    public Protocol(int id, int version, Protocol.State state, Protocol.Direction direction) {
        this.id = id;
        this.version = version;
        this.state = state;
        this.direction = direction;
    }
    
    public int getId() {
        return id;
    }
    
    public int getVersion() {
        return version;
    }
    
    public Protocol.State getState() {
        return state;
    }
    
    public Protocol.Direction getDirection() {
        return direction;
    }
    
    @Override
    public boolean equals(Object object) {
        return object instanceof Protocol
                && Objects.equals(getId(), ((Protocol) object).getId())
                && Objects.equals(getVersion(), ((Protocol) object).getVersion())
                && Objects.equals(getState(), ((Protocol) object).getState())
                && Objects.equals(getDirection(), ((Protocol) object).getDirection());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getVersion(), getState(), getDirection());
    }
    
    public enum State {
        
        HANDSHAKE, STATUS, LOGIN, PLAY;
        
        @Override
        public String toString() {
            return StringUtils.capitalize(name().toLowerCase());
        }
    }
    
    public enum Direction {
        
        CLIENTBOUND, SERVERBOUND;
        
        @Override
        public String toString() {
            return StringUtils.capitalize(name().toLowerCase());
        }
    }
}