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

package org.keycloak.testframework.realm;


import org.keycloak.representations.idm.AuthenticationExecutionRepresentation;

/**
 * {@link AuthenticationExecutionRepresentation} 的流式构建器，用于测试框架中组装认证执行配置。
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2016 Red Hat Inc.
 */
public class AuthenticationExecutionBuilder extends Builder<AuthenticationExecutionRepresentation> {

    private AuthenticationExecutionBuilder(AuthenticationExecutionRepresentation rep) {
        super(rep);
    }

    /** 创建空的认证执行构建器。 */
    public static AuthenticationExecutionBuilder create() {
        return new AuthenticationExecutionBuilder(new AuthenticationExecutionRepresentation());
    }

    /** @param id 执行项内部 id */
    public AuthenticationExecutionBuilder id(String id) {
        rep.setId(id);
        return this;
    }

    /** @param parentFlow 所属父认证流 id */
    public AuthenticationExecutionBuilder parentFlow(String parentFlow) {
        rep.setParentFlow(parentFlow);
        return this;
    }

    /** @param requirement 执行要求（REQUIRED、ALTERNATIVE、DISABLED 等） */
    public AuthenticationExecutionBuilder requirement(String requirement) {
        rep.setRequirement(requirement);
        return this;
    }

    /** @param authenticator 认证器 Provider id */
    public AuthenticationExecutionBuilder authenticator(String authenticator) {
        rep.setAuthenticator(authenticator);
        return this;
    }

    /** @param priority 执行优先级（数值越小越靠前） */
    public AuthenticationExecutionBuilder priority(int priority) {
        rep.setPriority(priority);
        return this;
    }

    /** @param authenticatorFlow 是否为嵌套认证器流（而非单步认证器） */
    public AuthenticationExecutionBuilder authenticatorFlow(boolean authenticatorFlow) {
        rep.setAuthenticatorFlow(authenticatorFlow);
        return this;
    }

}
