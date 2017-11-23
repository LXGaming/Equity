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

package nz.co.lolnet.equity.util;

import nz.co.lolnet.equity.Equity;
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
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

@Plugin(name = TerminalConsoleAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class TerminalConsoleAppender extends AbstractAppender {
    
    public static final String PLUGIN_NAME = "TerminalConsole";
    private static final PrintStream PRINT_STREAM = System.out;
    private static boolean built;
    private static Field reading;
    private static Terminal terminal;
    private static LineReader lineReader;
    
    protected TerminalConsoleAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoredExceptions) {
        super(name, filter, layout, ignoredExceptions);
    }
    
    @Override
    public void append(LogEvent event) {
        if (getTerminal() == null) {
            PRINT_STREAM.print(getLayout().toSerializable(event));
            return;
        }
        
        if (getLineReader() != null && isReading()) {
            getLineReader().callWidget(LineReader.CLEAR);
            getTerminal().writer().print(getLayout().toSerializable(event));
            getLineReader().callWidget(LineReader.REDRAW_LINE);
            getLineReader().callWidget(LineReader.REDISPLAY);
        } else {
            getTerminal().writer().print(getLayout().toSerializable(event));
        }
        
        getTerminal().writer().flush();
    }
    
    public static void buildTerminal(String appName, boolean jlineOverride) throws IllegalStateException {
        if (isBuilt()) {
            throw new IllegalStateException("Terminal is already built");
        }
        
        setBuilt(true);
        
        try {
            setReading(LineReaderImpl.class.getDeclaredField("reading"));
            getReading().setAccessible(true);
            boolean dumb = jlineOverride || System.getProperty("java.class.path").contains("idea_rt.jar");
            setTerminal(TerminalBuilder.builder().dumb(dumb).build());
            setLineReader(LineReaderBuilder.builder().appName(appName).terminal(getTerminal()).build());
        } catch (IllegalStateException ex) {
            if (Equity.getInstance().getLogger().isDebugEnabled()) {
                Equity.getInstance().getLogger().warn("Disabling terminal, you're running in an unsupported environment.", ex);
            } else {
                Equity.getInstance().getLogger().warn("Disabling terminal, you're running in an unsupported environment.");
            }
        } catch (IOException | NoSuchFieldException | RuntimeException ex) {
            Equity.getInstance().getLogger().error("Failed to initialize terminal. Falling back to standard output", ex);
        }
    }
    
    public static Optional<String> readline() {
        try {
            return Optional.ofNullable(getLineReader().readLine(">"));
        } catch (EndOfFileException | UserInterruptException ex) {
            return Optional.empty();
        }
    }
    
    public static void close() throws IOException {
        if (!isBuilt() || getTerminal() == null) {
            return;
        }
        
        setBuilt(false);
        getTerminal().close();
    }
    
    private boolean isReading() {
        try {
            Objects.requireNonNull(getReading());
            Objects.requireNonNull(getLineReader());
            return (boolean) getReading().get(getLineReader());
        } catch (ExceptionInInitializerError | IllegalAccessException | RuntimeException ex) {
            Equity.getInstance().getLogger().error("Encountered an error processing {}::isReading", getClass().getSimpleName(), ex);
            return false;
        }
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
    
    private static boolean isBuilt() {
        return built;
    }
    
    private static void setBuilt(boolean built) {
        TerminalConsoleAppender.built = built;
    }
    
    private static Field getReading() {
        return reading;
    }
    
    private static void setReading(Field reading) {
        TerminalConsoleAppender.reading = reading;
    }
    
    private static Terminal getTerminal() {
        return terminal;
    }
    
    private static void setTerminal(Terminal terminal) {
        TerminalConsoleAppender.terminal = terminal;
    }
    
    private static LineReader getLineReader() {
        return lineReader;
    }
    
    private static void setLineReader(LineReader lineReader) {
        TerminalConsoleAppender.lineReader = lineReader;
    }
}