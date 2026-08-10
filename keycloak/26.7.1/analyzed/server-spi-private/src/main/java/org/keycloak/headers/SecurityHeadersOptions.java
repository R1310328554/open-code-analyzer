/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.headers;

/**
 * 安全响应头配置的流式构建器。
 * <p>由 {@link SecurityHeadersProvider#options()} 返回， 在 {@link SecurityHeadersProvider#addHeaders} 前链式调整 CSP frame-ancestors 等行为。</p>
 */
public interface SecurityHeadersOptions {

    /** 允许指定来源嵌入 iframe（CSP {@code frame-ancestors}）。 */
    SecurityHeadersOptions allowFrameSrc(String source);

    /** 允许任意来源嵌入（放宽 frame-ancestors 限制）。 */
    SecurityHeadersOptions allowAnyFrameAncestor();

    /** 跳过为本响应添加安全头（如静态资源或特殊端点）。 */
    SecurityHeadersOptions skipHeaders();

    /** 允许空 Content-Type，不强制设置默认类型。 */
    SecurityHeadersOptions allowEmptyContentType();

}
