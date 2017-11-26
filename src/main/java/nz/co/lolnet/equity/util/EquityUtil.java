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

import io.netty.channel.WriteBufferWaterMark;
import io.netty.util.AttributeKey;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.text.Text;
import nz.co.lolnet.equity.text.format.TextColors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EquityUtil {
    
    public static Text.Builder getTextPrefix() {
        return Text.builder().append(Text.of("[" + Reference.APP_NAME + "] ").color(TextColors.BLUE).bold(true).build());
    }
    
    public static AttributeKey<Connection> getConnectionKey() {
        return AttributeKey.valueOf("proxy_connection");
    }
    
    public static AttributeKey<String> getSideKey() {
        return AttributeKey.valueOf("proxy_side");
    }
    
    public static String getAddress(SocketAddress socketAddress) {
        return getHost(socketAddress).orElse("Unknown") + ":" + getPort(socketAddress).orElse(0);
    }
    
    public static Optional<String> getHost(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            return Optional.of(inetSocketAddress.getHostString());
        }
        
        return Optional.empty();
    }
    
    public static Optional<Integer> getPort(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress) {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            return Optional.of(inetSocketAddress.getPort());
        }
        
        return Optional.empty();
    }
    
    /**
     * Removes non-printable characters (excluding new line and carriage return) in the provided {@link java.lang.String String}.
     *
     * @param string The {@link java.lang.String String} to filter.
     * @return The filtered {@link java.lang.String String}.
     */
    public static String filter(String string) {
        return StringUtils.replaceAll(string, "[^\\x20-\\x7E\\x0A\\x0D]", "");
    }
    
    public static String getTimeStringFromSeconds(long time) {
        time = Math.abs(time);
        long second = time % 60;
        long minute = (time / 60) % 60;
        long hour = (time / 3600) % 24;
        long day = time / 86400;
        
        StringBuilder stringBuilder = new StringBuilder();
        appendUnit(stringBuilder, day, "day", "days");
        appendUnit(stringBuilder, hour, "hour", "hours");
        appendUnit(stringBuilder, minute, "minute", "minutes");
        appendUnit(stringBuilder, second, "second", "seconds");
        return stringBuilder.toString();
    }
    
    public static StringBuilder appendUnit(StringBuilder stringBuilder, long unit, String singular, String plural) {
        if (unit > 0) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(", ");
            }
            
            stringBuilder.append(unit).append(" ");
            if (unit == 1) {
                stringBuilder.append(singular);
            } else {
                stringBuilder.append(plural);
            }
        }
        
        return stringBuilder;
    }
    
    public static Duration getDuration(Instant instant) {
        return Duration.between(instant, Instant.now());
    }
    
    public static ThreadFactory buildThreadFactory(String namingPattern) {
        return new BasicThreadFactory.Builder().namingPattern(namingPattern).daemon(true).priority(Thread.NORM_PRIORITY).build();
    }
    
    public static WriteBufferWaterMark getWriteBufferWaterMark() {
        return new WriteBufferWaterMark(512 * 1024, 2048 * 1024);
    }
    
    public static Optional<Path> getPath() {
        String userDir = System.getProperty("user.dir");
        if (StringUtils.isNotBlank(userDir)) {
            return Optional.of(Paths.get(userDir));
        }
        
        return Optional.empty();
    }
    
    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) throws NullPointerException {
        Objects.requireNonNull(elements);
        return Stream.of(elements).collect(Collectors.toCollection(ArrayList::new));
    }
    
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }
}