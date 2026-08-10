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

package org.keycloak.authentication.forms;

import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.Config;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;

/**
 * 注册条款与条件表单动作：要求用户在提交注册前勾选接受条款。
 */
public class RegistrationTermsAndConditions implements FormAction, FormActionFactory, ConfiguredProvider {

	/** Provider ID：registration-terms-and-conditions。 */
	public static final String PROVIDER_ID = "registration-terms-and-conditions";

    /** 表单字段名：条款接受复选框。 */
	protected static final String FIELD = "termsAccepted";

	@Override
	/** @return 管理控制台显示名称 */
	public String getDisplayType() {
		return "Terms and conditions";
	}

	@Override
	public String getReferenceCategory() {
		return "terms-and-conditions";
	}

	@Override
	public boolean isConfigurable() {
		return false;
	}

	private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
			AuthenticationExecutionModel.Requirement.REQUIRED,
			AuthenticationExecutionModel.Requirement.DISABLED
	};
	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES;
	}

	@Override
	public boolean isUserSetupAllowed() {
		return false;
	}

	@Override
	/** 标记注册页需要展示条款接受复选框。 */
	public void buildPage(FormContext context, LoginFormsProvider form) {
		form.setAttribute("termsAcceptanceRequired", true);
	}

	@Override
	/** 校验用户已勾选 termsAccepted 字段。 */
	public void validate(ValidationContext context) {
		MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
		if (formData.containsKey(FIELD)) {
			context.success();
			return;
		}

		context.error(Errors.INVALID_REGISTRATION);
		context.validationError(formData, Collections.singletonList(new FormMessage(FIELD, "termsAcceptanceRequired")));
	}

	@Override
	public void success(FormContext context) {

	}

	@Override
	/** @return 条款校验不要求已识别用户 */
	public boolean requiresUser() {
		return false;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return true;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

	}

	@Override
	/** @return 帮助说明：要求用户接受条款与条件后再提交注册 */
	public String getHelpText() {
		return "Asks the user to accept terms and conditions before submitting its registration form.";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return Collections.emptyList();
	}

	@Override
	/** @return 自身作为单例表单动作 */
	public FormAction create(KeycloakSession session) {
		return this;
	}

	@Override
	public void init(Config.Scope config) {

	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {

	}

	@Override
	public void close() {

	}

	@Override
	/** @return Provider ID */
	public String getId() {
		return PROVIDER_ID;
	}
}
