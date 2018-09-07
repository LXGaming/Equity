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

package nz.co.lolnet.equity.managers;

import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.commands.AbstractCommand;
import nz.co.lolnet.equity.commands.ConnectionCommand;
import nz.co.lolnet.equity.commands.HelpCommand;
import nz.co.lolnet.equity.commands.InfoCommand;
import nz.co.lolnet.equity.commands.KickCommand;
import nz.co.lolnet.equity.commands.ListCommand;
import nz.co.lolnet.equity.commands.ReloadCommand;
import nz.co.lolnet.equity.commands.StopCommand;
import nz.co.lolnet.equity.util.Toolbox;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

public class CommandManager {
    
    private static final List<AbstractCommand> REGISTERED_COMMANDS = Toolbox.newArrayList();
    
    public static void buildCommands() {
        getRegisteredCommands().add(new ConnectionCommand());
        getRegisteredCommands().add(new HelpCommand());
        getRegisteredCommands().add(new InfoCommand());
        getRegisteredCommands().add(new KickCommand());
        getRegisteredCommands().add(new ListCommand());
        getRegisteredCommands().add(new ReloadCommand());
        getRegisteredCommands().add(new StopCommand());
    }
    
    public static void process(String message) {
        try {
            Optional<List<String>> arguments = getArguments(message);
            if (!arguments.isPresent() || arguments.get().isEmpty()) {
                return;
            }
            
            Optional<AbstractCommand> command = getCommand(arguments.get().remove(0));
            if (!command.isPresent()) {
                Equity.getInstance().getLogger().info("Command not found");
                return;
            }
            
            Equity.getInstance().getLogger().info("Processing {}", command.get().getName());
            command.get().execute(arguments.get());
        } catch (Exception ex) {
            Equity.getInstance().getLogger().error("Encountered an error processing {}::process", "CommandManager", ex);
        }
    }
    
    public static Optional<AbstractCommand> getCommand(String argument) {
        for (AbstractCommand command : getRegisteredCommands()) {
            if (checkCommandName(argument, command.getName()) || checkCommandAliases(argument, command.getAliases())) {
                return Optional.of(command);
            }
        }
        
        return Optional.empty();
    }
    
    private static Optional<List<String>> getArguments(String message) {
        if (StringUtils.isBlank(message)) {
            return Optional.empty();
        }
        
        if (StringUtils.startsWith(message, "/")) {
            return Optional.of(Toolbox.newArrayList(StringUtils.split(Toolbox.filter(StringUtils.substringAfter(message, "/")), " ")));
        }
        
        return Optional.of(Toolbox.newArrayList(StringUtils.split(Toolbox.filter(message), " ")));
    }
    
    private static boolean checkCommandName(String target, String name) {
        return StringUtils.isNoneBlank(target, name) && StringUtils.equalsIgnoreCase(target, name);
    }
    
    private static boolean checkCommandAliases(String target, List<String> aliases) {
        if (StringUtils.isBlank(target) || aliases == null || aliases.isEmpty()) {
            return false;
        }
        
        for (String alias : aliases) {
            if (StringUtils.equalsIgnoreCase(target, alias)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static List<AbstractCommand> getRegisteredCommands() {
        return REGISTERED_COMMANDS;
    }
}