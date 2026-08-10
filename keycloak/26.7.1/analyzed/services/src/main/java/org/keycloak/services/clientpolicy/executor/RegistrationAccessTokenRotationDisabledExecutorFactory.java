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

import java.util.Collections;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * {@link RegistrationAccessTokenRotationDisabledExecutor} 的 Provider 工厂。
 * <p>创建禁用注册访问令牌轮换的执行器实例。</p>
 */
public class RegistrationAccessTokenRotationDisabledExecutorFactory implements ClientPolicyExecutorProviderFactory {

	/** 执行器 Provider 标识符 */
	public static final String PROVIDER_ID = "registration-access-token-rotation-disabled";

	/** @return 执行器说明（英文原文保留） */
	@Override
	public String getHelpText() {
		return "Disables registration access rotation for the client.";
	}

	/** @return 无额外配置项 */
	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return Collections.emptyList();
	}

	/** @param session Keycloak 会话 @return 新的轮换禁用执行器 */
	@Override
	public ClientPolicyExecutorProvider create(KeycloakSession session) {
		return new RegistrationAccessTokenRotationDisabledExecutor(getId(), session);
	}

	/** 工厂初始化（无全局配置） */
	@Override
	public void init(Config.Scope config) {

	}

	/** 会话工厂就绪回调 */
	@Override
	public void postInit(KeycloakSessionFactory factory) {

	}

	/** 工厂关闭钩子 */
	@Override
	public void close() {

	}

	/** @return 执行器标识 {@link #PROVIDER_ID} */
	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	/** @return 始终支持该执行器 */
	@Override
	public boolean isSupported(Config.Scope config) {
		return true;
	}
}
