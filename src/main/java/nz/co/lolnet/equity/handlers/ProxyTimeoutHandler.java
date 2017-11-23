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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutException;

import java.util.concurrent.TimeUnit;

public class ProxyTimeoutHandler extends IdleStateHandler {
    
    private boolean closed;
    
    public ProxyTimeoutHandler(int timeout) {
        this(timeout, TimeUnit.MILLISECONDS);
    }
    
    public ProxyTimeoutHandler(long timeout, TimeUnit timeUnit) {
        super(timeout, 0, 0, timeUnit);
    }
    
    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (evt.state() == IdleState.READER_IDLE && !isClosed()) {
            ctx.fireExceptionCaught(ReadTimeoutException.INSTANCE);
            ctx.close();
            setClosed(true);
        }
    }
    
    private boolean isClosed() {
        return closed;
    }
    
    private void setClosed(boolean closed) {
        this.closed = closed;
    }
    
    public static String getName() {
        return "proxy_timeout";
    }
}