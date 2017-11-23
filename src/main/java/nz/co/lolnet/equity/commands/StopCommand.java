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
import nz.co.lolnet.equity.util.EquityUtil;

import java.util.List;

public class StopCommand extends AbstractCommand {
    
    @Override
    public void execute(List<String> arguments) {
        Equity.getInstance().getLogger().warn("Shutting down...");
        Equity.getInstance().setRunning(false);
    }
    
    @Override
    public String getName() {
        return "Stop";
    }
    
    @Override
    public String getDescription() {
        return "Shuts down the Equity instance.";
    }
    
    @Override
    public List<String> getAliases() {
        return EquityUtil.newArrayList("End");
    }
}