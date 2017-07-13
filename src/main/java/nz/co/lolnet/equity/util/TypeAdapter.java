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

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;

public class TypeAdapter implements InstanceCreator<Object> {
	
	private final Object object;
	
	public TypeAdapter(Object object) {
		this.object = object;
	}
	
	@Override
	public Object createInstance(Type type) {
		if (getObject() == null || type == null) {
			return null;
		}
		
		if (getObject().getClass().getName().equals(type.getTypeName())) {
			return getObject();
		}
		
		return null;
	}
	
	private Object getObject() {
		return object;
	}
}