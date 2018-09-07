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

package nz.co.lolnet.equity.configuration;

import nz.co.lolnet.equity.entries.Server;
import nz.co.lolnet.equity.util.Toolbox;

import java.util.List;

public class Config {
    
    private boolean debug;
    private int port;
    private boolean jlineOverride;
    private boolean nativeTransport;
    private boolean proxyProtocol;
    private boolean ipForward;
    private int maxThreads;
    private int connectTimeout;
    private int readTimeout;
    private int shutdownTimeout;
    private List<Server> servers;
    
    public Config() {
        setDebug(false);
        setPort(25565);
        setJlineOverride(true);
        setNativeTransport(true);
        setProxyProtocol(false);
        setIpForward(false);
        setMaxThreads(0);
        setConnectTimeout(5000);
        setReadTimeout(30000);
        setShutdownTimeout(5000);
        setServers(Toolbox.newArrayList(new Server()));
    }
    
    public boolean isDebug() {
        return debug;
    }
    
    private void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    public int getPort() {
        return port;
    }
    
    private void setPort(int port) {
        this.port = port;
    }
    
    public boolean isJlineOverride() {
        return jlineOverride;
    }
    
    private void setJlineOverride(boolean jlineOverride) {
        this.jlineOverride = jlineOverride;
    }
    
    public boolean isNativeTransport() {
        return nativeTransport;
    }
    
    private void setNativeTransport(boolean nativeTransport) {
        this.nativeTransport = nativeTransport;
    }
    
    public boolean isProxyProtocol() {
        return proxyProtocol;
    }
    
    private void setProxyProtocol(boolean proxyProtocol) {
        this.proxyProtocol = proxyProtocol;
    }
    
    public boolean isIpForward() {
        return ipForward;
    }
    
    private void setIpForward(boolean ipForward) {
        this.ipForward = ipForward;
    }
    
    public int getMaxThreads() {
        return maxThreads;
    }
    
    private void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    private void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    private void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public int getShutdownTimeout() {
        return shutdownTimeout;
    }
    
    private void setShutdownTimeout(int shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }
    
    public List<Server> getServers() {
        return servers;
    }
    
    private void setServers(List<Server> servers) {
        this.servers = servers;
    }
}