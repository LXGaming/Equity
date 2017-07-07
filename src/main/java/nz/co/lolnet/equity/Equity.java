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

package nz.co.lolnet.equity;

import nz.co.lolnet.equity.configuration.Configuration;
import nz.co.lolnet.equity.entries.Config;
import nz.co.lolnet.equity.managers.ConnectionManager;
import nz.co.lolnet.equity.managers.PacketManager;
import nz.co.lolnet.equity.managers.ProxyManager;
import nz.co.lolnet.equity.managers.ServerManager;
import nz.co.lolnet.equity.util.EquityUtil;
import nz.co.lolnet.equity.util.LogHelper;
import nz.co.lolnet.equity.util.Reference;
import nz.co.lolnet.equity.util.ShutdownHook;

public class Equity {
	
	private static Equity instance;
	private Configuration configuration;
	private ConnectionManager connectionManager;
	private PacketManager packetManager;
	private ProxyManager proxyManager;
	private ServerManager serverManager;
	
	public Equity() {
		instance = this;
		connectionManager = new ConnectionManager();
		packetManager = new PacketManager();
		proxyManager = new ProxyManager();
		serverManager = new ServerManager();
	}
	
	public void loadEquity() {
		EquityUtil.getApplicationInformation().forEach(action -> {
			LogHelper.info(action);
		});
		
		LogHelper.info("Initializing...");
		if (getConfiguration() == null || !getConfiguration().loadConfiguration()) {
			LogHelper.error("Unable to load " + Reference.APP_NAME + " as the Configurations are not available!");
		}
		
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		getPacketManager().registerPackets();
		getProxyManager().startProxy();
	}
	
	public static Equity getInstance() {
		return instance;
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public Config getConfig() {
		if (getConfiguration() != null) {
			return getConfiguration().getConfig();
		}
		return null;
	}
	
	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}
	
	public PacketManager getPacketManager() {
		return packetManager;
	}
	
	public ProxyManager getProxyManager() {
		return proxyManager;
	}
	
	public ServerManager getServerManager() {
		return serverManager;
	}
}