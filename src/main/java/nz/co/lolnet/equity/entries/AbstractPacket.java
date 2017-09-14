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

package nz.co.lolnet.equity.entries;

import java.util.Objects;
import java.util.Optional;

import nz.co.lolnet.equity.Equity;

public abstract class AbstractPacket {
	
	public void read(ProxyMessage proxyMessage) {
		throw new UnsupportedOperationException("Packet must implement read method!");
	}
	
	public void write(ProxyMessage proxyMessage) {
		throw new UnsupportedOperationException("Packet must implement write method!");
	}
	
	protected void writePacketId(ProxyMessage proxyMessage) throws IllegalStateException, NullPointerException {
		Objects.requireNonNull(proxyMessage);
		if (!proxyMessage.isValid()) {
			throw new IllegalStateException("ProxyMessage is invalid");
		}
		
		Optional<Integer> packetId = Equity.getInstance().getPacketManager().getPacketId(getClass(), proxyMessage.getConnection().getProtocolVersion());
		if (packetId.isPresent()) {
			proxyMessage.getPacket().writeVarInt(packetId.get());
			return;
		}
		
		throw new IllegalStateException("Failed to writePacketId");
	}
}