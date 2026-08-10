/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.ai.importer.security;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.common.utils.InternetAddressUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportArtifact;
import com.alibaba.nacos.plugin.ai.importer.model.AiResourceImportSource;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.util.Locale;
import java.util.Map;

/**
 * Central guard for import artifacts crossing the plugin boundary.
 * <p>AI 资源导入安全守卫，在插件边界处校验导入源端点与制品内容，防止 SSRF、私有网络访问及超大载荷等安全风险。</p>
 *
 * @author xiweng.yy
 * @since 3.2.1
 */
@Service
public class AiResourceImportSecurityGuard {
    
    /** 导入源属性：是否允许 HTTP（kebab-case）。 */
    public static final String PROPERTY_ALLOW_HTTP = "allow-http";
    
    /** 导入源属性：是否允许 HTTP（camelCase）。 */
    public static final String PROPERTY_ALLOW_HTTP_CAMEL = "allowHttp";
    
    /** 导入源属性：是否允许访问私有/本地网络（kebab-case）。 */
    public static final String PROPERTY_ALLOW_PRIVATE_NETWORK = "allow-private-network";
    
    /** 导入源属性：是否允许访问私有/本地网络（camelCase）。 */
    public static final String PROPERTY_ALLOW_PRIVATE_NETWORK_CAMEL = "allowPrivateNetwork";
    
    private static final String HTTPS_SCHEME = "https";
    
    private static final String HTTP_SCHEME = "http";
    
    private static final String LOCALHOST = "localhost";
    
    private static final String LOCALHOST_SUFFIX = ".localhost";
    
    /**
     * Check artifact type and size before validation or import.
     * <p>在校验或导入前检查制品：非空、资源类型匹配、载荷大小不超过源配置的 {@code maxArtifactSize} 上限。</p>
     *
     * @param source resolved source
     * @param expectedResourceType expected resource type
     * @param artifact fetched artifact
     * @throws NacosException if the artifact violates the import boundary
     */
    public void checkArtifact(AiResourceImportSource source, String expectedResourceType,
        AiResourceImportArtifact artifact) throws NacosException {
        if (artifact == null) { // 制品不能为空
            throw invalid("AI resource import artifact must not be null.");
        }
        if (!StringUtils.equals(expectedResourceType, artifact.getResourceType())) { // 资源类型须一致
            throw invalid("AI resource import artifact resource type mismatch.");
        }
        long payloadSize = 0;
        if (artifact.getPayload() != null) {
            payloadSize += artifact.getPayload().length;
        }
        if (artifact.getPayloadJson() != null) {
            payloadSize += artifact.getPayloadJson().length();
        }
        if (source.getMaxArtifactSize() > 0 && payloadSize > source.getMaxArtifactSize()) { // 超限则拒绝
            throw invalid("AI resource import artifact size exceeds source limit.");
        }
    }
    
    /**
     * Check source endpoint before an importer makes network requests.
     * <p>导入器发起网络请求前校验源端点：协议须为 http/https，默认强制 https；主机不得指向 localhost、环回或私有网段，除非源属性显式放行。</p>
     *
     * @param source resolved source
     * @throws NacosException if the source endpoint violates the import boundary
     */
    public void checkSourceEndpoint(AiResourceImportSource source) throws NacosException {
        if (source == null || StringUtils.isBlank(source.getEndpoint())) { // 无端点时跳过校验
            return;
        }
        URI endpoint = parseEndpoint(source.getEndpoint());
        String scheme = endpoint.getScheme() == null ? null
            : endpoint.getScheme().toLowerCase(Locale.ENGLISH);
        if (!HTTPS_SCHEME.equals(scheme) && !HTTP_SCHEME.equals(scheme)) {
            throw invalid("AI resource import source endpoint must use http or https.");
        }
        if (HTTP_SCHEME.equals(scheme) && !isSourcePropertyEnabled(source, PROPERTY_ALLOW_HTTP, // 未启用 allow-http 则禁止明文 HTTP
            PROPERTY_ALLOW_HTTP_CAMEL)) {
            throw invalid(
                "AI resource import source endpoint must use https unless allow-http is enabled.");
        }
        if (StringUtils.isBlank(endpoint.getHost())) {
            throw invalid("AI resource import source endpoint host must not be empty.");
        }
        if (isUnsafeHost(endpoint.getHost()) && !isSourcePropertyEnabled(source, // 私有/本地主机须显式放行
            PROPERTY_ALLOW_PRIVATE_NETWORK, PROPERTY_ALLOW_PRIVATE_NETWORK_CAMEL)) {
            throw invalid(
                "AI resource import source endpoint resolves to a private or local target.");
        }
    }
    
    /** 解析并校验端点为绝对 URL。 */
    private URI parseEndpoint(String endpoint) throws NacosException {
        try {
            URI result = URI.create(endpoint.trim());
            if (!result.isAbsolute()) {
                throw invalid("AI resource import source endpoint must be an absolute URL.");
            }
            return result;
        } catch (IllegalArgumentException e) {
            throw invalid("AI resource import source endpoint is invalid.");
        }
    }
    
    /** 判断主机是否为 localhost 或解析到不安全 IP 地址。 */
    private boolean isUnsafeHost(String host) throws NacosException {
        String normalized = InternetAddressUtil.removeBrackets(host).toLowerCase(Locale.ENGLISH);
        if (LOCALHOST.equals(normalized) || normalized.endsWith(LOCALHOST_SUFFIX)) {
            return true;
        }
        if (!InternetAddressUtil.isIp(normalized)) {
            return false;
        }
        try {
            return isUnsafeAddress(InetAddress.getByName(normalized));
        } catch (Exception e) {
            throw invalid("AI resource import source endpoint host is invalid.");
        }
    }
    
    /** 判断 IP 是否为环回、链路本地、站点本地、组播或 ULA IPv6。 */
    private boolean isUnsafeAddress(InetAddress address) {
        return address.isAnyLocalAddress() || address.isLoopbackAddress()
            || address.isLinkLocalAddress() || address.isSiteLocalAddress()
            || address.isMulticastAddress() || isUniqueLocalIpv6Address(address);
    }
    
    /** 检测 IPv6 唯一本地地址（fc00::/7）。 */
    private boolean isUniqueLocalIpv6Address(InetAddress address) {
        byte[] bytes = address.getAddress();
        return bytes.length == 16 && (bytes[0] & 0xfe) == 0xfc;
    }
    
    /** 读取导入源 properties，支持 kebab 与 camel 两种键名。 */
    private boolean isSourcePropertyEnabled(AiResourceImportSource source, String kebabKey,
        String camelKey) {
        Map<String, String> properties = source.getProperties();
        if (properties == null || properties.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(properties.get(kebabKey))
            || Boolean.parseBoolean(properties.get(camelKey));
    }
    
    /** 构造参数校验失败的 {@link NacosApiException}。 */
    private NacosException invalid(String message) {
        return new NacosApiException(NacosException.INVALID_PARAM,
            ErrorCode.PARAMETER_VALIDATE_ERROR, message);
    }
}
