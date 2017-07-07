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

package nz.co.lolnet.equity.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import nz.co.lolnet.equity.entries.Config;
import nz.co.lolnet.equity.util.EquityUtil;
import nz.co.lolnet.equity.util.LogHelper;

public class Configuration {
	
	private final Path path;
	private Config config;
	
	public Configuration(Path path) {
		this.path = path;
	}
	
	public boolean loadConfiguration() {
		setConfig((Config) loadObject(new Config(), "config.json"));
		LogHelper.info("Loaded configuration files.");
		return isValid();
	}
	
	private boolean isValid() {
		if (getConfig() != null) {
			return true;
		}
		return false;
	}
	
	private Object loadObject(Object object, String name) {
		try {
			if (object == null || StringUtils.isBlank(name)) {
				throw new IllegalArgumentException("Supplied arguments are null!");
			}
			
			Gson gson = EquityUtil.getGsonWithTypeAdapter(object);
			File file = getPath().resolve(name).toFile();
			if (gson == null || file == null) {
				throw new NullPointerException("Gson or File are null!");
			}
			
			if (!file.exists() && !saveObject(object, name)) {
				return object;
			}
			
			String string = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
			if (StringUtils.isBlank(string)) {
				throw new IOException("File '" + name + "' is blank!");
			}
			
			Object jsonObject = gson.fromJson(string, object.getClass());
			if (jsonObject == null) {
				throw new JsonParseException("Failed to parse File '" + name + "'!");
			}
			
			return jsonObject;
		} catch (IOException | OutOfMemoryError | RuntimeException ex) {
			LogHelper.error("Encountered an error processing 'loadObject' for '" + name + "' in '" + getClass().getSimpleName() + "' - " + ex.getMessage());
			ex.printStackTrace();
		}
		return object;
	}
	
	private boolean saveObject(Object object, String name) {
		try {
			if (object == null || StringUtils.isBlank(name)) {
				throw new IllegalArgumentException("Supplied arguments are null!");
			}
			
			Gson gson = EquityUtil.getGsonWithTypeAdapter(object);
			File file = getPath().resolve(name).toFile();
			if (gson == null || file == null) {
				throw new NullPointerException("Gson or File are null!");
			}
			
			File parentFile = file.getParentFile();
			if (parentFile != null && !parentFile.exists() && parentFile.mkdirs()) {
				LogHelper.info("Successfully created directory '" + parentFile.getName() + "'.");
			}
			
			if (!file.exists()) {
				file.createNewFile();
				LogHelper.info("Successfully created file '" + file.getName() + "'.");
			}
			
			Files.write(file.toPath(), gson.toJson(object, object.getClass()).getBytes(StandardCharsets.UTF_8));
			return true;
		} catch (IOException | OutOfMemoryError | RuntimeException ex) {
			LogHelper.error("Encountered an error processing 'saveObject' for '" + name + "' in '" + getClass().getSimpleName() + "' - " + ex.getMessage());
			ex.printStackTrace();
		}
		return false;
	}
	
	private Path getPath() {
		return path;
	}
	
	public Config getConfig() {
		return config;
	}
	
	private void setConfig(Config config) {
		this.config = config;
	}
}