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

import nz.co.lolnet.equity.util.Toolbox;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

public class Server {
    
    private String name;
    private String host;
    private int port;
    private int timeout;
    private List<Integer> versions;
    public transient List<String> players = Toolbox.newArrayList();
    
    public Server() {
        setName("localhost");
        setHost("127.0.0.1");
        setPort(0);
        setTimeout(2000);
        setVersions(Toolbox.newArrayList(335, 316, 315, 210, 110, 109, 108, 107, 47));
    }
    
    public Optional<String> getIdentity() {
        if (StringUtils.isNotBlank(getName())) {
            return Optional.of(getName());
        }
        
        if (StringUtils.isNotBlank(getHost())) {
            return Optional.of(getHost() + ":" + getPort());
        }
        
        return Optional.empty();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getHost() {
        return host;
    }
    
    private void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
    
    private void setPort(int port) {
        this.port = port;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    private void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public List<Integer> getVersions() {
        return versions;
    }
    
    private void setVersions(List<Integer> versions) {
        this.versions = versions;
    }
}