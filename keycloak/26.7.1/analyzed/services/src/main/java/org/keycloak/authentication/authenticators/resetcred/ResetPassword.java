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

package org.keycloak.authentication.authenticators.resetcred;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;


/**
 * 重置密码认证器：在凭据重置流程中设置“更新密码”必需操作。
 * <p>执行项为 REQUIRED 时始终设置；为 OPTIONAL 时仅当用户已配置密码凭证时设置。</p>
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ResetPassword extends AbstractSetRequiredActionAuthenticator {

    /** 提供者标识符。 */
    public static final String PROVIDER_ID = "reset-password";

    /** 按执行要求添加 UPDATE_PASSWORD 必需操作并以 password 类别标记成功。 */
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        if (context.getExecution().isRequired() ||
                (context.getExecution().isConditional() &&
                        configuredFor(context))) {
            context.getAuthenticationSession().addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
        }
        // 传递 password 类别以触发暴力破解保护的临时锁定重置
        context.success(PasswordCredentialModel.TYPE);
    }

    /** @return 用户是否已配置密码凭证 */
    protected boolean configuredFor(AuthenticationFlowContext context) {
        return context.getUser().credentialManager().isConfiguredFor(PasswordCredentialModel.TYPE);
    }

    /** @return 管理控制台显示名称 */
    @Override
    public String getDisplayType() {
        return "Reset Password";
    }

    /** @return 重置密码必需操作的触发条件说明 */
    @Override
    public String getHelpText() {
        return "Sets the Update Password required action if execution is REQUIRED.  Will also set it if execution is OPTIONAL and the password is currently configured for it.";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
