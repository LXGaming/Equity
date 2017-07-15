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

package nz.co.lolnet.equity.util;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;

import nz.co.lolnet.equity.Equity;

public class ShutdownHook extends Thread {
	
	@Override
	public void run() {
		Thread.currentThread().setName(Reference.APP_NAME + " Shutdown");
		process();
		LogManager.shutdown();
	}
	
	private void process() {
		Equity.getInstance().getLogger().info("Shutting down...");
		if (Equity.getInstance() == null) {
			Equity.getInstance().getLogger().info("Cannot perform shutdown tasks as {} is null!", Reference.APP_NAME);
			return;
		}
		
		Equity.getInstance().setRunning(false);
		shutdownConnections();
		shutdownProxy();
		Equity.getInstance().getLogger().info("Shutdown complete.");   
	}
	
	private void shutdownConnections() {
		try {
			Equity.getInstance().getLogger().info("Shutting down Connections...");
			if (Equity.getInstance().getConnectionManager() == null) {
				throw new IllegalStateException("ConnectionManager is null!");
			}
			
			if (Equity.getInstance().getConnectionManager().getConnections() == null) {
				throw new IllegalStateException("Connections is null!");
			}
			
			Equity.getInstance().getLogger().info("Closing {} Connections...", Equity.getInstance().getConnectionManager().getConnections().size());
			
			int failed = 0;
			while (Equity.getInstance().getConnectionManager().getConnections().size() > failed) {
				int connectionSize = Equity.getInstance().getConnectionManager().getConnections().size();
				Equity.getInstance().getConnectionManager().removeConnection(Equity.getInstance().getConnectionManager().getConnections().get(failed));
				if (Equity.getInstance().getConnectionManager().getConnections().size() != connectionSize - 1) {
					failed++;
				}
			}
			
			if (failed > 0) {
				Equity.getInstance().getLogger().warn("Failed to remove {} Connections", failed);
			}
			
			Equity.getInstance().getLogger().info("Closed Connections.");
		} catch (RuntimeException ex) {
			Equity.getInstance().getLogger().error("Encountered an error processing {}::shutdownConnections", getClass().getSimpleName(), ex);
		}
	}
	
	private void shutdownProxy() {
		try {
			Equity.getInstance().getLogger().info("Shutting down Proxy...");
			if (Equity.getInstance().getProxyManager() == null) {
				throw new IllegalStateException("ProxyManager is null!");
			}
			
			if (Equity.getInstance().getProxyManager().getServerBootstrap() != null && Equity.getInstance().getProxyManager().getServerBootstrap().config().group() != null) {
				Equity.getInstance().getLogger().info("Closing EventLoopGroup...");
				Equity.getInstance().getProxyManager().getServerBootstrap().config().group().shutdownGracefully();
				Equity.getInstance().getProxyManager().getServerBootstrap().config().group().awaitTermination(Equity.getInstance().getConfig().getShutdownTimeout(), TimeUnit.MILLISECONDS);
				Equity.getInstance().getLogger().info("Closed EventLoopGroup.");
			}
		} catch (InterruptedException | RuntimeException ex) {
			Equity.getInstance().getLogger().error("Encountered an error processing {}::shutdownProxy", getClass().getSimpleName(), ex);
		}
	}
}