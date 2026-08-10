/*
 *  Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.nacos.sys.env;

import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.boot.origin.TextResourceOrigin;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 带来源追踪的 properties 文件加载器（源自 Spring Boot 实现）。
 *
 * <p>解析 {@code .properties} 并保留 {@link OriginTrackedValue} 与 {@link TextResourceOrigin}，便于配置项溯源与覆盖链追踪。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class OriginTrackedPropertiesLoader {
    
    private final Resource resource;
    
    /**
     * 构造加载器并绑定 properties 资源。
     *
     * @param resource the resource of the {@code .properties} data
     */
    OriginTrackedPropertiesLoader(Resource resource) {
        Assert.notNull(resource, "Resource must not be null");
        this.resource = resource;
    }
    
    /**
     * 加载 properties 并展开 {@code key[]=a,b} 列表语法。
     *
     * @return the loaded properties
     * @throws IOException on read error
     */
    public Map<String, OriginTrackedValue> load() throws IOException {
        return load(true);
    }
    
    /**
     * 加载 properties，可选是否展开数组简写键。
     *
     * @param expandLists if list {@code name[]=a,b,c} shortcuts should be expanded
     * @return the loaded properties
     * @throws IOException on read error
     */
    public Map<String, OriginTrackedValue> load(boolean expandLists) throws IOException {
        try (OriginTrackedPropertiesLoader.CharacterReader reader =
            new CharacterReader(this.resource)) {
            Map<String, OriginTrackedValue> result = new LinkedHashMap<>();
            StringBuilder buffer = new StringBuilder();
            while (reader.read()) {
                String key = loadKey(buffer, reader).trim();
                if (expandLists && key.endsWith("[]")) {
                    key = key.substring(0, key.length() - 2);
                    int index = 0;
                    do {
                        OriginTrackedValue value = loadValue(buffer, reader, true);
                        put(result, key + "[" + (index++) + "]", value);
                        if (!reader.isEndOfLine()) {
                            reader.read();
                        }
                    } while (!reader.isEndOfLine());
                } else {
                    OriginTrackedValue value = loadValue(buffer, reader, false);
                    put(result, key, value);
                }
            }
            return result;
        }
    }
    
    private void put(Map<String, OriginTrackedValue> result, String key, OriginTrackedValue value) {
        if (!key.isEmpty()) {
            result.put(key, value);
        }
    }
    
    private String loadKey(StringBuilder buffer,
        OriginTrackedPropertiesLoader.CharacterReader reader)
        throws IOException {
        buffer.setLength(0);
        boolean previousWhitespace = false;
        while (!reader.isEndOfLine()) {
            if (reader.isPropertyDelimiter()) {
                reader.read();
                return buffer.toString();
            }
            if (!reader.isWhiteSpace() && previousWhitespace) {
                return buffer.toString();
            }
            previousWhitespace = reader.isWhiteSpace();
            buffer.append(reader.getCharacter());
            reader.read();
        }
        return buffer.toString();
    }
    
    private OriginTrackedValue loadValue(StringBuilder buffer,
        OriginTrackedPropertiesLoader.CharacterReader reader,
        boolean splitLists) throws IOException {
        buffer.setLength(0);
        while (reader.isWhiteSpace() && !reader.isEndOfLine()) {
            reader.read();
        }
        TextResourceOrigin.Location location = reader.getLocation();
        while (!reader.isEndOfLine() && !(splitLists && reader.isListDelimiter())) {
            buffer.append(reader.getCharacter());
            reader.read();
        }
        Origin origin = new TextResourceOrigin(this.resource, location);
        return OriginTrackedValue.of(buffer.toString(), origin);
    }
    
    /**
     * 逐字符读取 properties 源：跳过注释、处理续行与反斜杠转义。
     */
    private static class CharacterReader implements Closeable {
        
        private final String[] escapes = {"trnf", "\t\r\n\f"};
        
        private final LineNumberReader reader;
        
        private int columnNumber = -1;
        
        private boolean escaped;
        
        private int character;
        
        CharacterReader(Resource resource) throws IOException {
            this.reader = new LineNumberReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.ISO_8859_1));
        }
        
        @Override
        public void close() throws IOException {
            this.reader.close();
        }
        
        public boolean read() throws IOException {
            return read(false);
        }
        
        public boolean read(boolean wrappedLine) throws IOException {
            this.escaped = false;
            this.character = this.reader.read();
            this.columnNumber++;
            if (this.columnNumber == 0) {
                skipLeadingWhitespace();
                if (!wrappedLine) {
                    skipComment();
                }
            }
            if (this.character == '\\') {
                this.escaped = true;
                readEscaped();
            } else if (this.character == '\n') {
                this.columnNumber = -1;
            }
            return !isEndOfFile();
        }
        
        private void skipLeadingWhitespace() throws IOException {
            while (isWhiteSpace()) {
                this.character = this.reader.read();
                this.columnNumber++;
            }
        }
        
        private void skipComment() throws IOException {
            if (this.character == '#' || this.character == '!') {
                while (this.character != '\n' && this.character != -1) {
                    this.character = this.reader.read();
                }
                this.columnNumber = -1;
                read();
            }
        }
        
        private void readEscaped() throws IOException {
            this.character = this.reader.read();
            int escapeIndex = escapes[0].indexOf(this.character);
            if (escapeIndex != -1) {
                this.character = escapes[1].charAt(escapeIndex);
            } else if (this.character == '\n') {
                this.columnNumber = -1;
                read(true);
            } else if (this.character == 'u') {
                readUnicode();
            }
        }
        
        private void readUnicode() throws IOException {
            this.character = 0;
            for (int i = 0; i < 4; i++) {
                int digit = this.reader.read();
                if (digit >= '0' && digit <= '9') {
                    this.character = (this.character << 4) + digit - '0';
                } else if (digit >= 'a' && digit <= 'f') {
                    this.character = (this.character << 4) + digit - 'a' + 10;
                } else if (digit >= 'A' && digit <= 'F') {
                    this.character = (this.character << 4) + digit - 'A' + 10;
                } else {
                    throw new IllegalStateException("Malformed \\uxxxx encoding.");
                }
            }
        }
        
        public boolean isWhiteSpace() {
            return !this.escaped
                && (this.character == ' ' || this.character == '\t' || this.character == '\f');
        }
        
        public boolean isEndOfFile() {
            return this.character == -1;
        }
        
        public boolean isEndOfLine() {
            return this.character == -1 || (!this.escaped && this.character == '\n');
        }
        
        public boolean isListDelimiter() {
            return !this.escaped && this.character == ',';
        }
        
        public boolean isPropertyDelimiter() {
            return !this.escaped && (this.character == '=' || this.character == ':');
        }
        
        public char getCharacter() {
            return (char) this.character;
        }
        
        public TextResourceOrigin.Location getLocation() {
            return new TextResourceOrigin.Location(this.reader.getLineNumber(), this.columnNumber);
        }
        
    }
}
