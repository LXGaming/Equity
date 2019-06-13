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

package io.github.lxgaming.equity.util;

import io.github.lxgaming.equity.Equity;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Optional;

@Plugin(name = TerminalConsoleAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class TerminalConsoleAppender extends AbstractAppender {
    
    public static final String PLUGIN_NAME = "TerminalConsole";
    private static final PrintStream PRINT_STREAM = System.out;
    private static Terminal terminal;
    private static WrappedLineReader lineReader;
    
    protected TerminalConsoleAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoredExceptions) {
        super(name, filter, layout, ignoredExceptions);
    }
    
    @Override
    public void append(LogEvent event) {
        if (terminal == null) {
            PRINT_STREAM.print(getLayout().toSerializable(event));
            return;
        }
        
        if (lineReader != null && lineReader.isReading()) {
            lineReader.callWidget(LineReader.CLEAR);
            terminal.writer().print(getLayout().toSerializable(event));
            lineReader.callWidget(LineReader.REDRAW_LINE);
            lineReader.callWidget(LineReader.REDISPLAY);
        } else {
            terminal.writer().print(getLayout().toSerializable(event));
        }
        
        terminal.writer().flush();
    }
    
    public static void buildTerminal(String appName, boolean jlineOverride) throws IllegalStateException {
        if (terminal != null && lineReader != null) {
            throw new IllegalStateException("Terminal is already built");
        }
        
        try {
            boolean dumb = jlineOverride || System.getProperty("java.class.path").contains("idea_rt.jar");
            terminal = TerminalBuilder.builder().dumb(dumb).build();
            lineReader = WrappedLineReader.builder().appName(appName).terminal(terminal).build();
        } catch (IllegalStateException ex) {
            Equity.getInstance().getLogger().warn("Disabling terminal, you're running in an unsupported environment.");
        } catch (IOException | RuntimeException ex) {
            Equity.getInstance().getLogger().error("Failed to initialize terminal. Falling back to standard output", ex);
        }
    }
    
    public static Optional<String> readline() {
        try {
            return Optional.ofNullable(lineReader.readLine(">"));
        } catch (EndOfFileException | UserInterruptException ex) {
            return Optional.empty();
        }
    }
    
    public static void close() throws IOException {
        if (terminal == null || lineReader == null) {
            return;
        }
        
        terminal.close();
        terminal = null;
        lineReader = null;
    }
    
    @PluginFactory
    public static TerminalConsoleAppender createAppender(
            @Required(message = "No name provided for TerminalConsoleAppender") @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute(value = "ignoreExceptions", defaultBoolean = true) boolean ignoreExceptions) {
        
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        
        return new TerminalConsoleAppender(name, filter, layout, ignoreExceptions);
    }
}