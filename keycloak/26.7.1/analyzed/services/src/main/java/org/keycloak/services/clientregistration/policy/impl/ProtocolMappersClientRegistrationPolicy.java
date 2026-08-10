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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.clientregistration.ClientRegistrationContext;
import org.keycloak.services.clientregistration.ClientRegistrationProvider;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy;
import org.keycloak.services.clientregistration.policy.ClientRegistrationPolicyException;

import org.jboss.logging.Logger;

/**
 * 协议映射器类型白名单注册策略。
 * <p>在注册与更新时校验请求中的协议映射器类型是否在允许列表内；注册后移除自动添加但类型不在白名单内的内置映射器。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ProtocolMappersClientRegistrationPolicy implements ClientRegistrationPolicy {

    /** 日志记录器 */
    private static final Logger logger = Logger.getLogger(ProtocolMappersClientRegistrationPolicy.class);

    /** Keycloak 会话 */
    private final KeycloakSession session;
    /** 策略组件配置模型 */
    private final ComponentModel componentModel;

    /** 构造策略实例。
     * @param session Keycloak 会话
     * @param componentModel 策略组件模型
     */
    public ProtocolMappersClientRegistrationPolicy(KeycloakSession session, ComponentModel componentModel) {
        this.session = session;
        this.componentModel = componentModel;
    }

    /** {@inheritDoc} 注册前校验协议映射器类型 */
    @Override
    public void beforeRegister(ClientRegistrationContext context) throws ClientRegistrationPolicyException {
        testMappers(context, null);
    }

    /** 校验请求中的协议映射器：新映射器类型须在白名单内；更新时允许保留未变更的已有映射器。
     * @param context 客户端注册上下文
     * @param clientModel 更新时的现有客户端（注册时为 null）
     * @throws ClientRegistrationPolicyException 类型不允许或映射器 ID 无效时抛出
     */
    protected void testMappers(ClientRegistrationContext context, ClientModel clientModel) throws ClientRegistrationPolicyException {
        List<ProtocolMapperRepresentation> protocolMappers = context.getClient().getProtocolMappers();
        if (protocolMappers == null) {
            return;
        }

        List<String> allowedMapperProviders = getAllowedMapperProviders();

        for (ProtocolMapperRepresentation mapperRepresentation : protocolMappers) {
            String mapperType = mapperRepresentation.getProtocolMapper();

			if (allowedMapperProviders.contains(mapperType)) {
				continue;
			}
			if (clientModel == null) {
				failWithProtocolMapperTypeNotAllowedError(mapperRepresentation);
				return;
			}
			String mapperRepresentationId = mapperRepresentation.getId();
			if (mapperRepresentationId == null) {
				String message = "Missing id for mapper named '%s'".formatted(mapperRepresentation.getName());
				ServicesLogger.LOGGER.warn(message);
				throw new ClientRegistrationPolicyException(message);
			}
			ProtocolMapperModel mapperModel = clientModel.getProtocolMapperById(mapperRepresentationId);
			if (mapperModel == null) {
				String message = "No existing mapper model found for id '%s'".formatted(mapperRepresentationId);
				ServicesLogger.LOGGER.warn(message);
				throw new ClientRegistrationPolicyException(message);
			}
			String storedMapperType = mapperModel.getProtocolMapper();
			if (!Objects.equals(mapperType, storedMapperType)) {
				failWithProtocolMapperTypeNotAllowedError(mapperRepresentation);
				return;
			}
			Map<String, String> modelConfig = mapperModel.getConfig();
			Map<String, String> representationConfig = mapperRepresentation.getConfig();
			if (!Objects.equals(representationConfig, modelConfig)) {
				failWithProtocolMapperTypeNotAllowedError(mapperRepresentation);
				return;
			}
		}
    }

	/** 记录警告并抛出协议映射器类型不允许异常。
	 * @param mapper 被拒绝的映射器表示
	 */
	protected void failWithProtocolMapperTypeNotAllowedError(ProtocolMapperRepresentation mapper) {
		ServicesLogger.LOGGER.clientRegistrationMapperNotAllowed(mapper.getName(), mapper.getProtocolMapper());
		throw new ClientRegistrationPolicyException("ProtocolMapper type not allowed");
	}

    // 同时移除自动添加但类型不在白名单内的内置映射器
    /** {@inheritDoc} 注册后移除不允许类型的内置映射器 */
    @Override
    public void afterRegister(ClientRegistrationContext context, ClientModel clientModel) {
        // 移除注册过程中自动添加但类型不在白名单内的映射器
        List<String> allowedMapperProviders = getAllowedMapperProviders();
        clientModel.getProtocolMappersStream()
                .filter(mapper -> !allowedMapperProviders.contains(mapper.getProtocolMapper()))
                .peek(mapperToRemove -> logger.debugf("Removing builtin mapper '%s' of type '%s' as type is not permitted",
                        mapperToRemove.getName(), mapperToRemove.getProtocolMapper()))
                .collect(Collectors.toList())
                .forEach(clientModel::removeProtocolMapper);
    }

    /** {@inheritDoc} 更新前校验协议映射器类型与配置 */
    @Override
    public void beforeUpdate(ClientRegistrationContext context, ClientModel clientModel) throws ClientRegistrationPolicyException {
        testMappers(context, clientModel);
    }

    /** {@inheritDoc} 更新后无额外处理 */
    @Override
    public void afterUpdate(ClientRegistrationContext context, ClientModel clientModel) {
    }

    /** {@inheritDoc} 查看前无额外校验 */
    @Override
    public void beforeView(ClientRegistrationProvider provider, ClientModel clientModel) throws ClientRegistrationPolicyException {

    }

    /** {@inheritDoc} 删除前无额外校验 */
    @Override
    public void beforeDelete(ClientRegistrationProvider provider, ClientModel clientModel) throws ClientRegistrationPolicyException {

    }

    /** 从组件配置读取允许的协议映射器 Provider ID 列表。
     * @return 允许的映射器类型 ID 列表
     */
    private List<String> getAllowedMapperProviders() {
        return componentModel.getConfig().getOrDefault(ProtocolMappersClientRegistrationPolicyFactory.ALLOWED_PROTOCOL_MAPPER_TYPES, Collections.emptyList());
    }

}
