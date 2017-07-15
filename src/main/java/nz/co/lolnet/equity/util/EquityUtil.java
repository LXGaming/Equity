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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection;
import nz.co.lolnet.equity.entries.Packet;

public class EquityUtil {
	
	public static ThreadFactory getThreadFactory(String namingPattern) {
		return new BasicThreadFactory.Builder()
				.namingPattern(namingPattern)
				.daemon(true)
				.priority(Thread.NORM_PRIORITY)
				.build();
	}
	
	public static List<String> getApplicationInformation() {
		List<String> lines = new ArrayList<String>();
		lines.add(Reference.APP_NAME + " v" + Reference.APP_VERSION);
		lines.add("  Authors: " + String.join(", ", Reference.AUTHORS));
		lines.add("  Source: " + Reference.SOURCE);
		lines.add("  Website: " + Reference.WEBSITE);
		int length = Collections.max(lines, Comparator.comparingInt(String::length)).length();
		lines.add(0, String.join("", Collections.nCopies(length, "-")));
		lines.add(String.join("", Collections.nCopies(length, "-")));
		return lines;
	}
	
	public static GenericFutureListener<? extends Future<? super Void>> getFutureListener(Channel channel) {
		return future -> {
			if (future.isDone() && future.isSuccess()) {
				channel.read();
			} else {
				channel.close();
			}
		};
	}
	
	public static Packet getPacket(ByteBufAllocator byteBufAllocator, ByteBuf byteBuf, int length) {
		Packet packet = new Packet(Unpooled.EMPTY_BUFFER);
		if (byteBuf.hasMemoryAddress()) {
			packet = new Packet(byteBuf.slice(byteBuf.readerIndex(), length).retain());
			byteBuf.skipBytes(length);
		} else {
			ByteBuf directBuffer = byteBufAllocator.directBuffer(length);
			byteBuf.readBytes(directBuffer);
			packet = new Packet(directBuffer);
		}
		
		return packet;
	}
	
	public static boolean safeRelease(ByteBuf byteBuf) {
		try {
			if (byteBuf == null) {
				throw new IllegalArgumentException("Supplied arguments are null!");
			}
			
			if (byteBuf.refCnt() > 0) {
				byteBuf.release(byteBuf.refCnt());
			}
			
			return true;
		} catch (RuntimeException ex) {
			Equity.getInstance().getLogger().error("Encountered an error processing EquityUtil::safeRelease", ex);
		}
		
		return false;
	}
	
	public static Gson getGsonWithTypeAdapter(Object object) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		if (object != null) {
			gsonBuilder.registerTypeAdapter(object.getClass(), new TypeAdapter(object));
		}
		
		return gsonBuilder.setPrettyPrinting().create();
	}
	
	public static AttributeKey<Connection> getAttributeKey() {
		return AttributeKey.valueOf("CONNECTION");
	}
	
	public static String getAddress(SocketAddress socketAddress) {
		return getHost(socketAddress) + ":" + getPort(socketAddress);
	}
	
	public static String getHost(SocketAddress socketAddress) {
		if (socketAddress != null && socketAddress.getClass().isAssignableFrom(InetSocketAddress.class)) {
			InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
			return inetSocketAddress.getHostString();
		}
		
		return "UNKNOWN";
	}
	
	public static int getPort(SocketAddress socketAddress) {
		if (socketAddress != null && socketAddress.getClass().isAssignableFrom(InetSocketAddress.class)) {
			InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
			return inetSocketAddress.getPort();
		}
		
		return 0;
	}
}