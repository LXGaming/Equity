/*
 * Copyright 2017 Alex Thomson
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

package io.github.lxgaming.equity.commands;

import io.github.lxgaming.equity.Equity;
import io.github.lxgaming.equity.entries.Connection;
import io.github.lxgaming.equity.managers.ConnectionManager;

import java.util.List;
import java.util.Optional;

public class KickCommand extends AbstractCommand {
    
    @Override
    public void execute(List<String> arguments) {
        if (arguments.isEmpty()) {
            Equity.getInstance().getLogger().info("Usage: {} {}", getName(), getUsage());
            return;
        }
        
        Optional<Connection> connection = ConnectionManager.getConnection(arguments.get(0));
        if (connection.isPresent() && connection.get().getIdentity().isPresent()) {
            ConnectionManager.removeConnection(connection.get());
            Equity.getInstance().getLogger().info("Kicked {}", connection.get().getIdentity().get());
            return;
        }
        
        Equity.getInstance().getLogger().info("Connection '{}' cannot be found", arguments.get(0));
    }
    
    @Override
    public String getName() {
        return "Kick";
    }
    
    @Override
    public String getDescription() {
        return "Forcibly disconnects a connection.";
    }
    
    @Override
    public String getUsage() {
        return "<Identity>";
    }
}