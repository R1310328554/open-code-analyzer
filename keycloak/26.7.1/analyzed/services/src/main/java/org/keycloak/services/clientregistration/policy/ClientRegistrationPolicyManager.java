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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.keycloak.component.ComponentModel;
import org.keycloak.events.Details;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.clientregistration.ClientRegistrationContext;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;

import org.jboss.logging.Logger;

/**
 * 客户端注册策略管理器。
 * <p>在注册、更新、查看与删除客户端时，按 {@link RegistrationAuth} 类型触发领域配置的 {@link ClientRegistrationPolicy} 组件，并汇总各策略允许的 Web Origins。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientRegistrationPolicyManager {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(ClientRegistrationPolicyManager.class);

    /** 注册客户端前触发所有匹配策略的 {@code beforeRegister} 钩子。
     * @param context 客户端注册上下文
     * @param authType 注册认证类型（匿名或已认证）
     * @throws ClientRegistrationPolicyException 策略拒绝时抛出
     */
    public static void triggerBeforeRegister(ClientRegistrationContext context, RegistrationAuth authType) throws ClientRegistrationPolicyException  {
        triggerPolicies(context.getSession(), context.getProvider(), authType, "before register client", (ClientRegistrationPolicy policy) -> {

            policy.beforeRegister(context);

        });
    }

    /** 注册客户端后触发所有匹配策略的 {@code afterRegister} 钩子。
     * @param context 客户端注册上下文
     * @param authType 注册认证类型
     * @param client 已创建的客户端模型
     */
    public static void triggerAfterRegister(ClientRegistrationContext context, RegistrationAuth authType, ClientModel client) {
        try {
            triggerPolicies(context.getSession(), context.getProvider(), authType, "after register client " + client.getClientId(), (ClientRegistrationPolicy policy) -> {

                policy.afterRegister(context, client);

            });
        } catch (ClientRegistrationPolicyException crpe) {
            throw new IllegalStateException(crpe);
        }
    }


    /** 更新客户端前触发所有匹配策略的 {@code beforeUpdate} 钩子。
     * @param context 客户端注册上下文
     * @param authType 注册认证类型
     * @param client 待更新的客户端模型
     * @throws ClientRegistrationPolicyException 策略拒绝时抛出
     */
    public static void triggerBeforeUpdate(ClientRegistrationContext context, RegistrationAuth authType, ClientModel client) throws ClientRegistrationPolicyException  {
        triggerPolicies(context.getSession(), context.getProvider(), authType, "before update client " + client.getClientId(), (ClientRegistrationPolicy policy) -> {

            policy.beforeUpdate(context, client);

        });
    }

    /** 更新客户端后触发所有匹配策略的 {@code afterUpdate} 钩子。
     * @param context 客户端注册上下文
     * @param authType 注册认证类型
     * @param client 已更新的客户端模型
     */
    public static void triggerAfterUpdate(ClientRegistrationContext context, RegistrationAuth authType, ClientModel client) {
        try {
            triggerPolicies(context.getSession(), context.getProvider(), authType, "after update client " + client.getClientId(), (ClientRegistrationPolicy policy) -> {

                policy.afterUpdate(context, client);

            });
        } catch (ClientRegistrationPolicyException crpe) {
            throw new IllegalStateException(crpe);
        }
    }

    /** 查看客户端前触发所有匹配策略的 {@code beforeView} 钩子。
     * @param session Keycloak 会话
     * @param provider 客户端注册 Provider
     * @param authType 注册认证类型
     * @param client 待查看的客户端模型
     * @throws ClientRegistrationPolicyException 策略拒绝时抛出
     */
    public static void triggerBeforeView(KeycloakSession session, ClientRegistrationProvider provider, RegistrationAuth authType, ClientModel client) throws ClientRegistrationPolicyException {
        triggerPolicies(session, provider, authType, "before view client " + client.getClientId(), (ClientRegistrationPolicy policy) -> {

            policy.beforeView(provider, client);

        });
    }

    /** 删除客户端前触发所有匹配策略的 {@code beforeDelete} 钩子。
     * @param session Keycloak 会话
     * @param provider 客户端注册 Provider
     * @param authType 注册认证类型
     * @param client 待删除的客户端模型
     * @throws ClientRegistrationPolicyException 策略拒绝时抛出
     */
    public static void triggerBeforeRemove(KeycloakSession session, ClientRegistrationProvider provider, RegistrationAuth authType, ClientModel client) throws ClientRegistrationPolicyException {
        triggerPolicies(session, provider, authType, "before delete client " + client.getClientId(), (ClientRegistrationPolicy policy) -> {

            policy.beforeDelete(provider, client);

        });
    }



    /** 汇总指定认证类型下所有策略声明的允许 Web Origins。
     * @param session Keycloak 会话
     * @param authType 注册认证类型
     * @return 允许的 Origin 列表
     */
    public static List<String> getAllowedOrigins(KeycloakSession session, RegistrationAuth authType) {
        RealmModel realm = session.getContext().getRealm();
        String policyTypeKey = getComponentTypeKey(authType);
        List<String> origins = new ArrayList<>();
        realm.getComponentsStream(realm.getId(), ClientRegistrationPolicy.class.getName())
                .filter(componentModel -> Objects.equals(componentModel.getSubType(), policyTypeKey))
                .forEach(policyModel -> {
                    ClientRegistrationPolicy policy = session.getProvider(ClientRegistrationPolicy.class, policyModel);
                    if (policy != null) {
                        origins.addAll(policy.getAllowedOrigins());
                    }
                });
        return origins;
    }

    /** 按认证类型筛选并依次执行领域中的注册策略。
     * @param session Keycloak 会话
     * @param provider 客户端注册 Provider
     * @param authType 注册认证类型
     * @param opDescription 操作描述（用于日志与事件）
     * @param op 策略回调
     * @throws ClientRegistrationPolicyException 策略拒绝时抛出
     */
    private static void triggerPolicies(KeycloakSession session, ClientRegistrationProvider provider, RegistrationAuth authType,
                                        String opDescription, ClientRegOperation op) throws ClientRegistrationPolicyException {
        RealmModel realm = session.getContext().getRealm();

        String policyTypeKey = getComponentTypeKey(authType);
        realm.getComponentsStream(realm.getId(), ClientRegistrationPolicy.class.getName())
                .filter(componentModel -> Objects.equals(componentModel.getSubType(), policyTypeKey))
                .forEach(policyModel -> runPolicy(policyModel, session, provider, opDescription, op));
    }

    /** 实例化并执行单个策略组件，失败时记录事件并重新抛出异常。
     * @param policyModel 策略组件模型
     * @param session Keycloak 会话
     * @param provider 客户端注册 Provider
     * @param opDescription 操作描述
     * @param op 策略回调
     * @throws ClientRegistrationPolicyException 策略拒绝或 Provider 缺失时抛出
     */
    private static void runPolicy(ComponentModel policyModel, KeycloakSession session, ClientRegistrationProvider provider,
                           String opDescription, ClientRegOperation op) throws ClientRegistrationPolicyException {
        ClientRegistrationPolicy policy = session.getProvider(ClientRegistrationPolicy.class, policyModel);
        if (policy == null) {
            throw new ClientRegistrationPolicyException("Policy of type '" + policyModel.getProviderId() + "' not found");
        }

        if (logger.isTraceEnabled()) {
            logger.tracef("Running policy '%s' %s", policyModel.getName(), opDescription);
        }

        try {
            op.run(policy);
        } catch (ClientRegistrationPolicyException crpe) {
            provider.getEvent().detail(Details.CLIENT_REGISTRATION_POLICY, policyModel.getName());
            crpe.setPolicyModel(policyModel);
            ServicesLogger.LOGGER.clientRegistrationRequestRejected(opDescription, crpe.getMessage());
            throw crpe;
        }
    }

    /** 策略执行回调函数式接口 */
    private interface ClientRegOperation {

        /** 对给定策略执行操作
         * @param policy 客户端注册策略实例
         * @throws ClientRegistrationPolicyException 策略拒绝时抛出
         */
        void run(ClientRegistrationPolicy policy) throws ClientRegistrationPolicyException;

    }

    /** 将 {@link RegistrationAuth} 转为组件 subType 键（小写枚举名）。
     * @param authType 注册认证类型
     * @return 组件 subType 字符串
     */
    public static String getComponentTypeKey(RegistrationAuth authType) {
        return authType.toString().toLowerCase();
    }
}
