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
package org.keycloak.ssf.transmitter.support;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.keycloak.ssf.SsfException;

/**
 * 针对 {@code ssf.validPushUrls} 客户端属性白名单的每接收方 SSRF 门控。
 * 使用与 OIDC 重定向 URI 校验相同的精确或尾部通配符匹配语义，验证接收方提供的
 * {@code delivery.endpoint_url}，并附加三项 SSRF 收紧：
 *
 * <ol>
 *     <li>裸 {@code "*"} 条目从有效白名单静默丢弃——单次按键不得禁用 SSRF 防护。</li>
 *     <li>通配符条目须含本身无通配符且无查询的主机部分；防止 {@code "https://*"} 或
 *         {@code "https://*.example.com/*"} 等扩大白名单超出操作员审阅范围。</li>
 *     <li>匹配成功后对接收方 URL 再校验：scheme 须为 {@code https}，主机不得解析为
 *         环回/链路本地/站点本地/唯一本地/组播/任意本地地址。{@link #allowInsecure} 为
 *         {@code true} 时两项均 bypass（封闭网络部署与推送到本地 mock 的集成测试）。</li>
 * </ol>
 *
 * <p>{@link #matchesAllowList(Set, String)} 中的匹配逻辑刻意复制
 * {@code RedirectUtils.matchesRedirects}——保持 SSF 本地，不触碰 OIDC 重定向 URI 匹配器安全面。
 * 行为须与后者一致；跟踪 {@code RedirectUtils} 变更并在需要时镜像。
 */
public final class SsfPushUrlValidator {

    /** 是否允许非 HTTPS 及私有/环回主机（封闭网络或测试）。 */
    private final boolean allowInsecure;

    /** @param allowInsecure 是否允许不安全推送目标 */
    public SsfPushUrlValidator(boolean allowInsecure) {
        this.allowInsecure = allowInsecure;
    }

    /**
     * 将接收方 push URL 与白名单校验。拒绝时抛出带稳定机器可读原因码的
     * {@link SsfPushUrlValidationException}；匹配成功则静默返回。
     */
    public void validate(String pushUrl, Set<String> validPushUrls) {
        if (pushUrl == null || pushUrl.isBlank()) {
            throw new SsfPushUrlValidationException(Reason.URL_MISSING,
                    "delivery.endpoint_url is required for push delivery");
        }
        if (validPushUrls == null || validPushUrls.isEmpty()) {
            throw new SsfPushUrlValidationException(Reason.ALLOWLIST_EMPTY,
                    "delivery method 'push' requires the receiver client to declare ssf.validPushUrls");
        }
        Set<String> effective = filterUsableEntries(validPushUrls);
        if (effective.isEmpty()) {
            throw new SsfPushUrlValidationException(Reason.ALLOWLIST_EMPTY,
                    "delivery method 'push' requires at least one usable ssf.validPushUrls entry"
                            + " (bare '*' entries are not honoured)");
        }
        if (matchesAllowList(effective, pushUrl) == null) {
            throw new SsfPushUrlValidationException(Reason.NOT_IN_ALLOWLIST,
                    "delivery.endpoint_url is not in the receiver client's ssf.validPushUrls");
        }
        validateSchemeAndHost(pushUrl);
    }

