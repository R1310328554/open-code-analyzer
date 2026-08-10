/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.condition;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.ClientPolicyVote;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.logging.Logger;

/**
 * 客户端更新者主机条件：按发起客户端创建/更新请求的远程 IP 或域名匹配策略。
 * <p>支持精确主机名/IP 匹配及 {@code *.domain} 通配域名后缀匹配。</p>
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientUpdaterSourceHostsCondition extends AbstractClientPolicyConditionProvider<ClientUpdaterSourceHostsCondition.Configuration> {

    private static final Logger logger = Logger.getLogger(ClientUpdaterSourceHostsCondition.class);

    /** @param session Keycloak 会话 */
    public ClientUpdaterSourceHostsCondition(KeycloakSession session) {
        super(session);
    }

    /** {@inheritDoc} @return 条件配置类型 */
    @Override
    public Class<Configuration> getConditionConfigurationClass() {
        return Configuration.class;
    }


    /** 条件配置：受信任的主机名或域名列表（{@code *.example.com} 表示域名后缀）。 */
    public static class Configuration extends ClientPolicyConditionConfigurationRepresentation {

        @JsonProperty("trusted-hosts")
        protected List<String> trustedHosts;

        /** @return 受信任主机/域名配置列表 */
        public List<String> getTrustedHosts() {
            return trustedHosts;
        }

        /** @param trustedHosts 受信任主机/域名列表 */
        public void setTrustedHosts(List<String> trustedHosts) {
            this.trustedHosts = trustedHosts;
        }
    }

    /** {@inheritDoc} @return {@link ClientUpdaterSourceHostsConditionFactory#PROVIDER_ID} */
    @Override
    public String getProviderId() {
        return ClientUpdaterSourceHostsConditionFactory.PROVIDER_ID;
    }

    /** {@inheritDoc} 在客户端 CRUD 事件上按远程主机投票 */
    @Override
    public ClientPolicyVote applyPolicy(ClientPolicyContext context) throws ClientPolicyException {
        switch (context.getEvent()) {
        case REGISTER:
        case UPDATE:
        case REGISTERED:
        case UPDATED:
            if (isHostMatched()) return ClientPolicyVote.YES;
            return ClientPolicyVote.NO;
        default:
            return ClientPolicyVote.ABSTAIN;
        }
    }

    /** 判断当前请求的远程地址是否匹配受信任主机或域名。 */
    private boolean isHostMatched() {
        String hostAddress = session.getContext().getConnection().getRemoteAddr();

        logger.tracev("Verifying remote host = {0}", session.getContext().getConnection().getRemoteHost());

        if (hostAddress == null) {
            return false;
        }

        List<String> trustedHosts = getTrustedHosts();
        List<String> trustedDomains = getTrustedDomains();

        // 先按 IP 地址匹配非通配主机名
        String verifiedHost = verifyHostInTrustedHosts(hostAddress, trustedHosts);
        if (verifiedHost != null) {
            return true;
        }

        // 再按 DNS 反向解析的主机名后缀匹配通配域名
        verifiedHost = verifyHostInTrustedDomains(hostAddress, trustedDomains);
        if (verifiedHost != null) {
            return true;
        }

        return false;
    }

    /** 从配置提取非 {@code *.} 前缀的精确主机名列表。 @return 主机名列表 */
    protected List<String> getTrustedHosts() {
        List<String> trustedHostsConfig = configuration.getTrustedHosts();
        return trustedHostsConfig.stream().filter((String hostname) -> {
            return !hostname.startsWith("*.");
        }).collect(Collectors.toList());
    }

    /** 从配置提取 {@code *.domain} 形式的域名后缀列表。 @return 域名后缀列表 */
    protected List<String> getTrustedDomains() {
        List<String> trustedHostsConfig = configuration.getTrustedHosts();
        List<String> domains = new LinkedList<>();

        for (String hostname : trustedHostsConfig) {
            if (hostname.startsWith("*.")) {
                hostname = hostname.substring(2);
                domains.add(hostname);
            }
        }

        return domains;
    }

    /**
     * 将配置主机名解析为 IP 并与请求地址比较。
     * @param hostAddress 请求远程 IP
     * @param trustedHosts 精确主机名列表
     * @return 匹配的主机名，未匹配时 {@code null}
     */
        for (String confHostName : trustedHosts) {
            try {
                String hostIPAddress = InetAddress.getByName(confHostName).getHostAddress();
                logger.tracev("Trying host {0} of address {1}", confHostName, hostIPAddress);
                if (hostIPAddress.equals(hostAddress)) {
                    logger.tracev("Successfully verified host = {0}", confHostName);
                    return confHostName;
                }
            } catch (UnknownHostException uhe) {
                logger.tracev("Unknown host from realm configuration = {0}", confHostName);
            }
        }

        return null;
    }

    /**
     * 反向解析请求 IP 为主机名并检查是否以受信任域名后缀结尾。
     * @param hostAddress 请求远程 IP
     * @param trustedDomains 域名后缀列表
     * @return 匹配的主机名，未匹配时 {@code null}
     */
        if (!trustedDomains.isEmpty()) {
            try {
                String hostname = InetAddress.getByName(hostAddress).getHostName();
                logger.tracev("Trying verify request from address {0} of host {1} by domains", hostAddress, hostname);
                for (String confDomain : trustedDomains) {
                    if (hostname.endsWith(confDomain)) {
                        logger.tracev("Successfully verified host {0} by trusted domain {1}", hostname, confDomain);
                        return hostname;
                    }
                }
            } catch (UnknownHostException uhe) {
                logger.tracev("Request of address {0} came from unknown host. Skip verification by domains", hostAddress);
            }
        }

        return null;
    }
}
