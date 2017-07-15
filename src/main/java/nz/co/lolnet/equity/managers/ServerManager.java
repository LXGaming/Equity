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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Server;

public class ServerManager {
	
	public Server getServer(int protocolVersion) {
		List<Server> validServers = new ArrayList<Server>();
		for (Iterator<Server> iterator = Equity.getInstance().getConfig().getServers().iterator(); iterator.hasNext();) {
			Server server = iterator.next();
			if (!isValid(server)) {
				continue;
			}
			
			if (server.getProtocolVersions().contains(protocolVersion) && isAvailable(server)) {
				validServers.add(server);
			}
		}
		
		if (!validServers.isEmpty()) {
			return validServers.get(new SecureRandom().nextInt(validServers.size()));
		}
		
		return null;
	}
	
	private boolean isAvailable(Server server) {
		InetSocketAddress socketAddress = new InetSocketAddress(server.getHost(), server.getPort());
		try (Socket socket = new Socket()) {
			socket.connect(socketAddress, server.getTimeout());
			return true;
		} catch (IOException | RuntimeException ex) {
			Equity.getInstance().getLogger().warn("Server {} is not available!", server.getIdentity());
		}
		
		return false;
	}
	
	private boolean isValid(Server server) {
		if (server != null && StringUtils.isNotBlank(server.getHost()) && server.getPort() >= 0 && server.getPort() <= 65535 && server.getProtocolVersions() != null) {
			return true;
		}
		
		return false;
	}
}