/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientregistration.policy.impl;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.PairwiseSubMapperUtils;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.clientregistration.ClientRegistrationContext;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicyException;

import org.jboss.logging.Logger;

/**
 * 受信任主机客户端注册策略。
 * <p>校验发起注册请求的远程主机 IP/域名，以及客户端配置中的 rootUrl、baseUrl、adminUrl 与 redirectUris 是否落在受信任主机或域名列表内。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TrustedHostClientRegistrationPolicy implements ClientRegistrationPolicy {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(TrustedHostClientRegistrationPolicy.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 策略组件模型 */
    private final ComponentModel componentModel;

    /** 构造受信任主机注册策略。
     * @param session Keycloak 会话
     * @param componentModel 策略组件配置
     */
    public TrustedHostClientRegistrationPolicy(KeycloakSession session, ComponentModel componentModel) {
        this.session = session;
        this.componentModel = componentModel;
    }

    /** {@inheritDoc} 注册前校验远程主机与客户端 URL */
    @Override
    public void beforeRegister(ClientRegistrationContext context) throws ClientRegistrationPolicyException {
        verifyHost();
        verifyClientUrls(context);
    }

    /** {@inheritDoc} 注册后无额外处理 */
    @Override
    public void afterRegister(ClientRegistrationContext context, ClientModel clientModel) {
    }


    /** {@inheritDoc} 更新前校验远程主机与客户端 URL */
    @Override
    public void beforeUpdate(ClientRegistrationContext context, ClientModel clientModel) throws ClientRegistrationPolicyException {
        verifyHost();
        verifyClientUrls(context);
    }

    /** {@inheritDoc} 更新后无额外处理 */
    @Override
    public void afterUpdate(ClientRegistrationContext context, ClientModel clientModel) {

    }

    /** {@inheritDoc} 查看前校验远程主机 */
    @Override
    public void beforeView(ClientRegistrationProvider provider, ClientModel clientModel) throws ClientRegistrationPolicyException {
        verifyHost();
    }

    /** {@inheritDoc} 删除前校验远程主机 */
    @Override
    public void beforeDelete(ClientRegistrationProvider provider, ClientModel clientModel) throws ClientRegistrationPolicyException {
        verifyHost();
    }

    // 实现细节

    /** 若启用主机匹配，校验当前请求的远程地址是否在受信任列表中。
     * @throws ClientRegistrationPolicyException 主机不受信任时抛出
     */
    protected void verifyHost() throws ClientRegistrationPolicyException {
        boolean hostMustMatch = isHostMustMatch();
        if (!hostMustMatch) {
            return;
        }

        String hostAddress = session.getContext().getConnection().getRemoteAddr();

        logger.debugf("Verifying remote host : %s", session.getContext().getConnection().getRemoteHost());

        if (!verifyHost(hostAddress)) {
            ServicesLogger.LOGGER.failedToVerifyRemoteHost(session.getContext().getConnection().getRemoteHost());
            throw new ClientRegistrationPolicyException("Host not trusted.");
        }
    }

    /** 按 IP 与域名规则校验给定主机地址。
     * @param hostAddress 远程 IP 或主机名
     * @return 受信任时返回 true
     */
    protected boolean verifyHost(String hostAddress) {
        if (hostAddress == null) {
            return false;
        }
        List<String> trustedHosts = getTrustedHosts();
        List<String> trustedDomains = getTrustedDomains();

        // 通过 IP 地址匹配受信任主机列表
        String verifiedHost = verifyHostInTrustedHosts(hostAddress, trustedHosts);
        if (verifiedHost != null) {
            return true;
        }

        // 若 IP 对应的主机名属于受信任域名则通过（依赖正确 DNS 配置）
        verifiedHost = verifyHostInTrustedDomains(hostAddress, trustedDomains);
        if (verifiedHost != null) {
            return true;
        }

        return false;
    }


    /** 返回不含通配符前缀的受信任主机名/IP 列表 */
    protected List<String> getTrustedHosts() {
        List<String> trustedHostsConfig = componentModel.getConfig().getOrDefault(TrustedHostClientRegistrationPolicyFactory.TRUSTED_HOSTS, Collections.emptyList());
        return trustedHostsConfig.stream().filter((String hostname) -> !hostname.startsWith("*.")).toList();
    }


    /** 返回完整受信任主机/域名配置（含 {@code *.} 通配符项） */
    protected List<String> getTrustedDomains() {
        return componentModel.getConfig().getOrDefault(TrustedHostClientRegistrationPolicyFactory.TRUSTED_HOSTS, Collections.emptyList());
    }

    /** 将远程地址与受信任主机名解析后的 IP 逐一比对。
     * @return 匹配成功时返回对应配置主机名，否则 null
     */
    protected String verifyHostInTrustedHosts(String hostAddress, List<String> trustedHosts) {
        for (String confHostName : trustedHosts) {
            try {
                String hostIPAddress = InetAddress.getByName(confHostName).getHostAddress();

                logger.tracef("Trying host '%s' of address '%s'", confHostName, hostIPAddress);
                if (hostIPAddress.equals(hostAddress)) {
                    logger.debugf("Successfully verified host : %s", confHostName);
                    return confHostName;
                }
            } catch (UnknownHostException uhe) {
                logger.debugf(uhe, "Unknown host from realm configuration: %s", confHostName);
            }
        }

        return null;
    }

    /** 判断主机名是否匹配受信任域名（支持 {@code *.domain} 通配符）。 */
    private boolean checkTrustedDomain(String hostname, String trustedDomain) {
        if (trustedDomain.startsWith("*.")) {
            String domain = trustedDomain.substring(2);
            return hostname.equals(domain) || hostname.endsWith("." + domain);
        }
        return hostname.equals(trustedDomain);
    }

    /** 通过反向/正向 DNS 解析校验主机名是否属于受信任域名。
     * @return 校验通过时返回主机名，否则 null
     */
    protected String verifyHostInTrustedDomains(String hostAddress, List<String> trustedDomains) {
        try {
            InetAddress address = InetAddress.getByName(hostAddress);
            String hostname = address.getHostName();

            logger.debugf("Trying verify request from address '%s' of host '%s' by domains", hostAddress, hostname);

            // Windows 上环回地址反向解析可能返回 IP 而非 localhost
            // 统一规范为 localhost 以便域名规则一致
            if (address.isLoopbackAddress()) {
                hostname = "localhost";
            } else if (hostname.equals(address.getHostAddress())) {
                logger.debugf("The hostAddress '%s' was not resolved to a hostname", hostAddress);
                return null;
            }

            // 非环回地址需正向确认：主机名解析结果须包含原始 IP
            if (!address.isLoopbackAddress() && Arrays.stream(InetAddress.getAllByName(hostname)).noneMatch(a -> address.equals(a))) {
                logger.debugf("The hostAddress '%s' is not among the direct lookups returned resolving '%s'", hostAddress, hostname);
                return null;
            }

            for (String confDomain : trustedDomains) {
                if (checkTrustedDomain(hostname, confDomain)) {
                    logger.debugf("Successfully verified host '%s' by trusted domain '%s'", hostname, confDomain);
                    return hostname;
                }
            }
        } catch (UnknownHostException uhe) {
            logger.debugf(uhe, "Request of address '%s' came from unknown host. Skip verification by domains unless it's within localhost domain", hostAddress);

            String lower = hostAddress == null ? null : hostAddress.toLowerCase();
            if (lower != null && ("localhost".equals(lower) || lower.endsWith(".localhost"))) {
                for (String confDomain : trustedDomains) {
                    if (checkTrustedDomain(lower, confDomain)) {
                        logger.debugf("Treating host '%s' as loopback due to localhost domain and returning success by trusted domain '%s'", lower, confDomain);
                        return lower;
                    }
                }
            }
        }

        return null;
    }


    /** 校验客户端 representation 中各 URL 的主机是否在受信任列表内。
     * @param context 注册上下文
     * @throws ClientRegistrationPolicyException URL 不在白名单或格式错误时抛出
     */
    protected void verifyClientUrls(ClientRegistrationContext context) throws ClientRegistrationPolicyException {
        boolean redirectUriMustMatch = isClientUrisMustMatch();
        if (!redirectUriMustMatch) {
            return;
        }

        List<String> trustedHosts = getTrustedHosts();
        List<String> trustedDomains = getTrustedDomains();

        ClientRepresentation client = context.getClient();
        String rootUrl = client.getRootUrl();
        String baseUrl = client.getBaseUrl();
        String adminUrl = client.getAdminUrl();
        List<String> redirectUris = client.getRedirectUris();

        baseUrl = relativeToAbsoluteURI(rootUrl, baseUrl);
        adminUrl = relativeToAbsoluteURI(rootUrl, adminUrl);
        Set<String> resolvedRedirects = PairwiseSubMapperUtils.resolveValidRedirectUris(rootUrl, redirectUris);

        if (rootUrl != null) {
            checkURLTrusted(rootUrl, trustedHosts, trustedDomains);
        }

        if (baseUrl != null) {
            checkURLTrusted(baseUrl, trustedHosts, trustedDomains);
        }
        if (adminUrl != null) {
            checkURLTrusted(adminUrl, trustedHosts, trustedDomains);
        }
        for (String redirect : resolvedRedirects) {
            checkURITrusted(redirect, trustedHosts, trustedDomains);
        }

    }

    /** 校验 HTTP(S) URL 的主机是否受信任。
     * @throws ClientRegistrationPolicyException URL 格式错误或主机不受信任
     */
    protected void checkURLTrusted(String url, List<String> trustedHosts, List<String> trustedDomains) throws ClientRegistrationPolicyException {
        try {
            String host = new URL(url).getHost();

            if (checkHostTrusted(host, trustedHosts, trustedDomains)) {
                return;
            }
        } catch (MalformedURLException mfe) {
            logger.debugf(mfe, "URL '%s' is malformed", url);
            throw new ClientRegistrationPolicyException("URL is malformed");
        }

        ServicesLogger.LOGGER.urlDoesntMatch(url);
        throw new ClientRegistrationPolicyException("URL doesn't match any trusted host or trusted domain");
    }

    /** 校验 URI（如自定义 scheme 重定向）的主机是否受信任。
     * @throws ClientRegistrationPolicyException URI 格式错误或主机不受信任
     */
    protected void checkURITrusted(String uri, List<String> trustedHosts, List<String> trustedDomains) throws ClientRegistrationPolicyException {
        try {
            String host = new URI(uri).getHost();

            if (checkHostTrusted(host, trustedHosts, trustedDomains)) {
                return;
            }
        } catch (URISyntaxException use) {
            logger.debugf(use, "URI '%s' is malformed", uri);
            throw new ClientRegistrationPolicyException("URI is malformed");
        }

        ServicesLogger.LOGGER.uriDoesntMatch(uri);
        throw new ClientRegistrationPolicyException("URI doesn't match any trusted host or trusted domain");
    }

    /** 判断主机名是否匹配受信任主机或域名列表。 */
    private boolean checkHostTrusted(String host, List<String> trustedHosts, List<String> trustedDomains) {
        for (String trustedHost : trustedHosts) {
            if (host.equals(trustedHost)) {
                return true;
            }
        }

        for (String trustedDomain : trustedDomains) {
            if (checkTrustedDomain(host, trustedDomain)) {
                return true;
            }
        }

        return false;
    }


    /** 将以 {@code /} 开头的相对 URI 基于 rootUrl 拼接为绝对地址。 */
    private static String relativeToAbsoluteURI(String rootUrl, String relative) {
        if (relative == null) {
            return null;
        }

        if (!relative.startsWith("/")) {
            return relative;
        } else if (rootUrl == null || rootUrl.isEmpty()) {
            return null;
        }

        return rootUrl + relative;
    }

    /** 是否要求发起注册请求的主机必须匹配受信任列表。 */
    boolean isHostMustMatch() {
        return parseBoolean(TrustedHostClientRegistrationPolicyFactory.HOST_SENDING_REGISTRATION_REQUEST_MUST_MATCH);
    }

    /** 是否要求客户端 URL 必须匹配受信任主机/域名。 */
    boolean isClientUrisMustMatch() {
        return parseBoolean(TrustedHostClientRegistrationPolicyFactory.CLIENT_URIS_MUST_MATCH);
    }

    // 缺省为 true
    /** 从组件配置读取布尔值，未配置时默认为 true。 */
    private boolean parseBoolean(String propertyKey) {
        String val = componentModel.getConfig().getFirst(propertyKey);
        return val == null || Boolean.parseBoolean(val);
    }
}
