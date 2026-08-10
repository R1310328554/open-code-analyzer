/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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

package org.keycloak.theme;

/**
 * 主题静态资源描述符。
 * <p>封装样式表、脚本或 favicon 的路径及 HTML 属性（media、integrity、defer 等），由 {@link ThemeResourcesParser} 从 theme.properties 解析构建。</p>
 */
public class ThemeResourceDescriptor {

    /** 资源相对路径。 */
    private final String path;
    private final String media;
    private final String integrity;
    private final String crossorigin;
    private final String defer;
    private final String async;
    private final String type;
    private final String blocking;
    private final String rel;

    /** 规范化 favicon 路径：去除 leading {@code /}。 */
    static String normalizeFaviconPath(String path) {
        if (path != null && path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    /** 通过 Builder 构造；favicon 为 true 时自动规范化路径。 */
    private ThemeResourceDescriptor(Builder builder, boolean favicon) {
        this.path = favicon ? normalizeFaviconPath(builder.path) : builder.path;
        this.media = builder.media;
        this.integrity = builder.integrity;
        this.crossorigin = builder.crossorigin;
        this.defer = builder.defer;
        this.async = builder.async;
        this.type = builder.type;
        this.blocking = builder.blocking;
        this.rel = builder.rel;
    }

    /** 以给定路径创建 Builder。 */
    public static Builder builder(String path) {
        return new Builder(path);
    }

    /** 返回资源路径。 */
    public String getPath() {
        return path;
    }

    /** 返回 {@code media} 属性值。 */
    public String getMedia() {
        return media;
    }

    /** 返回 SRI {@code integrity} 属性值。 */
    public String getIntegrity() {
        return integrity;
    }

    /** 返回 {@code crossorigin} 属性值。 */
    public String getCrossorigin() {
        return crossorigin;
    }

    /** 返回 {@code defer} 属性原始值。 */
    public String getDefer() {
        return defer;
    }

    /** 返回 {@code async} 属性原始值。 */
    public String getAsync() {
        return async;
    }

    /** 返回 MIME {@code type} 属性值。 */
    public String getType() {
        return type;
    }

    /** 返回 {@code blocking} 属性值。 */
    public String getBlocking() {
        return blocking;
    }

    /** 返回 link 元素的 {@code rel} 属性值。 */
    public String getRel() {
        return rel;
    }

    /** 是否配置了非空 media 属性。 */
    public boolean hasMedia() {
        return media != null && !media.isEmpty();
    }

    /** 是否配置了非空 integrity 属性。 */
    public boolean hasIntegrity() {
        return integrity != null && !integrity.isEmpty();
    }

    /** 是否配置了非空 crossorigin 属性。 */
    public boolean hasCrossorigin() {
        return crossorigin != null && !crossorigin.isEmpty();
    }

    /** defer 属性是否为真值（{@code true} 或 {@code defer}）。 */
    public boolean hasDefer() {
        return defer != null && isTruthy(defer, "defer");
    }

    /** async 属性是否为真值（{@code true} 或 {@code async}）。 */
    public boolean hasAsync() {
        return async != null && isTruthy(async, "async");
    }

    /** 是否配置了非空 type 属性。 */
    public boolean hasType() {
        return type != null && !type.isEmpty();
    }

    /** 是否配置了非空 blocking 属性。 */
    public boolean hasBlocking() {
        return blocking != null && !blocking.isEmpty();
    }

    /** 是否配置了非空 rel 属性。 */
    public boolean hasRel() {
        return rel != null && !rel.isEmpty();
    }

    /** 判断属性值是否为 HTML 布尔真值。 */
    private static boolean isTruthy(String value, String keyword) {
        return "true".equalsIgnoreCase(value) || keyword.equalsIgnoreCase(value);
    }

    /** 根据文件扩展名推断 favicon MIME 类型。 */
    static String inferFaviconType(String path) {
        if (path == null) {
            return null;
        }
        String lower = path.toLowerCase();
        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".ico")) {
            return "image/x-icon";
        }
        return null;
    }

    /** 流式构建 {@link ThemeResourceDescriptor}。 */
    public static final class Builder {
        private final String path;
        private String media;
        private String integrity;
        private String crossorigin;
        private String defer;
        private String async;
        private String type;
        private String blocking;
        private String rel;

        private Builder(String path) {
            this.path = path;
        }

        /** 设置 media 属性。 */
        public Builder media(String media) {
            this.media = media;
            return this;
        }

        /** 设置 integrity 属性。 */
        public Builder integrity(String integrity) {
            this.integrity = integrity;
            return this;
        }

        /** 设置 crossorigin 属性。 */
        public Builder crossorigin(String crossorigin) {
            this.crossorigin = crossorigin;
            return this;
        }

        /** 设置 defer 属性。 */
        public Builder defer(String defer) {
            this.defer = defer;
            return this;
        }

        /** 设置 async 属性。 */
        public Builder async(String async) {
            this.async = async;
            return this;
        }

        /** 设置 type 属性。 */
        public Builder type(String type) {
            this.type = type;
            return this;
        }

        /** 设置 blocking 属性。 */
        public Builder blocking(String blocking) {
            this.blocking = blocking;
            return this;
        }

        /** 设置 rel 属性。 */
        public Builder rel(String rel) {
            this.rel = rel;
            return this;
        }

        /** 按属性名设置对应字段；值为 null 时忽略。 */
        public Builder attribute(String name, String value) {
            if (value == null) {
                return this;
            }
            switch (name) {
                case "media" -> media = value;
                case "integrity" -> integrity = value;
                case "crossorigin" -> crossorigin = value;
                case "defer" -> defer = value;
                case "async" -> async = value;
                case "type" -> type = value;
                case "blocking" -> blocking = value;
                case "rel" -> rel = value;
                default -> { }
            }
            return this;
        }

        /** 构建普通主题资源描述符。 */
        public ThemeResourceDescriptor build() {
            return new ThemeResourceDescriptor(this, false);
        }

        /** 构建 favicon 描述符，自动推断 type 并默认 rel=icon。 */
        public ThemeResourceDescriptor buildFavicon() {
            String normalizedPath = normalizeFaviconPath(path);
            if (type == null || type.isEmpty()) {
                type = inferFaviconType(normalizedPath);
            }
            if (rel == null || rel.isEmpty()) {
                rel = "icon";
            }
            return new ThemeResourceDescriptor(this, true);
        }
    }
}
