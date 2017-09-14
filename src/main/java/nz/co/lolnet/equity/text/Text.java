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

package nz.co.lolnet.equity.text;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class Text extends TextComponent {
	
	private final List<TextComponent> extra;
	
	public Text() {
		this(Text.of(""), null);
	}
	
	public Text(TextComponent textComponent, List<TextComponent> extra) {
		super();
		setText(textComponent.getText());
		setColor(textComponent.getColor());
		setBold(textComponent.getBold());
		setItalic(textComponent.getItalic());
		setObfuscated(textComponent.getObfuscated());
		setStrikethrough(textComponent.getStrikethrough());
		setUnderline(textComponent.getUnderline());
		this.extra = extra;
	}
	
	public static LiteralText.Builder of(String string) {
		return new LiteralText.Builder().text(string);
	}
	
	public static Text.Builder builder() {
		return new Text.Builder();
	}
	
	public List<TextComponent> getExtra() {
		return extra;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this, getClass());
	}
	
	public static class Builder {
		
		private List<TextComponent> children;
		
		public Builder() {
			children = new ArrayList<TextComponent>();
		}
		
		public Text build() {
			if (getChildren().isEmpty()) {
				return new Text();
			}
			
			if (getChildren().size() == 1) {
				return new Text(getChildren().get(0), null);
			}
			
			return new Text(Text.of(""), getChildren());
		}
		
		public Builder append(TextComponent textComponent) {
			getChildren().add(textComponent);
			return this;
		}
		
		public Builder append(LiteralText.Builder literalTextBuilder) {
			getChildren().add(literalTextBuilder.build());
			return this;
		}
		
		private List<TextComponent> getChildren() {
			return children;
		}
	}
}