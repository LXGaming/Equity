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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Server {
	
	private String name;
	private String host;
	private int port;
	private List<Integer> protocolVersions;
	
	public Server() {
		setName("LocalHost");
		setHost("127.0.0.1");
		setPort(25577);
		setProtocolVersions(Arrays.asList(335, 316, 315, 210, 110, 109, 108, 107, 47));
	}
	
	public String getIdentity() {
		if (StringUtils.isNotBlank(getName())) {
			return getName();
		}
		
		return getHost() + ":" + getPort();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getHost() {
		return host;
	}
	
	private void setHost(String host) {
		this.host = host;
	}
	
	public int getPort() {
		return port;
	}
	
	private void setPort(int port) {
		this.port = port;
	}
	
	public List<Integer> getProtocolVersions() {
		return protocolVersions;
	}
	
	private void setProtocolVersions(List<Integer> protocolVersions) {
		this.protocolVersions = protocolVersions;
	}
}