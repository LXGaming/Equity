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

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.haproxy.ProxyHAProxyMessageDecoder;
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.entries.Connection.ConnectionSide;

public class ProxyChannelHandler extends ChannelInitializer<Channel> {
	
	private final ConnectionSide connectionSide;
	
	public ProxyChannelHandler(ConnectionSide connectionSide) {
		this.connectionSide = connectionSide;
	}
	
	@Override
	protected void initChannel(Channel channel) {
		if (Equity.getInstance().getProxyManager() == null || !Equity.getInstance().getProxyManager().isRunning()) {
			channel.close();
			return;
		}
		
		channel.pipeline().addFirst("ProxyDecoder", new ProxyDecodingHandler(getConnectionSide()));
		
		if (getConnectionSide() != null && getConnectionSide().equals(ConnectionSide.CLIENT)) {
			channel.pipeline().addFirst("ProxyLegacy", new ProxyLegacyHandler());
			if (Equity.getInstance().getConfig().isProxyProtocol()) {
				channel.pipeline().addFirst("ProxyHAProxyMessageDecoder", new ProxyHAProxyMessageDecoder());
			}
			
			channel.pipeline().addLast("ProxyClient", new ProxyClientHandler());
		}
		
		if (getConnectionSide() != null && getConnectionSide().equals(ConnectionSide.SERVER)) {
			channel.pipeline().addLast("ProxyServer", new ProxyServerHandler());
		}
		
		channel.pipeline().addLast("ProxyEncoder", new ProxyEncodingHandler(getConnectionSide()));
	}
	
	private ConnectionSide getConnectionSide() {
		return connectionSide;
	}
}