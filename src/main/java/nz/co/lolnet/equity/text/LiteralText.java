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

import nz.co.lolnet.equity.text.format.TextColor;

public class LiteralText {

    public static LiteralText.Builder builder() {
        return new LiteralText.Builder();
    }

    public static class Builder extends TextComponent {

        public TextComponent build() {
            return this;
        }

        public Builder text(String text) {
            setText(text);
            return this;
        }

        public Builder color(TextColor color) {
            setColor(color);
            return this;
        }

        public Builder bold(boolean bold) {
            setBold(bold);
            return this;
        }

        public Builder italic(boolean italic) {
            setItalic(italic);
            return this;
        }

        public Builder obfuscated(boolean obfuscated) {
            setObfuscated(obfuscated);
            return this;
        }

        public Builder strikethrough(boolean strikethrough) {
            setStrikethrough(strikethrough);
            return this;
        }

        public Builder underline(boolean underline) {
            setUnderline(underline);
            return this;
        }
    }
}