/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.forms.login.freemarker.model;

import java.util.Collections;
import java.util.List;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationSelectionOption;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.forms.login.LoginFormsPages;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 认证流程上下文 Bean：向 FreeMarker 模板暴露认证器选择、用户名展示与“尝试其他方式”链接等 UI 状态。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AuthenticationContextBean {

    private final AuthenticationFlowContext context;
    private final LoginFormsPages page;

    /** @param context 当前认证流上下文 @param page 正在渲染的登录页类型 */
    public AuthenticationContextBean(AuthenticationFlowContext context, LoginFormsPages page) {
        this.context = context;
        this.page = page;
    }

    /** @return 可选认证方式列表；无上下文时为空 */
    public List<AuthenticationSelectionOption> getAuthenticationSelections() {
        return context==null ? Collections.emptyList() : context.getAuthenticationSelections();
    }

    /** @return 是否存在多种认证方式且当前非选择器页时展示“尝试其他方式” */
    public boolean showTryAnotherWayLink() {
        return getAuthenticationSelections().size() > 1 && page != LoginFormsPages.LOGIN_SELECT_AUTHENTICATOR;
    }


    /** @return 是否应在页面上展示用户名/尝试用户名 */
    public boolean showUsername() {
        if (context == null) {
            return false;
        }

        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();

        if (Boolean.parseBoolean(authenticationSession.getAuthNote(AbstractUsernameFormAuthenticator.USERNAME_HIDDEN))) {
            return getAttemptedUsername() != null;
        }

        return context.getUser() != null && authenticationSession != null && page!=LoginFormsPages.ERROR;
    }

    /** @return 重置凭证页且应展示用户名时为 true */
    public boolean showResetCredentials() {
        return showUsername() && page == LoginFormsPages.LOGIN_RESET_PASSWORD;
    }


    // 注：attemptedUsername 指登录页输入的用户标识，未必等于账户真实 username
    // （可能是邮箱等其他标识）
    /** @return 登录页尝试输入的用户标识，必要时回退真实 username */
    public String getAttemptedUsername() {
        if (context == null) {
            return null;
        }

        String username = context.getAuthenticationSession().getAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME);

        // attemptedUsername 不存在时回退到用户真实 username
        if (username == null && context.getUser() != null) {
            username = context.getUser().getUsername();
        }

        return username;
    }
}
