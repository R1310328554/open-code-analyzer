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

package org.keycloak.models;

import java.util.HashMap;
import java.util.Map;

/**
 * 凭据校验输出：认证用户、状态及供客户端/后续步骤使用的附加状态。
 * Output of credential validation
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CredentialValidationOutput {

    private final UserModel authenticatedUser; // 已认证用户
    private final Status authStatus;           // 认证状态：成功、失败、回退或需继续
    private final Map<String, String> state;   // 认证附加状态（回传客户端或记录所用凭据）

    public CredentialValidationOutput(UserModel authenticatedUser, Status authStatus, Map<String, String> state) {
        this.authenticatedUser = authenticatedUser;
        this.authStatus = authStatus;
        this.state = state;
    }

    /** 构造失败结果。 */
    public static CredentialValidationOutput failed() {
        return new CredentialValidationOutput(null, CredentialValidationOutput.Status.FAILED, new HashMap<>());
    }

    /** 构造需回退到其他用户存储的结果。 */
    public static CredentialValidationOutput fallback() {
        return new CredentialValidationOutput(null, CredentialValidationOutput.Status.FALLBACK, new HashMap<>());
    }

    public UserModel getAuthenticatedUser() {
        return authenticatedUser;
    }

    public Status getAuthStatus() {
        return authStatus;
    }

    /**
     * 提供者回传的附加状态。
     * State that is passed back by provider
     *
     * @return
     */
    public Map<String, String> getState() {
        return state;
    }

    public CredentialValidationOutput merge(CredentialValidationOutput that) {
        throw new IllegalStateException("Not supported yet");
    }

    public enum Status {

        /** 用户已成功认证；此时 {@link #getAuthenticatedUser()} 必须非空。 */
        /**
         * User was successfully authenticated. The {@link #getAuthenticatedUser()} must return authenticated user when this is used
         */
        AUTHENTICATED,

        /** 联邦提供者认证失败（用户已识别但凭据错误，阻止回退到其他存储）。 */
        /**
         * Federation provider failed to authenticate user. This is typically used when user storage provider recognizes the user, but credentials
         * are incorrect, so federation provider can mark whole authentication as not successful without eventual fallback to other user storage provider
         */
        FAILED,

        /** 联邦提供者无法识别用户，可尝试链中下一个用户存储。 */
        /**
         * Federation provider was not able to recognize the user. It is possible that credential was valid, but fereration provider was not able to lookup the user in it's storage.
         * Fallback to other user storage provider in the chain might be possible
         */
        FALLBACK,

        /** 联邦认证未完成，需进一步质询后重试同一提供者。 */
        /**
         * Federation provider did not fully authenticate user. It may be needed to ask user for further challenge to then re-try authentication with same federation provider
         */
        CONTINUE,
    }
}
