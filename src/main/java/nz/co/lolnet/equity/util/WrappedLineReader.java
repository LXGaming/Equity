/*
 * Copyright 2018 lolnet.co.nz
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

package nz.co.lolnet.equity.util;

import org.jline.reader.Completer;
import org.jline.reader.Expander;
import org.jline.reader.Highlighter;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.Parser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.Map;

public final class WrappedLineReader extends LineReaderImpl {
    
    private WrappedLineReader(Terminal terminal) throws IOException {
        this(terminal, null, null);
    }
    
    private WrappedLineReader(Terminal terminal, String appName) throws IOException {
        this(terminal, appName, null);
    }
    
    private WrappedLineReader(Terminal terminal, String appName, Map<String, Object> variables) {
        super(terminal, appName, variables);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public boolean isReading() {
        return this.reading;
    }
    
    public static final class Builder {
        
        private Terminal terminal;
        private String appName;
        private Map<String, Object> variables = Toolbox.newHashMap();
        private Map<LineReader.Option, Boolean> options = Toolbox.newHashMap();
        private History history;
        private Completer completer;
        private Highlighter highlighter;
        private Parser parser;
        private Expander expander;
        
        public WrappedLineReader build() throws IOException {
            if (getTerminal() == null) {
                terminal(TerminalBuilder.terminal());
            }
            
            WrappedLineReader lineReader = new WrappedLineReader(getTerminal(), getAppName(), getVariables());
            getOptions().forEach(lineReader::option);
            
            if (getHistory() != null) {
                lineReader.setHistory(getHistory());
            }
            
            if (getCompleter() != null) {
                lineReader.setCompleter(getCompleter());
            }
            
            if (getHighlighter() != null) {
                lineReader.setHighlighter(getHighlighter());
            }
            
            if (getParser() != null) {
                lineReader.setParser(getParser());
            }
            
            if (getExpander() != null) {
                lineReader.setExpander(getExpander());
            }
            
            return lineReader;
        }
        
        private Terminal getTerminal() {
            return terminal;
        }
        
        public Builder terminal(Terminal terminal) {
            this.terminal = terminal;
            return this;
        }
        
        private String getAppName() {
            return appName;
        }
        
        public Builder appName(String appName) {
            this.appName = appName;
            return this;
        }
        
        private Map<String, Object> getVariables() {
            return variables;
        }
        
        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }
        
        private Map<LineReader.Option, Boolean> getOptions() {
            return options;
        }
        
        public Builder options(Map<LineReader.Option, Boolean> options) {
            this.options = options;
            return this;
        }
        
        private History getHistory() {
            return history;
        }
        
        public Builder history(History history) {
            this.history = history;
            return this;
        }
        
        private Completer getCompleter() {
            return completer;
        }
        
        public Builder completer(Completer completer) {
            this.completer = completer;
            return this;
        }
        
        private Highlighter getHighlighter() {
            return highlighter;
        }
        
        public Builder highlighter(Highlighter highlighter) {
            this.highlighter = highlighter;
            return this;
        }
        
        private Parser getParser() {
            return parser;
        }
        
        public Builder parser(Parser parser) {
            this.parser = parser;
            return this;
        }
        
        private Expander getExpander() {
            return expander;
        }
        
        public Builder expander(Expander expander) {
            this.expander = expander;
            return this;
        }
    }
}