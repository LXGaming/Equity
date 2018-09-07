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
import nz.co.lolnet.equity.managers.CommandManager;
import nz.co.lolnet.equity.util.Toolbox;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

public class HelpCommand extends AbstractCommand {
    
    @Override
    public void execute(List<String> arguments) {
        if (!arguments.isEmpty()) {
            Optional<AbstractCommand> command = CommandManager.getCommand(StringUtils.join(arguments, " "));
            if (!command.isPresent()) {
                Equity.getInstance().getLogger().info("No help for {}", StringUtils.join(arguments, " "));
                return;
            }
            
            Equity.getInstance().getLogger().info("========== Help: {} ==========", command.get().getName());
            Equity.getInstance().getLogger().info("Description: {}", command.get().getDescription());
            
            if (StringUtils.isNotBlank(command.get().getUsage())) {
                Equity.getInstance().getLogger().info("Usage: {} {}", command.get().getName(), command.get().getUsage());
            } else {
                Equity.getInstance().getLogger().info("Usage: {}", command.get().getName());
            }
            
            if (command.get().getAliases() != null && !command.get().getAliases().isEmpty()) {
                Equity.getInstance().getLogger().info("Aliases: {}", StringUtils.join(command.get().getAliases(), ", "));
            }
            
            return;
        }
        
        Equity.getInstance().getLogger().info("========== Help: Index ==========");
        CommandManager.getRegisteredCommands().forEach(command -> {
            Equity.getInstance().getLogger().info("{}: {}", command.getName(), command.getDescription());
        });
        
        Equity.getInstance().getLogger().info("<> = Required argument, [] = Optional argument");
    }
    
    @Override
    public String getName() {
        return "Help";
    }
    
    @Override
    public String getDescription() {
        return "Provides help/list of commands.";
    }
    
    @Override
    public String getUsage() {
        return "[Command]";
    }
    
    @Override
    public List<String> getAliases() {
        return Toolbox.newArrayList("?");
    }
}