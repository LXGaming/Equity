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

package nz.co.lolnet.equity;

import java.nio.file.Paths;

import nz.co.lolnet.equity.configuration.Configuration;

public class Main {
	
	public static void main(String[] args) {
		Equity equity = new Equity();
		equity.setConfiguration(new Configuration(Paths.get(System.getProperty("user.dir"))));
		equity.loadEquity();
	}
}