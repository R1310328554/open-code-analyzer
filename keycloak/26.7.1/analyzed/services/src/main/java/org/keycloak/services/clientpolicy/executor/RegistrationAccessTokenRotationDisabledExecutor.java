/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.executor;

import org.keycloak.models.ClientRegistrationAccessTokenConstants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;

/**
 * 注册访问令牌轮换禁用执行器。
 * <p>在客户端策略事件触发时将会话属性 {@code ROTATION_ENABLED} 设为 false，从而禁止注册访问令牌自动轮换。</p>
 */
public class RegistrationAccessTokenRotationDisabledExecutor implements ClientPolicyExecutorProvider<ClientPolicyExecutorConfigurationRepresentation> {

	/** 执行器 Provider 标识符 */
	private final String providerId;
	/** Keycloak 会话，用于读写轮换开关属性 */
	private final KeycloakSession session;

	/** @param providerId 执行器标识 @param session Keycloak 会话 */
	public RegistrationAccessTokenRotationDisabledExecutor(String providerId, KeycloakSession session) {
		this.providerId = providerId;
		this.session = session;
	}

	@Override
	public String getProviderId() {
		return providerId;
	}

	/** 若轮换属性已设置，则将其强制设为 false */
	@Override
	public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
		// 未启用轮换上下文时无需处理
		if (session.getAttribute(ClientRegistrationAccessTokenConstants.ROTATION_ENABLED) == null){
			return;
		}
		session.setAttribute(ClientRegistrationAccessTokenConstants.ROTATION_ENABLED, false);
	}

}
