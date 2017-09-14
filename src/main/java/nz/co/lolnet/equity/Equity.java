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

import java.nio.file.Paths;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import io.netty.util.ResourceLeakDetector;
import nz.co.lolnet.equity.configuration.Configuration;
import nz.co.lolnet.equity.entries.Config;
import nz.co.lolnet.equity.entries.Messages;
import nz.co.lolnet.equity.managers.ConnectionManager;
import nz.co.lolnet.equity.managers.PacketManager;
import nz.co.lolnet.equity.managers.ProxyManager;
import nz.co.lolnet.equity.util.EquityUtil;
import nz.co.lolnet.equity.util.Reference;
import nz.co.lolnet.equity.util.ShutdownHook;

public class Equity {
	
	private static Equity instance;
	private boolean running;
	private final Logger logger;
	private final Configuration configuration;
	private final ConnectionManager connectionManager;
	private final PacketManager packetManager;
	private final ProxyManager proxyManager;
	
	public Equity() {
		instance = this;
		running = false;
		logger = LogManager.getLogger(Reference.APP_ID);
		configuration = new Configuration(Paths.get(System.getProperty("user.dir")));
		connectionManager = new ConnectionManager();
		packetManager = new PacketManager();
		proxyManager = new ProxyManager();
	}
	
	public void loadEquity() {
		EquityUtil.getApplicationInformation().forEach(action -> {
			Equity.getInstance().getLogger().info(action);
		});
		
		Equity.getInstance().getLogger().info("Initializing...");
		getConfiguration().loadConfiguration();
		getConfiguration().saveConfiguration();
		
		System.setProperty("java.net.preferIPv4Stack", "true");
		if (getConfig().isDebug()) {
			ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
			Configurator.setLevel(getLogger().getName(), Level.DEBUG);
			Equity.getInstance().getLogger().debug("Debugging enabled.");
		} else {
			ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
			Configurator.setLevel(getLogger().getName(), Level.INFO);
		}
		
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		getPacketManager().registerPackets();
		getProxyManager().startProxy();
		Equity.getInstance().setRunning(false);
	}
	
	public static Equity getInstance() {
		return instance;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	public Config getConfig() {
		if (getConfiguration() != null) {
			return getConfiguration().getConfig();
		}
		
		return null;
	}
	
	public Messages getMessages() {
		if (getConfiguration() != null) {
			return getConfiguration().getMessages();
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
}