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
package org.keycloak.social.stackoverflow;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * Stack Overflow 身份提供者配置。
 * <p>扩展 OAuth2 配置，持有 Stack Exchange API 所需的 {@code key} 参数。</p>
 *
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class StackOverflowIdentityProviderConfig extends OAuth2IdentityProviderConfig {

	/** 从 realm 中的 IdP 模型构造配置。 */
	public StackOverflowIdentityProviderConfig(IdentityProviderModel model) {
		super(model);
	}

    /** 创建空配置。 */
    public StackOverflowIdentityProviderConfig() {
        
    }

    /** 获取 Stack Exchange API 注册 key。 */
    public String getKey() {
		return getConfig().get("key");
	}

	/** 设置 Stack Exchange API key。 */
	public void setKey(String key) {
		getConfig().put("key", key);
	}

}