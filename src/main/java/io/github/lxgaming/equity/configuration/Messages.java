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

package io.github.lxgaming.equity.configuration;

import io.github.lxgaming.equity.text.Text;
import io.github.lxgaming.equity.util.Toolbox;

public class Messages {
    
    private Text error;
    private Text unavailable;
    private Text unsupported;
    
    public Messages() {
        setError(Toolbox.getTextPrefix().append(Text.of("Error")).build());
        setUnavailable(Toolbox.getTextPrefix().append(Text.of("Unavailable")).build());
        setUnsupported(Toolbox.getTextPrefix().append(Text.of("Unsupported")).build());
    }
    
    public Text getError() {
        return error;
    }
    
    private void setError(Text error) {
        this.error = error;
    }
    
    public Text getUnavailable() {
        return unavailable;
    }
    
    private void setUnavailable(Text unavailable) {
        this.unavailable = unavailable;
    }
    
    public Text getUnsupported() {
        return unsupported;
    }
    
    private void setUnsupported(Text unsupported) {
        this.unsupported = unsupported;
    }
}