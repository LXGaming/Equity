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

public class ProxyMessage {
	
	private final Connection connection;
	private final Packet packet;
	
	public ProxyMessage(Connection connection) {
		this(connection, new Packet());
	}
	
	public ProxyMessage(Connection connection, Packet packet) {
		this.connection = connection;
		this.packet = packet;
	}
	
	public boolean isValid() {
		if (getConnection() != null && getPacket() != null) {
			return true;
		}
		
		return false;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public Packet getPacket() {
		return packet;
	}
}