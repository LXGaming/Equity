/*
 * Copyright 2017 Alex Thomson
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

package io.github.lxgaming.equity.text;

import io.github.lxgaming.equity.text.format.TextColor;

public class TextComponent {
    
    private String text;
    private String color;
    private Boolean bold;
    private Boolean italic;
    private Boolean obfuscated;
    private Boolean strikethrough;
    private Boolean underline;
    
    protected TextComponent() {
    }
    
    public String getText() {
        return text;
    }
    
    protected void setText(String text) {
        this.text = text;
    }
    
    public String getColor() {
        return color;
    }
    
    protected void setColor(TextColor color) {
        setColor(color.getId());
    }
    
    protected void setColor(String color) {
        this.color = color;
    }
    
    protected Boolean getBold() {
        return bold;
    }
    
    public boolean isBold() {
        if (getBold() != null) {
            return getBold();
        }
        
        return false;
    }
    
    protected void setBold(Boolean bold) {
        this.bold = bold;
    }
    
    protected Boolean getItalic() {
        return italic;
    }
    
    public boolean isItalic() {
        if (getItalic() != null) {
            return getItalic();
        }
        
        return false;
    }
    
    protected void setItalic(Boolean italic) {
        this.italic = italic;
    }
    
    protected Boolean getObfuscated() {
        return obfuscated;
    }
    
    public boolean isObfuscated() {
        if (getObfuscated() != null) {
            return getObfuscated();
        }
        
        return false;
    }
    
    protected void setObfuscated(Boolean obfuscated) {
        this.obfuscated = obfuscated;
    }
    
    protected Boolean getStrikethrough() {
        return strikethrough;
    }
    
    protected void setStrikethrough(Boolean strikethrough) {
        this.strikethrough = strikethrough;
    }
    
    public boolean hasStrikethrough() {
        if (getStrikethrough() != null) {
            return getStrikethrough();
        }
        
        return false;
    }
    
    protected Boolean getUnderline() {
        return underline;
    }
    
    protected void setUnderline(Boolean underline) {
        this.underline = underline;
    }
    
    public boolean hasUnderline() {
        if (getUnderline() != null) {
            return getUnderline();
        }
        
        return false;
    }
}