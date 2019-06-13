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

import java.util.List;

public class ReloadCommand extends AbstractCommand {
    
    @Override
    public void execute(List<String> arguments) {
        Equity.getInstance().getConfiguration().loadConfiguration();
        Equity.getInstance().reloadLogger();
        Equity.getInstance().getLogger().info("Reloaded");
    }
    
    @Override
    public String getName() {
        return "Reload";
    }
    
    @Override
    public String getDescription() {
        return "Reloads the Equity configuration.";
    }
}