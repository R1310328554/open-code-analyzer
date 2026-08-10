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

/**
 * 认证流程中单个执行步骤/认证器的运行状态。
 *
 * Status of an execution/authenticator in a Authentication Flow
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public enum FlowStatus {
    /** 执行成功。 Successful execution */
    SUCCESS,

    /** 已发起挑战；OPTIONAL 可忽略，ALTERNATIVE 视其他执行结果而定。 Execution offered a challenge. */
    CHALLENGE,

    /** 强制向用户发送挑战，不受执行要求级别影响。 Regardless of the execution's requirement, this challenge will be sent to the user. */
    FORCE_CHALLENGE,

    /** 中止流程并返回执行提供的 HTTP 响应。 Flow will be aborted and a Response provided by the execution will be sent. */
    FAILURE_CHALLENGE,

    /** 中止流程（无特定响应体）。 Flow will be aborted. */
    FAILED,

    /** 已尝试但无法处理（非错误），如 Kerberos 未收到 negotiate 头。 This is not an error condition. Execution was attempted, but the authenticator is unable to process the request. */
    ATTEMPTED,

    /** 分叉流程：克隆并重置认证会话，重定向到浏览器登录。 This flow is being forked. */
    FORK,

    /** 流程重置到起点，如在 OTP 页取消返回用户名密码页。 This flow was reset to the beginning. */
    FLOW_RESET

}
