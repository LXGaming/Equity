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
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class KickCommand extends AbstractCommand {
    
    @Override
    public void execute(List<String> arguments) {
        if (arguments.isEmpty()) {
            Equity.getInstance().getLogger().info("Usage: {} {}", getName(), getUsage());
            return;
        }
        
        for (Connection connection : ConnectionManager.getConnections()) {
            if (StringUtils.equals(connection.getUsername(), arguments.get(0))) {
                ConnectionManager.removeConnection(connection);
                Equity.getInstance().getLogger().info("Kicked {}", connection.getUsername());
                return;
            }
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
        return "<Name>";
    }
}