    /**
     * 丢弃裸 {@code *} 及结构无效通配条目（主机含 {@code *} 或 {@code ?}、缺 scheme/主机、
     * 非 http(s) scheme）。检查保守——无法 confident 解析并批准的条目移除而非拒绝，
     * 避免错误配置条目静默扩大门控。
     */
    Set<String> filterUsableEntries(Set<String> entries) {
        Set<String> usable = new LinkedHashSet<>();
        for (String entry : entries) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            if ("*".equals(entry)) {
                continue;
            }
            String stripped = entry.endsWith("*")
                    ? entry.substring(0, entry.length() - 1)
                    : entry;
            URI parsed;
            try {
                parsed = new URI(stripped);
            } catch (Exception e) {
                continue;
            }
            if (!parsed.isAbsolute()) {
                continue;
            }
            String scheme = parsed.getScheme();
            if (scheme == null
                    || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                continue;
            }
            String host = parsed.getHost();
            if (host == null || host.isBlank()) {
                continue;
            }
            if (host.contains("*") || host.contains("?")) {
                continue;
            }
            usable.add(entry);
        }
        return usable;
    }

    /**
     * 复制 {@code RedirectUtils.matchesRedirects}，{@code allowWildcards=true} 硬编码——
     * SSF push URL 白名单始终支持尾部 {@code *} 通配符。返回匹配条目（通配符情况下去除 {@code *}），
     * 无匹配返回 {@code null}。
     *
     * <p>与 OIDC 匹配器行为对齐；若后者修复边界情况，在此镜像修复。
     */
    static String matchesAllowList(Set<String> validUrls, String pushUrl) {
        for (String validUrl : validUrls) {
            // 裸 * 由 filterUsableEntries 上游过滤；此处无需特殊处理。
            // 分支注释保留以标明与 RedirectUtils.matchesRedirects 的有意差异。
            // if ("*".equals(validUrl)) return validUrl;
            if (validUrl.endsWith("*") && !validUrl.contains("?")) {
                int idx = pushUrl.indexOf('?');
                if (idx == -1) {
                    idx = pushUrl.indexOf('#');
                }
                String r = idx == -1 ? pushUrl : pushUrl.substring(0, idx);
                int length = validUrl.length() - 1;
                String trimmed = validUrl.substring(0, length);
                // 当 '*' 直接附在 authority 上（scheme:// 后无路径分隔符）时，
                // 裸前缀匹配也会接受仅以白名单主机开头的 host。
                int schemeIdx = trimmed.indexOf("://");
                if (schemeIdx >= 0 && trimmed.indexOf('/', schemeIdx + 3) == -1
                        && !trimmed.equals(r) && !r.startsWith(trimmed + "/")) {
                    continue;
                }
                if (r.startsWith(trimmed)) {
                    return trimmed;
                }
                if (length - 1 > 0 && trimmed.charAt(length - 1) == '/') {
                    length--;
                }
                trimmed = validUrl.substring(0, length);
                if (trimmed.equals(r)) {
                    return trimmed;
                }
            } else if (validUrl.equals(pushUrl)) {
                return validUrl;
            }
        }
        return null;
    }

    /**
     * 校验接收方 URL 的 scheme 与主机类别。{@link #allowInsecure} 为 {@code false} 时
     * URL 须为 {@code https}，主机不得解析为环回/链路本地/站点本地/唯一本地/组播/任意本地地址。
     * 无法解析的主机名接受——推送到不可解析主机会在投递时无害失败，非 SSRF。
     *
     * <p>谓词组合（{@code isLoopbackAddress() || isAnyLocalAddress() || ...} 及 IPv6 唯一本地检查）
     * 镜像 {@code keycloak-common} 中 {@code SslRequired.isLocal}。
     */
    private void validateSchemeAndHost(String pushUrl) {
        if (allowInsecure) {
            return;
        }
        URI uri;
        try {
            uri = new URI(pushUrl);
        } catch (Exception e) {
            throw new SsfPushUrlValidationException(Reason.URL_MALFORMED,
                    "delivery.endpoint_url is not a valid URI");
        }
        String scheme = uri.getScheme();
        if (scheme == null || !scheme.equalsIgnoreCase("https")) {
            throw new SsfPushUrlValidationException(Reason.SCHEME_INSECURE,
                    "delivery.endpoint_url must use https; set the "
                            + "allow-insecure-push-targets SPI option to permit http for closed-network deployments");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new SsfPushUrlValidationException(Reason.URL_MALFORMED,
                    "delivery.endpoint_url must include a host");
        }
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            // 配置时无法解析。接受——接收方 push 在投递时失败，非 SSRF。
            return;
        }
        for (InetAddress address : addresses) {
            if (isPrivateOrLoopback(address)) {
                throw new SsfPushUrlValidationException(Reason.HOST_PRIVATE,
                        "delivery.endpoint_url resolves to a non-routable, loopback, link-local,"
                                + " or private-network address (" + address.getHostAddress() + "); set the "
                                + "allow-insecure-push-targets SPI option to permit this for closed-network deployments");
            }
        }
    }

    private static boolean isPrivateOrLoopback(InetAddress address) {
        return address.isLoopbackAddress()
                || address.isAnyLocalAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()
                || isUniqueLocalIpv6(address);
    }

    /**
     * IPv6 唯一本地地址范围 fc00::/7（RFC 4193）。镜像 {@code SslRequired.isUniqueLocal}。
     */
    private static boolean isUniqueLocalIpv6(InetAddress address) {
        if (!(address instanceof Inet6Address)) {
            return false;
        }
        byte[] bytes = address.getAddress();
        return ((byte) (bytes[0] & 0b11111110)) == (byte) 0xFC;
    }

    /** 稳定原因码，供调用方映射异常到特定 HTTP 响应或日志。 */
    public enum Reason {
        /** URL 缺失 */
        URL_MISSING,
        /** URL 格式错误 */
        URL_MALFORMED,
        /** 白名单为空 */
        ALLOWLIST_EMPTY,
        /** 不在白名单中 */
        NOT_IN_ALLOWLIST,
        /** 非安全 scheme */
        SCHEME_INSECURE,
        /** 主机为私有/环回地址 */
        HOST_PRIVATE
    }

    /**
     * 专用 {@link SsfException}，携带稳定 {@link Reason}，测试/集成可映射拒绝原因而无需字符串匹配用户消息。
     * 基类 {@code SsfException} 已由现有异常映射器映射为 HTTP 400，调用方抛出即可向接收方返回清晰拒绝。
     */
    public static class SsfPushUrlValidationException extends SsfException {

        /** 拒绝原因码 */
        private final Reason reason;

        /** @param reason 原因码 @param message 用户可见消息 */
        public SsfPushUrlValidationException(Reason reason, String message) {
            super(message);
            this.reason = reason;
        }

        /** 返回拒绝原因码。 */
        public Reason getReason() {
            return reason;
        }
    }
}
