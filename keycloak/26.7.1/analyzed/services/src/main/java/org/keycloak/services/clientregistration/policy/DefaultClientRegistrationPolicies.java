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

package org.keycloak.services.clientregistration.policy;

import java.util.Arrays;
import java.util.Collections;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.RealmModel;
import org.keycloak.protocol.oidc.mappers.AddressMapper;
import org.keycloak.protocol.oidc.mappers.FullNameMapper;
import org.keycloak.protocol.oidc.mappers.SHA256PairwiseSubMapper;
import org.keycloak.protocol.oidc.mappers.UserAttributeMapper;
import org.keycloak.protocol.oidc.mappers.UserPropertyMapper;
import org.keycloak.protocol.saml.mappers.RoleListMapper;
import org.keycloak.protocol.saml.mappers.UserAttributeStatementMapper;
import org.keycloak.protocol.saml.mappers.UserPropertyAttributeStatementMapper;
import org.keycloak.services.clientregistration.policy.impl.ClientScopesClientRegistrationPolicyFactory;
import org.keycloak.services.clientregistration.policy.impl.ConsentRequiredClientRegistrationPolicyFactory;
import org.keycloak.services.clientregistration.policy.impl.MaxClientsClientRegistrationPolicyFactory;
import org.keycloak.services.clientregistration.policy.impl.ProtocolMappersClientRegistrationPolicyFactory;
import org.keycloak.services.clientregistration.policy.impl.RegistrationWebOriginsPolicyFactory;
import org.keycloak.services.clientregistration.policy.impl.ScopeClientRegistrationPolicyFactory;
import org.keycloak.services.clientregistration.policy.impl.TrustedHostClientRegistrationPolicyFactory;

