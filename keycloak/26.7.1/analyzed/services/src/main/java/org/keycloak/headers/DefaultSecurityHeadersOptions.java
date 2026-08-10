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
 * {@link SecurityHeadersOptions} 的可变实现，控制单次响应的安全头写入行为。
 * <p>支持跳过全部头、放宽 frame-ancestors、允许空 Content-Type 等选项。</p>
 */
public class DefaultSecurityHeadersOptions implements SecurityHeadersOptions {

    /** 为 true 时 {@link DefaultSecurityHeadersProvider} 跳过写入任何安全头。 */
    private boolean skipHeaders;
    /** 为 true 时移除 X-Frame-Options 并放宽 CSP frame-ancestors。 */
    private boolean allowAnyFrameAncestor;
    /** 为 true 时允许响应无 Content-Type（特定状态码/方法）。 */
    private boolean allowEmptyContentType;
    /** 额外允许的 frame-src CSP 源。 */
    private String allowedFrameSrc;

    /** 向 CSP 添加允许的 frame-src 源。 */
    public SecurityHeadersOptions allowFrameSrc(String source) {
        allowedFrameSrc = source;
        return this;
    }

    @Override
    public SecurityHeadersOptions allowAnyFrameAncestor() {
        allowAnyFrameAncestor = true;
        return this;
    }

    /** 标记跳过全部安全响应头。 */
    public SecurityHeadersOptions skipHeaders() {
        skipHeaders = true;
        return this;
    }

    @Override
    public SecurityHeadersOptions allowEmptyContentType() {
        allowEmptyContentType = true;
        return this;
    }

    /** @return 配置的 frame-src 源 */
    String getAllowedFrameSrc() {
        return allowedFrameSrc;
    }

    /** @return 是否允许任意 frame 祖先 */
    boolean isAllowAnyFrameAncestor() {
        return allowAnyFrameAncestor;
    }

    /** @return 是否跳过安全头写入 */
    boolean isSkipHeaders() {
        return skipHeaders;
    }

    /** @return 是否允许空 Content-Type */
    public boolean isAllowEmptyContentType() {
        return allowEmptyContentType;
    }

}
