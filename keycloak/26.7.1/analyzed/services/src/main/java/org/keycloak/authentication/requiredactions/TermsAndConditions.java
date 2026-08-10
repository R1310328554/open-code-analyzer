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

package org.keycloak.authentication.requiredactions;

import java.util.Arrays;

import jakarta.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;

/**
 * 条款与条件必需操作：要求用户阅读并接受 realm 条款。
 * <p>接受时在用户属性中记录时间戳；拒绝则清除属性并失败。</p>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class TermsAndConditions implements RequiredActionProvider, RequiredActionFactory {
    /** 提供者标识符。 */
    public static final String PROVIDER_ID = UserModel.RequiredAction.TERMS_AND_CONDITIONS.name();
    /** 用户属性名：记录接受条款的时间戳。 */
    public static final String USER_ATTRIBUTE = "terms_and_conditions";

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }


    @Override
    public void evaluateTriggers(RequiredActionContext context) {

    }


    /** 展示条款与条件表单。 */
    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        Response challenge = context.form()
            .setAttribute("user", context.getAuthenticationSession().getAuthenticatedUser())
            .createForm("terms.ftl");
        context.challenge(challenge);
    }

    /** 处理接受或拒绝：写入时间戳属性或失败。 */
    @Override
    public void processAction(RequiredActionContext context) {
        // Keycloak 21.0.0 曾将属性名改为大写，此处同时清理遗留大写属性
        // this change was reverted, but it is still possible some attributes created
        // in Keycloak 21.0.0 will be present in the database, we need to remove it too.
        // See https://github.com/keycloak/keycloak/issues/17277 for more details
        context.getUser().removeAttribute(USER_ATTRIBUTE.toUpperCase());

        if (context.getHttpRequest().getDecodedFormParameters().containsKey("cancel")) {
            context.getUser().removeAttribute(USER_ATTRIBUTE);
            context.failure(Messages.TERMS_AND_CONDITIONS_DECLINED);
            return;
        }

        context.getUser().setAttribute(USER_ATTRIBUTE, Arrays.asList(Integer.toString(Time.currentTime())));

        context.success();
    }

    @Override
    public String getDisplayText() {
        return "Terms and Conditions";
    }

    @Override
    public void close() {

    }
}
