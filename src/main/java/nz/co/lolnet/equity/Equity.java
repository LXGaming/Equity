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

import io.netty.util.ResourceLeakDetector;
import nz.co.lolnet.equity.configuration.Config;
import nz.co.lolnet.equity.configuration.Configuration;
import nz.co.lolnet.equity.configuration.Messages;
import nz.co.lolnet.equity.managers.CommandManager;
import nz.co.lolnet.equity.managers.PacketManager;
import nz.co.lolnet.equity.managers.ProxyManager;
import nz.co.lolnet.equity.util.EquityUtil;
import nz.co.lolnet.equity.util.Reference;
import nz.co.lolnet.equity.util.ShutdownHook;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

public class Equity {
    
    private static Equity instance;
    private volatile boolean running;
    private final Instant startTime;
    private final Logger logger;
    private final Path path;
    private final Configuration configuration;
    
    public Equity() {
        instance = this;
        startTime = Instant.now();
        logger = LogManager.getLogger(Reference.APP_ID);
        path = EquityUtil.getPath().orElse(null);
        configuration = new Configuration();
    }
    
    public void loadEquity() {
        getLogger().info("Initializing...");
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        getConfiguration().loadConfiguration();
        reloadLogger();
        CommandManager.buildCommands();
        PacketManager.buildPackets();
        ProxyManager.buildProxy();
        getConfiguration().saveConfiguration();
        setRunning(true);
        getLogger().info("{} v{} has loaded", Reference.APP_NAME, Reference.APP_VERSION);
    }
    
    public void reloadLogger() {
        if (getConfig().map(Config::isDebug).orElse(false)) {
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
            Configurator.setLevel(getLogger().getName(), Level.DEBUG);
            getLogger().debug("Debug mode enabled.");
        } else {
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
            Configurator.setLevel(getLogger().getName(), Level.INFO);
            getLogger().info("Debug mode disabled.");
        }
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
    
    public Instant getStartTime() {
        return startTime;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public Path getPath() {
        return path;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public Optional<Config> getConfig() {
        if (getConfiguration() != null) {
            return Optional.ofNullable(getConfiguration().getConfig());
        }
        
        return Optional.empty();
    }
    
    public Optional<Messages> getMessages() {
        if (getConfiguration() != null) {
            return Optional.ofNullable(getConfiguration().getMessages());
        }
        
        return Optional.empty();
    }
}