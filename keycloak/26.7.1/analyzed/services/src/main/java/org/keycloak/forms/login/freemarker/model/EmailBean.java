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
package org.keycloak.forms.login.freemarker.model;

import java.util.stream.Stream;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;

/**
 * 邮箱更新 FreeMarker Bean：在 {@link AbstractUserProfileBean} 基础上暴露用户邮箱字段。
 * <p>供更新邮箱页面渲染表单默认值与 {@link UserProfileContext#UPDATE_EMAIL} 属性元数据。</p>
 */
public class EmailBean extends AbstractUserProfileBean {

	/** 待更新邮箱的目标用户。 */
	private final UserModel user;
	/** @param user 目标用户 @param formData 表单回显数据 @param session Keycloak 会话 */
	public EmailBean(UserModel user, MultivaluedMap<String, String> formData, KeycloakSession session) {
		super(formData);
		this.user = user;
		init(session, false);
	}

	/** @return 表单中的 email 值，无表单数据时返回用户当前邮箱 */
	public String getValue() {
		return formData != null ? formData.getFirst("email") : user.getEmail();
	}

	@Override
	/** 创建 {@link UserProfileContext#UPDATE_EMAIL} 场景的用户配置实例。 */
	protected UserProfile createUserProfile(UserProfileProvider provider) {
		return provider.create(UserProfileContext.UPDATE_EMAIL, user);
	}

	@Override
	/** @param name 属性名 @return 用户现有属性值流 */
	protected Stream<String> getAttributeDefaultValues(String name) {
		return user.getAttributeStream(name);
	}

	@Override
	/** @return 用户配置上下文名称（UPDATE_PROFILE） */
	public String getContext() {
		return UserProfileContext.UPDATE_PROFILE.name();
	}
}
