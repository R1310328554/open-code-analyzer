/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Content-Security-Policy（CSP）响应头构建器：链式添加/合并指令并序列化为 HTTP 头值。
 * <p>默认策略限制 frame-src/frame-ancestors 为 {@code 'self'}，object-src 为 {@code 'none'}。</p>
 */
public class ContentSecurityPolicyBuilder {

    // 本类使用的 CSP 指令名常量
    public static final String DIRECTIVE_NAME_FRAME_SRC = "frame-src";
    public static final String DIRECTIVE_NAME_FRAME_ANCESTORS = "frame-ancestors";
    public static final String DIRECTIVE_NAME_OBJECT_SRC = "object-src";

    // CSP 指令值关键字
    public static final String DIRECTIVE_VALUE_SELF = "'self'";
    public static final String DIRECTIVE_VALUE_NONE = "'none'";

    private final Map<String, String> directives = new LinkedHashMap<>();

    /** 创建带 Keycloak 默认 CSP 指令的构建器。 */
    public static ContentSecurityPolicyBuilder create() {
        return new ContentSecurityPolicyBuilder()
                .add(DIRECTIVE_NAME_FRAME_SRC, DIRECTIVE_VALUE_SELF)
                .add(DIRECTIVE_NAME_FRAME_ANCESTORS, DIRECTIVE_VALUE_SELF)
                .add(DIRECTIVE_NAME_OBJECT_SRC, DIRECTIVE_VALUE_NONE);
    }

    /** 从已有 CSP 头字符串解析并创建构建器。 */
    public static ContentSecurityPolicyBuilder create(String directives) {
        return new ContentSecurityPolicyBuilder().parse(directives);
    }

    /** 设置 {@code frame-src} 指令（{@code null} 移除）。 */
    public ContentSecurityPolicyBuilder frameSrc(String frameSrc) {
        if (frameSrc == null) {
            directives.remove(DIRECTIVE_NAME_FRAME_SRC);
        } else {
            put(DIRECTIVE_NAME_FRAME_SRC, frameSrc);
        }
        return this;
    }

    /** 追加 {@code frame-src} 源。 */
    public ContentSecurityPolicyBuilder addFrameSrc(String frameSrc) {
        return add(DIRECTIVE_NAME_FRAME_SRC, frameSrc);
    }

    /** @return frame-ancestors 是否为默认 {@code 'self'} */
    public boolean isDefaultFrameAncestors() {
        return DIRECTIVE_VALUE_SELF.equals(directives.get(DIRECTIVE_NAME_FRAME_ANCESTORS));
    }

    /** 设置 {@code frame-ancestors} 指令（{@code null} 移除）。 */
    public ContentSecurityPolicyBuilder frameAncestors(String frameancestors) {
        if (frameancestors == null) {
            directives.remove(DIRECTIVE_NAME_FRAME_ANCESTORS);
        } else {
            put(DIRECTIVE_NAME_FRAME_ANCESTORS, frameancestors);
        }
        return this;
    }

    /** 追加 {@code frame-ancestors} 源。 */
    public ContentSecurityPolicyBuilder addFrameAncestors(String frameancestors) {
        return add(DIRECTIVE_NAME_FRAME_ANCESTORS, frameancestors);
    }

    /** @return 分号分隔的 CSP 头字符串 */
    public String build() {
        StringBuilder sb = new StringBuilder();
        if (!directives.isEmpty()) {
            for (Map.Entry<String, String> entry : directives.entrySet()) {
                sb.append(entry.getKey());
                if (!entry.getValue().isEmpty()) {
                    sb.append(" ").append(entry.getValue());
                }
                sb.append("; ");
            }
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private ContentSecurityPolicyBuilder put(String name, String value) {
        if (name != null && value != null) {
            directives.put(name, value);
        }
        return this;
    }

    private ContentSecurityPolicyBuilder add(String name, String value) {
        if (name != null && value != null) {
            String current = directives.get(name);
            if (current != null && !current.isEmpty()) {
                value = current + " " + value;
            }
            directives.put(name, value);
        }
        return this;
    }

    // W3C CSP 草案：https://www.w3.org/TR/CSP/
    // 仅处理空格，不解析规范中的其他空白符
    private ContentSecurityPolicyBuilder parse(String value) {
        if (value == null) {
            return this;
        }
        String[] values = value.split(";");
        if (values != null) {
            for (String directive : values) {
                directive = directive.trim();
                int idx = directive.indexOf(' ');
                if (idx > 0) {
                    add(directive.substring(0, idx), directive.substring(idx + 1, directive.length()).trim());
                } else if (!directive.isEmpty()) {
                    add(directive, "");
                }
            }
        }
        return this;
    }
}