/**
 * 默认客户端注册策略初始化。
 * <p>当领域尚未配置任何 {@link ClientRegistrationPolicy} 组件时，为匿名与已认证注册路径注入受信任主机、同意要求、协议映射器白名单等内置策略。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultClientRegistrationPolicies {

    /** 默认允许的内置协议映射器 Provider ID 列表 */
    private static String[] DEFAULT_ALLOWED_PROTOCOL_MAPPERS = {
            UserAttributeStatementMapper.PROVIDER_ID,
            UserAttributeMapper.PROVIDER_ID,
            UserPropertyAttributeStatementMapper.PROVIDER_ID,
            UserPropertyMapper.PROVIDER_ID,
            FullNameMapper.PROVIDER_ID,
            AddressMapper.PROVIDER_ID,
            new SHA256PairwiseSubMapper().getId(),
            RoleListMapper.PROVIDER_ID
    };

    /** 若领域无注册策略组件，则添加匿名与已认证的默认策略集。
     * @param realm 目标领域模型
     */
    public static void addDefaultPolicies(RealmModel realm) {
        String anonPolicyType = ClientRegistrationPolicyManager.getComponentTypeKey(RegistrationAuth.ANONYMOUS);
        String authPolicyType = ClientRegistrationPolicyManager.getComponentTypeKey(RegistrationAuth.AUTHENTICATED);

        // 若管理员故意删除全部策略，此处不会重新注入（可能为已知限制）
        if (realm.getComponentsStream(realm.getId(), ClientRegistrationPolicy.class.getName()).count() == 0) {
            addAnonymousPolicies(realm, anonPolicyType);
            addAuthPolicies(realm, authPolicyType);
        }
    }

    /** 创建策略组件模型的通用工厂方法。
     * @param name 组件显示名称
     * @param realm 所属领域
     * @param providerId 策略 Provider ID
     * @param policyType 策略 subType（anonymous/authenticated）
     * @return 未持久化的组件模型
     */
    private static ComponentModel createModelInstance(String name, RealmModel realm, String providerId, String policyType) {
        ComponentModel model = new ComponentModel();
        model.setName(name);
        model.setParentId(realm.getId());
        model.setProviderId(providerId);
        model.setProviderType(ClientRegistrationPolicy.class.getName());
        model.setSubType(policyType);
        return model;
    }

    /** 为匿名注册路径添加默认策略（受信任主机、同意、范围、客户端上限等）。
     * @param realm 目标领域
     * @param policyTypeKey 策略 subType 键
     */
    private static void addAnonymousPolicies(RealmModel realm, String policyTypeKey) {
        ComponentModel trustedHostModel = createModelInstance("Trusted Hosts", realm, TrustedHostClientRegistrationPolicyFactory.PROVIDER_ID, policyTypeKey);

        // 默认不配置任何受信任主机
        trustedHostModel.getConfig().put(TrustedHostClientRegistrationPolicyFactory.TRUSTED_HOSTS, Collections.emptyList());
        trustedHostModel.getConfig().putSingle(TrustedHostClientRegistrationPolicyFactory.HOST_SENDING_REGISTRATION_REQUEST_MUST_MATCH, "true");
        trustedHostModel.getConfig().putSingle(TrustedHostClientRegistrationPolicyFactory.CLIENT_URIS_MUST_MATCH, "true");
        realm.addComponentModel(trustedHostModel);

        ComponentModel consentRequiredModel = createModelInstance("Consent Required", realm, ConsentRequiredClientRegistrationPolicyFactory.PROVIDER_ID, policyTypeKey);
        realm.addComponentModel(consentRequiredModel);

        ComponentModel scopeModel = createModelInstance("Full Scope Disabled", realm, ScopeClientRegistrationPolicyFactory.PROVIDER_ID, policyTypeKey);
        realm.addComponentModel(scopeModel);

        ComponentModel maxClientsModel = createModelInstance("Max Clients Limit", realm, MaxClientsClientRegistrationPolicyFactory.PROVIDER_ID, policyTypeKey);
        maxClientsModel.put(MaxClientsClientRegistrationPolicyFactory.MAX_CLIENTS, MaxClientsClientRegistrationPolicyFactory.DEFAULT_MAX_CLIENTS);
        realm.addComponentModel(maxClientsModel);

        addGenericPolicies(realm, policyTypeKey);
    }


    /** 为已认证注册路径添加通用策略。
     * @param realm 目标领域
     * @param policyTypeKey 策略 subType 键
     */
    private static void addAuthPolicies(RealmModel realm, String policyTypeKey) {
        addGenericPolicies(realm, policyTypeKey);
    }

    /** 添加匿名与已认证路径共用的策略（协议映射器、客户端范围、注册 Web Origins）。
     * @param realm 目标领域
     * @param policyTypeKey 策略 subType 键
     */
    private static void addGenericPolicies(RealmModel realm, String policyTypeKey) {
        ComponentModel protMapperModel = createModelInstance("Allowed Protocol Mapper Types", realm, ProtocolMappersClientRegistrationPolicyFactory.PROVIDER_ID, policyTypeKey);
        protMapperModel.getConfig().put(ProtocolMappersClientRegistrationPolicyFactory.ALLOWED_PROTOCOL_MAPPER_TYPES, Arrays.asList(DEFAULT_ALLOWED_PROTOCOL_MAPPERS));
        realm.addComponentModel(protMapperModel);

        ComponentModel clientTemplatesModel = createModelInstance("Allowed Client Scopes", realm, ClientScopesClientRegistrationPolicyFactory.PROVIDER_ID, policyTypeKey);
        clientTemplatesModel.getConfig().put(ClientScopesClientRegistrationPolicyFactory.ALLOWED_CLIENT_SCOPES, Collections.emptyList());
        clientTemplatesModel.put(ClientScopesClientRegistrationPolicyFactory.ALLOW_DEFAULT_SCOPES, true);
        realm.addComponentModel(clientTemplatesModel);

        ComponentModel allowedRegistrationWebOrigins = createModelInstance("Allowed Registration Web Origins", realm, RegistrationWebOriginsPolicyFactory.PROVIDER_ID, policyTypeKey);
        allowedRegistrationWebOrigins.getConfig().put(RegistrationWebOriginsPolicyFactory.WEB_ORIGINS, Collections.emptyList());
        realm.addComponentModel(allowedRegistrationWebOrigins);
    }

}
