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

import com.google.gson.Gson;
import io.github.lxgaming.equity.text.Text;
import io.github.lxgaming.equity.util.Reference;

import java.util.List;
import java.util.UUID;

public class ServerMessage {
    
    private Version version;
    private Players players;
    private Text description;
    
    public ServerMessage() {
        setVersion(new Version());
        setPlayers(new Players());
        setDescription(Text.builder().build());
    }
    
    public Version getVersion() {
        return version;
    }
    
    public ServerMessage setVersion(Version version) {
        this.version = version;
        return this;
    }
    
    public Players getPlayers() {
        return players;
    }
    
    public ServerMessage setPlayers(Players players) {
        this.players = players;
        return this;
    }
    
    public Text getDescription() {
        return description;
    }
    
    public ServerMessage setDescription(Text description) {
        this.description = description;
        return this;
    }
    
    @Override
    public String toString() {
        return new Gson().toJson(this, getClass());
    }
    
    public static class Version {
        
        private String name;
        private int protocol;
        
        public Version() {
            setName(Reference.APP_NAME + " v" + Reference.APP_VERSION);
            setProtocol(0);
        }
        
        public String getName() {
            return name;
        }
        
        public Version setName(String name) {
            this.name = name;
            return this;
        }
        
        public int getProtocol() {
            return protocol;
        }
        
        public Version setProtocol(int protocol) {
            this.protocol = protocol;
            return this;
        }
        
        @Override
        public String toString() {
            return new Gson().toJson(this, getClass());
        }
    }
    
    public static class Players {
        
        private int max;
        private int online;
        private List<PlayerInfo> sample;
        
        public Players() {
            setMax(0);
            setOnline(0);
        }
        
        public int getMax() {
            return max;
        }
        
        public Players setMax(int max) {
            this.max = max;
            return this;
        }
        
        public int getOnline() {
            return online;
        }
        
        public Players setOnline(int online) {
            this.online = online;
            return this;
        }
        
        public List<PlayerInfo> getSample() {
            return sample;
        }
        
        public Players setSample(List<PlayerInfo> sample) {
            this.sample = sample;
            return this;
        }
        
        @Override
        public String toString() {
            return new Gson().toJson(this, getClass());
        }
    }
    
    public static class PlayerInfo {
        
        private String name;
        private UUID uniqueId;
        
        public PlayerInfo() {
            setName("Unknown");
            setUniqueId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        }
        
        public String getName() {
            return name;
        }
        
        public PlayerInfo setName(String name) {
            this.name = name;
            return this;
        }
        
        public UUID getUniqueId() {
            return uniqueId;
        }
        
        public PlayerInfo setUniqueId(UUID uniqueId) {
            this.uniqueId = uniqueId;
            return this;
        }
        
        @Override
        public String toString() {
            return new Gson().toJson(this, getClass());
        }
    }
}