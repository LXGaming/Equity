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

import nz.co.lolnet.equity.Equity;

public class ShutdownHook extends Thread {
	
	@Override
	public void run() {
		Thread.currentThread().setName(Reference.APP_NAME + " Shutdown");
		process();
	}
	
	private void process() {
		LogHelper.info("Shutting down...");
		if (Equity.getInstance() != null) {
			shutdownProxy();
		}
		
		LogHelper.info("Shutdown complete.");
	}
	
	private void shutdownProxy() {
		try {
			if (Equity.getInstance().getConnectionManager() == null || Equity.getInstance().getProxyManager() == null) {
				throw new IllegalStateException("ConnectionManager or ProxyManager is null!");
			}
			
			Equity.getInstance().getProxyManager().setRunning(false);
			if (Equity.getInstance().getConnectionManager().getConnections() != null) {
				LogHelper.info("Closing " + Equity.getInstance().getConnectionManager().getConnections().size() + " Connections...");
				for (int index = 0; index < Equity.getInstance().getConnectionManager().getConnections().size(); index++) {
					Equity.getInstance().getConnectionManager().removeConnection(Equity.getInstance().getConnectionManager().getConnections().get(0));
				}
				
				LogHelper.info("Closed Connections.");
			}
			
			if (Equity.getInstance().getProxyManager().getServerBootstrap() != null && Equity.getInstance().getProxyManager().getServerBootstrap().config().group() != null) {
				LogHelper.info("Closing EventLoopGroup...");
				Equity.getInstance().getProxyManager().getServerBootstrap().config().group().shutdownGracefully();
				Equity.getInstance().getProxyManager().getServerBootstrap().config().group().awaitTermination(Equity.getInstance().getConfig().getShutdownTimeout(), TimeUnit.MILLISECONDS);
				LogHelper.info("Closed EventLoopGroup.");
			}
			
		} catch (InterruptedException | RuntimeException ex) {
			LogHelper.error("Encountered an error processing 'shutdownProxy' in '" + getClass().getSimpleName() + "' - " + ex.getMessage());
			ex.printStackTrace();
		}
	}
}