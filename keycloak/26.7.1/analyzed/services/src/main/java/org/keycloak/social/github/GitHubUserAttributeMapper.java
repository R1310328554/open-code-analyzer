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
package org.keycloak.social.github;

import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;

/**
 * GitHub 用户属性映射器。
 * <p>将 GitHub API 用户 JSON 字段映射到 Keycloak 用户属性。</p>
 *
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class GitHubUserAttributeMapper extends AbstractJsonUserAttributeMapper {

	/** 映射器 provider id 常量。 */
	public static final String PROVIDER_ID = "github-user-attribute-mapper";
	/** 兼容的 IdP provider id 列表。 */
	private static final String[] cp = new String[] { GitHubIdentityProviderFactory.PROVIDER_ID };

	/** 返回仅支持 GitHub IdP 的 provider id 数组。 */
	@Override
	public String[] getCompatibleProviders() {
		return cp;
	}

	/** 返回映射器 id {@link #PROVIDER_ID}。 */
	@Override
	public String getId() {
		return PROVIDER_ID;
	}

}
