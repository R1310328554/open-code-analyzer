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

import org.keycloak.models.utils.FormMessage;

/**
 * 认证器请求分叉当前流程时内部抛出的异常。
 * <p>携带成功/错误 {@link org.keycloak.models.utils.FormMessage}，错误码为 {@link AuthenticationFlowError#FORK_FLOW}。</p>
 *
 * Thrown internally when authenticator wants to fork the current flow.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ForkFlowException extends AuthenticationFlowException {
    /** 分叉成功时展示的消息。 */
    protected FormMessage successMessage;
    /** 分叉失败时展示的消息。 */
    protected FormMessage errorMessage;

    /** 返回分叉成功消息。 */
    public FormMessage getSuccessMessage() {
        return successMessage;
    }

    /** 返回分叉错误消息。 */
    public FormMessage getErrorMessage() {
        return errorMessage;
    }

    /** 以成功/错误消息构造分叉异常。 */
    public ForkFlowException(FormMessage successMessage, FormMessage errorMessage) {
        super(AuthenticationFlowError.FORK_FLOW);
        this.successMessage = successMessage;
        this.errorMessage = errorMessage;
    }
}
