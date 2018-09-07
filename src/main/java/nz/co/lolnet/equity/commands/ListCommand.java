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
import nz.co.lolnet.equity.managers.ConnectionManager;
import nz.co.lolnet.equity.util.Toolbox;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class ListCommand extends AbstractCommand {
    
    @Override
    public void execute(List<String> arguments) {
        Map<String, List<String>> servers = Toolbox.newHashMap();
        ConnectionManager.getConnections().forEach(connection -> {
            if (!connection.getIdentity().isPresent()) {
                return;
            }
            
            servers.putIfAbsent(connection.getServer(), Toolbox.newArrayList());
            servers.get(connection.getServer()).add(connection.getIdentity().get());
        });
        
        servers.forEach((key, value) -> Equity.getInstance().getLogger().info("[{}] ({}): {}", key, value.size(), StringUtils.join(value, ", ")));
        if (servers.size() == 1) {
            Equity.getInstance().getLogger().info("{} active connection", ConnectionManager.getConnections().size());
        } else {
            Equity.getInstance().getLogger().info("{} active connections", ConnectionManager.getConnections().size());
        }
    }
    
    @Override
    public String getName() {
        return "List";
    }
    
    @Override
    public String getDescription() {
        return "Show a list of connections.";
    }
}