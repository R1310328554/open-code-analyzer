/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.ai.model.mcp.registry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

/**
 * MCP Registry 图标模型，可在 UI 中展示可选尺寸的图标。
 *
 * <p>必填 {@link #src}（HTTPS URI）；可选 mimeType、sizes、theme。
 * 字段与 components.schemas.Icon 一致。</p>
 *
 * @author xinluo
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Icon {
    
    @JsonProperty(value = "src", required = true)
    private String src;
    
    private MimeType mimeType;
    
    private List<String> sizes;
    
    private Theme theme;
    
    /**
     * 返回图标 HTTPS 资源地址。
     *
     * @return src 图标 URI
     */
    public String getSrc() {
        return src;
    }
    
    /**
     * 设置图标 HTTPS 资源地址。
     *
     * @param src 图标 URI
     */
    public void setSrc(String src) {
        this.src = src;
    }
    
    /**
     * Get mime type.
     *
     * @return mime type
      * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
     */
    public MimeType getMimeType() {
        return mimeType;
    }
    
    /**
     * Set mime type.
     *
     * @param mimeType mime type
      * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
     */
    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }
    
    /**
     * Get sizes.
     *
     * @return sizes
      * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
     */
    public List<String> getSizes() {
        return sizes;
    }
    
    /**
     * Set sizes.
     *
     * @param sizes sizes
      * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
     */
    public void setSizes(List<String> sizes) {
        this.sizes = sizes;
    }
    
    /**
     * Get theme.
     *
     * @return theme
      * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
     */
    public Theme getTheme() {
        return theme;
    }
    
    /**
     * Set theme.
     *
     * @param theme theme
      * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
     */
    public void setTheme(Theme theme) {
        this.theme = theme;
    }
    
    /**
     * 图标 MIME 类型枚举：image/png、jpeg、jpg、svg+xml、webp。
     * 序列化/反序列化为小写字符串值。
     */
    public static enum MimeType {
        
        /**
         * PNG mime type.
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        IMAGE_PNG("image/png"),
        /**
         * JPEG mime type.
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        IMAGE_JPEG("image/jpeg"),
        /**
         * JPG mime type.
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        IMAGE_JPG("image/jpg"),
        /**
         * SVG XML mime type.
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        IMAGE_SVG_XML("image/svg+xml"),
        /**
         * WebP mime type.
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        IMAGE_WEBP("image/webp");
        
        private final String value;
        
        /**
         * Constructor.
         *
         * @param value value
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        MimeType(String value) {
            this.value = value;
        }
        
        /**
         * Get value.
         *
         * @return value
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        @JsonValue
        public String getValue() {
            return value;
        }
        
        /**
         * Create from value.
         *
         * @param value value
         * @return MimeType
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        @JsonCreator
        public static MimeType fromValue(String value) {
            for (MimeType t : MimeType.values()) {
                if (t.value.equalsIgnoreCase(value)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Unknown mimeType: " + value);
        }
    }
    
    /**
     * 图标主题枚举：light 或 dark。
     * 序列化/反序列化为小写字符串值。
     */
    public static enum Theme {
        
        /**
         * Light theme.
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        LIGHT("light"),
        /**
         * Dark theme.
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        DARK("dark");
        
        private final String value;
        
        /**
         * Constructor.
         *
         * @param value value
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        Theme(String value) {
            this.value = value;
        }
        
        /**
         * Get value.
         *
         * @return value
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        @JsonValue
        public String getValue() {
            return value;
        }
        
        /**
         * Create from value.
         *
         * @param value value
         * @return Theme
          * <p>Nacos AI MCP 模型 API；详见上方说明。</p>
         */
        @JsonCreator
        public static Theme fromValue(String value) {
            for (Theme t : Theme.values()) {
                if (t.value.equalsIgnoreCase(value)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("Unknown theme: " + value);
        }
    }
}
