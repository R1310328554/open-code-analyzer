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

package org.keycloak.representations.idm;

import java.io.Serializable;

/**
 * 认证流程执行步骤的抽象表示，描述单个 authenticator 或其子流程的配置与优先级。
 * <p>
 * 被 {@code AuthenticationExecutionRepresentation} 等具体类继承。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class AbstractAuthenticationExecutionRepresentation implements Serializable {

    /** Authenticator 配置 ID。 */
    private String authenticatorConfig;
    /** Authenticator 或子流程的 provider ID。 */
    private String authenticator;
    /** 引用的 authenticator 是否为子流程。 */
    private boolean authenticatorFlow;
    /** 执行要求（REQUIRED、ALTERNATIVE、DISABLED 等）。 */
    private String requirement;
    /** 执行优先级（数值越小越先执行）。 */
    private Integer priority;

    public String getAuthenticatorConfig() {
        return authenticatorConfig;
    }

    public void setAuthenticatorConfig(String authenticatorConfig) {
        this.authenticatorConfig = authenticatorConfig;
    }

    public String getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(String authenticator) {
        this.authenticator = authenticator;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * 引用的 authenticator 是否为子流程？（已废弃，拼写错误字段）
     *
     * @return 是否为子流程
     * @deprecated 请使用 {@link #isAuthenticatorFlow()}
     */
    @Deprecated
    private boolean autheticatorFlow;

    /** @deprecated 请使用 {@link #isAuthenticatorFlow()} */
    @Deprecated
    public boolean isAutheticatorFlow() {
        return authenticatorFlow;
    }

    /** @deprecated 请使用 {@link #setAuthenticatorFlow(boolean)} */
    @Deprecated
    public void setAutheticatorFlow(boolean autheticatorFlow) {
        this.authenticatorFlow = autheticatorFlow;
    }

    /**
     * 引用的 authenticator 是否为子流程？
     *
     * @return 是否为子流程
     */
    public boolean isAuthenticatorFlow() {
        return authenticatorFlow;
    }

    public void setAuthenticatorFlow(boolean authenticatorFlow) {
        this.authenticatorFlow = authenticatorFlow;
    }

}
