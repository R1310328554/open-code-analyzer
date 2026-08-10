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

package org.keycloak.authentication;

import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

/**
 * 表单细粒度处理 SPI：将单页表单拆分为可独立启用的 FormAction（如 Recaptcha）。
 * <p>管理员可在控制台单独开关各 FormAction，而无需改动整页表单逻辑。</p>
 *
 * Fine grain processing of a form.  Allows you to split up the processing of a form into smaller parts so that you can
 * enable/disable them from the admin console.  For example, Recaptcha is a FormAction.  This allows you as the admin
 * to turn Recaptcha on/off even though it is on the same form/page as other registration validation.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface FormAction extends Provider {
    /**
     * 渲染挑战页时调用，向表单注入额外展示属性。
     *
     * When a FormAuthenticator is rendering the challenge page, even FormAction.buildPage() method will be called
     * This gives the FormAction the opportunity to add additional attributes to the form to be displayed.
     *
     * @param context
     * @param form
     */
    void buildPage(FormContext context, LoginFormsProvider form);
    /**
     * 表单处理第一阶段：校验用户输入，无效时可发起挑战。
     *
     * This is the first phase of form processing.  Each FormAction.validate() method is called.  This gives the
     * FormAction a chance to validate and challenge if user input is invalid.
     *
     * @param context
     */
    void validate(ValidationContext context);

    /**
     * 所有 FormAction 的 validate 均成功后调用。
     *
     * Called after all validate() calls of all FormAction providers are successful.
     *
     * @param context
     */
    void success(FormContext context);

    /**
     * 是否要求用户已设置（注册场景通常为 false）。
     *
     * Does this FormAction require that a user be set? For registration, this method will always return false.
     *
     * @return
     */
    boolean requiresUser();

    /**
     * 当前用户是否已配置本 FormAction 所需凭证/设置。
     *
     * Is this FormAction configured for the current user?
     *
     * @param session
     * @param realm
     * @param user
     * @return
     */
    boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user);

    /**
     * 为用户设置配置本 FormAction 所需的 Required Actions。
     *
     * Set actions to configure authenticator
     */
    void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user);


}
