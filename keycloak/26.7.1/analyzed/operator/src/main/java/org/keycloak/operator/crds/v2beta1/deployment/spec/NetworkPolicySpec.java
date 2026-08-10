/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.operator.crds.v2beta1.deployment.spec;

import java.util.List;
import java.util.Optional;

import org.keycloak.operator.Constants;
import org.keycloak.operator.crds.v2beta1.CRDUtils;
import org.keycloak.operator.crds.v2beta1.deployment.Keycloak;
import org.keycloak.operator.crds.v2beta1.deployment.KeycloakSpec;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.generator.annotation.Default;
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyPeer;
import io.sundr.builder.annotations.Buildable;

/**
 * Keycloak NetworkPolicy 配置规范，按端口维度限制入站流量来源。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Buildable(editableEnabled = false, builderPackage = "io.fabric8.kubernetes.api.builder")
public class NetworkPolicySpec {

    // 摘自 Kubernetes 文档：描述 NetworkPolicyPeer 列表的语义（OR 组合、空列表表示不限制来源）
    private static final String RULE_DESCRIPTION = "A list of sources which should be able to access this endpoint. " +
            "Items in this list are combined using a logical OR operation. " +
            "If this field is empty or missing, this rule matches all sources (traffic not restricted by source). " +
            "If this field is present and contains at least one item, this rule allows traffic only if the traffic matches at least one item in the from list.";

    /** 是否启用 NetworkPolicy 入站流量控制，默认为 true。 */
    @JsonProperty("enabled")
    @JsonPropertyDescription("Enables or disables the ingress traffic control.")
    @Default("true")
    private boolean networkPolicyEnabled = true;

    /** 允许访问 HTTP 端口的来源规则列表。 */
    @JsonProperty("http")
    @JsonPropertyDescription(RULE_DESCRIPTION)
    private List<NetworkPolicyPeer> httpRules;

    /** 允许访问 HTTPS 端口的来源规则列表。 */
    @JsonProperty("https")
    @JsonPropertyDescription(RULE_DESCRIPTION)
    private List<NetworkPolicyPeer> httpsRules;

    /** 允许访问管理端口的来源规则列表。 */
    @JsonProperty("management")
    @JsonPropertyDescription(RULE_DESCRIPTION)
    private List<NetworkPolicyPeer> managementRules;

    public boolean isNetworkPolicyEnabled() {
        return networkPolicyEnabled;
    }

    public void setNetworkPolicyEnabled(boolean networkPolicyEnabled) {
        this.networkPolicyEnabled = networkPolicyEnabled;
    }

    public List<NetworkPolicyPeer> getHttpRules() {
        return httpRules;
    }

    public void setHttpRules(List<NetworkPolicyPeer> httpRules) {
        this.httpRules = httpRules;
    }

    public List<NetworkPolicyPeer> getHttpsRules() {
        return httpsRules;
    }

    public void setHttpsRules(List<NetworkPolicyPeer> httpsRules) {
        this.httpsRules = httpsRules;
    }

    public List<NetworkPolicyPeer> getManagementRules() {
        return managementRules;
    }

    public void setManagementRules(List<NetworkPolicyPeer> managementRules) {
        this.managementRules = managementRules;
    }

    /** 从 Keycloak CR 提取 {@link NetworkPolicySpec} 配置。 */
    public static Optional<NetworkPolicySpec> networkPolicySpecOf(Keycloak keycloak) {
        return CRDUtils.keycloakSpecOf(keycloak)
                .map(KeycloakSpec::getNetworkPolicySpec);
    }

    /** 判断 Keycloak CR 是否启用了 NetworkPolicy，未配置时默认为 true。 */
    public static boolean isNetworkPolicyEnabled(Keycloak keycloak) {
        return networkPolicySpecOf(keycloak)
                .map(NetworkPolicySpec::isNetworkPolicyEnabled)
                .orElse(true);
    }

    /** 生成 Keycloak 实例对应的 NetworkPolicy 资源名称。 */
    public static String networkPolicyName(Keycloak keycloak) {
        return keycloak.getMetadata().getName() + Constants.KEYCLOAK_NETWORK_POLICY_SUFFIX;
    }

    /** 解析 HTTP 端口的 NetworkPolicy 来源规则。 */
    public static Optional<List<NetworkPolicyPeer>> httpRules(Keycloak keycloak) {
        return networkPolicySpecOf(keycloak)
                .map(NetworkPolicySpec::getHttpRules);
    }

    /** 解析 HTTPS 端口的 NetworkPolicy 来源规则。 */
    public static Optional<List<NetworkPolicyPeer>> httpsRules(Keycloak keycloak) {
        return networkPolicySpecOf(keycloak)
                .map(NetworkPolicySpec::getHttpsRules);
    }

    /** 解析管理端口的 NetworkPolicy 来源规则。 */
    public static Optional<List<NetworkPolicyPeer>> managementRules(Keycloak keycloak) {
        return networkPolicySpecOf(keycloak)
                .map(NetworkPolicySpec::getManagementRules);
    }

}
