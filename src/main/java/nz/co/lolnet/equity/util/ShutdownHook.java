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

import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.configuration.Config;
import nz.co.lolnet.equity.managers.ConnectionManager;
import nz.co.lolnet.equity.managers.ProxyManager;
import org.apache.logging.log4j.LogManager;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ShutdownHook extends Thread {
    
    @Override
    public void run() {
        Thread.currentThread().setName(Reference.APP_NAME + " Shutdown");
        process();
        LogManager.shutdown();
    }
    
    private void process() {
        if (Equity.getInstance() == null) {
            System.out.println("Cannot perform shutdown tasks as Equity instance is null!");
            return;
        }
        
        Equity.getInstance().getLogger().info("Shutting down...");
        shutdownConnections();
        shutdownProxy();
        Equity.getInstance().getLogger().info("Shutdown complete.");
    }
    
    private void shutdownConnections() {
        try {
            Equity.getInstance().getLogger().info("Closing {} Connections...", ConnectionManager.getConnections().size());
            
            int failed = 0;
            while (ConnectionManager.getConnections().size() > failed) {
                if (!ConnectionManager.removeConnection(ConnectionManager.getConnections().get(failed))) {
                    failed++;
                }
            }
            
            if (failed > 0) {
                Equity.getInstance().getLogger().warn("Failed to close {} Connections", failed);
            }
            
            Equity.getInstance().getLogger().info("Closed Connections.");
        } catch (RuntimeException ex) {
            Equity.getInstance().getLogger().error("Encountered an error processing {}::shutdownConnections", getClass().getSimpleName(), ex);
        }
    }
    
    private void shutdownProxy() {
        try {
            Objects.requireNonNull(ProxyManager.getEventLoopGroup(), "EventLoopGroup cannot be null");
            Equity.getInstance().getLogger().info("Closing EventLoopGroup...");
            ProxyManager.getEventLoopGroup().shutdownGracefully();
            ProxyManager.getEventLoopGroup().awaitTermination(Equity.getInstance().getConfig().map(Config::getShutdownTimeout).orElse(5000), TimeUnit.MILLISECONDS);
            Equity.getInstance().getLogger().info("Closed EventLoopGroup.");
        } catch (InterruptedException | RuntimeException ex) {
            Equity.getInstance().getLogger().error("Encountered an error processing {}::shutdownProxy", getClass().getSimpleName(), ex);
        }
    }
}