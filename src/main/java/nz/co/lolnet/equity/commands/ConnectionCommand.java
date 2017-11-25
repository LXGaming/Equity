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

package nz.co.lolnet.equity.commands;

import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.managers.ConnectionManager;

import java.util.List;
import java.util.Optional;

public class ConnectionCommand extends AbstractCommand {
    
    @Override
    public void execute(List<String> arguments) {
        if (arguments.isEmpty()) {
            Equity.getInstance().getLogger().info("Usage: {} {}", getName(), getUsage());
            return;
        }
        
        Optional<Connection> connection = ConnectionManager.getConnection(arguments.get(0));
        if (connection.isPresent() && connection.get().getIdentity().isPresent()) {
            Equity.getInstance().getLogger().info("===== Connection: {} =====", connection.get().getIdentity().get());
            Equity.getInstance().getLogger().info("PacketQueue: {}", connection.get().getPacketQueue().size());
            Equity.getInstance().getLogger().info("State: {}", connection.get().getState());
            Equity.getInstance().getLogger().info("Client Channel: {}", connection.get().getClientChannel() != null);
            Equity.getInstance().getLogger().info("Server Channel: {}", connection.get().getServerChannel() != null);
            Equity.getInstance().getLogger().info("Version: {}", connection.get().getVersion());
            Equity.getInstance().getLogger().info("Username: {}", connection.get().getUsername());
            Equity.getInstance().getLogger().info("Server: {}", connection.get().getServer());
            Equity.getInstance().getLogger().info("Active: {}", connection.get().isActive());
            return;
        }
        
        Equity.getInstance().getLogger().info("Connection '{}' cannot be found", arguments.get(0));
    }
    
    @Override
    public String getName() {
        return "Connection";
    }
    
    @Override
    public String getDescription() {
        return "Shows Connection information";
    }
    
    @Override
    public String getUsage() {
        return "<Identity>";
    }
}