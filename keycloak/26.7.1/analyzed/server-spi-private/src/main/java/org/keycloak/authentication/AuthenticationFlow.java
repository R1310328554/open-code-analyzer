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
import java.util.List;

import jakarta.ws.rs.core.Response;

/**
 * 认证流程运行时接口：处理用户动作与流程推进，并报告是否成功。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface AuthenticationFlow {
    /** 基础流程类型标识。 */
    String BASIC_FLOW = "basic-flow";
    /** 表单子流程类型标识。 */
    String FORM_FLOW = "form-flow";
    /** 客户端认证流程类型标识。 */
    String CLIENT_FLOW = "client-flow";

    /** 处理来自表单的 action 提交（指定 execution）。 */
    Response processAction(String actionExecution);
    /** 推进并执行流程下一步，返回挑战或完成响应。 */
    Response processFlow();
    /** 流程是否已成功完成。 */
    boolean isSuccessful();
    /** 收集流程中产生的异常列表，默认空。 */
    default List<AuthenticationFlowException> getFlowExceptions(){
        return Collections.emptyList();
    }
}
