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

/**
 * 认证流执行步骤的导出/导入表示，继承 {@link AbstractAuthenticationExecutionRepresentation} 的公共字段。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AuthenticationExecutionExportRepresentation extends AbstractAuthenticationExecutionRepresentation {

    /** 嵌套子流的别名（指向 AuthenticationFlowModel）。 */
    private String flowAlias;
    /** 是否允许用户在 Required Actions 中自行配置此执行器。 */
    private boolean userSetupAllowed;


    /** @return 是否允许用户自行配置 */
    public boolean isUserSetupAllowed() {
        return userSetupAllowed;
    }

    /** @param userSetupAllowed 是否允许用户自行配置 */
    public void setUserSetupAllowed(boolean userSetupAllowed) {
        this.userSetupAllowed = userSetupAllowed;
    }

    /**
     * 若此执行步骤本身是一个子流，返回指向 {@code AuthenticationFlowModel} 的流别名。
     *
     * @return 子流别名
     */
    public String getFlowAlias() {
        return flowAlias;
    }

    /** @param flowId 子流别名 */
    public void setFlowAlias(String flowId) {
        this.flowAlias = flowId;
    }
}
