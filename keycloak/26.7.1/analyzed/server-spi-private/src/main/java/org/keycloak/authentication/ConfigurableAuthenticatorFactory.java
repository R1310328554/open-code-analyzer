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

import java.util.Collections;
import java.util.Set;

import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ConfiguredProvider;

/**
 * 可配置认证器工厂的通用契约，定义管理控制台中的展示与执行要求选项。
 * <p>由 {@link AuthenticatorFactory}、{@link ClientAuthenticatorFactory}、{@link FormActionFactory} 等继承。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ConfigurableAuthenticatorFactory extends ConfiguredProvider {

    /** 管理控制台可选的执行要求：必需、备选、禁用。 */
    AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED};

    /**
     * 认证器在管理控制台中的友好显示名称。
     *
     * Friendly name for the authenticator
     *
     * @return
     */
    String getDisplayType();

    /**
     * 认证器通用类型标识（如 totp、password、cert），用于凭证与 LoA 分类。
     *
     * General authenticator type, i.e. totp, password, cert.
     *
     * @return null if not a referenceable category
     */
    String getReferenceCategory();

    /**
     * 可选附加分类（如 username/form 中的 passkeys）；不参与 LoA 计算。
     *
     * Optional categories that this authenticator can have (for example passkeys in username/form).
     * Optional categories are not taken into account by LoA.
     * @param session The current session in the request
     * @return Set of extra optional categories, empty by default
     */
    default Set<String> getOptionalReferenceCategories(KeycloakSession session) {
        return Collections.emptySet();
    }

    /**
     * 该认证器是否可在流程执行中配置。
     *
     * Is this authenticator configurable?
     *
     * @return
     */
    boolean isConfigurable();

    /**
     * 允许的执行要求选项（REQUIRED、ALTERNATIVE、DISABLED 等）。
     *
     * What requirement settings are allowed.
     *
     * @return
     */
    AuthenticationExecutionModel.Requirement[] getRequirementChoices();

    /**
     * 用户未配置该认证器时，是否允许通过 Required Action 引导设置。
     *
     * Does this authenticator have required actions that can set if the user does not have
     * this authenticator set up?
     *
     * @return
     */
    boolean isUserSetupAllowed();

}
