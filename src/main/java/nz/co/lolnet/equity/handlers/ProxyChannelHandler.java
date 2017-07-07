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

package nz.co.lolnet.equity.handlers;

import nz.co.lolnet.equity.entries.Connection.ConnectionSide;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public class ProxyChannelHandler extends ChannelInitializer<Channel> {
	
	private final ConnectionSide connectionSide;
	
	public ProxyChannelHandler(ConnectionSide connectionSide) {
		this.connectionSide = connectionSide;
	}
	
	@Override
	protected void initChannel(Channel channel) {
		channel.pipeline().addFirst(new ProxyDecodingHandler(getConnectionSide()));
		
		if (getConnectionSide() != null && getConnectionSide().equals(ConnectionSide.CLIENT)) {
			channel.pipeline().addFirst(new ProxyLegacyHandler());
			channel.pipeline().addLast(new ProxyClientHandler());
		}
		
		if (getConnectionSide() != null && getConnectionSide().equals(ConnectionSide.SERVER)) {
			channel.pipeline().addLast(new ProxyServerHandler());
		}
		
		channel.pipeline().addLast(new ProxyEncodingHandler(getConnectionSide()));
	}
	
	private ConnectionSide getConnectionSide() {
		return connectionSide;
	}
}