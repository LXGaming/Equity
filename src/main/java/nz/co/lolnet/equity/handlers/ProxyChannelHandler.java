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
import nz.co.lolnet.equity.Equity;
import nz.co.lolnet.equity.configuration.Config;
import nz.co.lolnet.equity.util.Toolbox;
import org.apache.commons.lang3.StringUtils;

public class ProxyChannelHandler extends ChannelInitializer<Channel> {
    
    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addFirst(ProxyDecodingHandler.getName(), new ProxyDecodingHandler());
        
        if (StringUtils.equals(channel.attr(Toolbox.getSideKey()).get(), ProxyClientHandler.getName())) {
            channel.pipeline().addBefore(ProxyDecodingHandler.getName(), ProxyLegacyHandler.getName(), new ProxyLegacyHandler());
            if (Equity.getInstance().getConfig().map(Config::isProxyProtocol).orElse(false)) {
                channel.pipeline().addFirst(HAProxyDecodingHandler.getName(), new HAProxyDecodingHandler());
            }
            
            channel.pipeline().addAfter(ProxyDecodingHandler.getName(), ProxyClientHandler.getName(), new ProxyClientHandler());
        }
        
        if (StringUtils.equals(channel.attr(Toolbox.getSideKey()).get(), ProxyServerHandler.getName())) {
            channel.pipeline().addAfter(ProxyDecodingHandler.getName(), ProxyServerHandler.getName(), new ProxyServerHandler());
        }
        
        channel.pipeline().addLast(ProxyEncodingHandler.getName(), new ProxyEncodingHandler());
        channel.pipeline().addFirst(ProxyTimeoutHandler.getName(), new ProxyTimeoutHandler(Equity.getInstance().getConfig().map(Config::getReadTimeout).orElse(30000)));
    }
}