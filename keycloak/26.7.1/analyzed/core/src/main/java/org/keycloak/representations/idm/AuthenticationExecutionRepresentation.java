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
 * 认证流中单个执行步骤的 REST 表示，用于 Admin API 读写流配置。
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class AuthenticationExecutionRepresentation extends AbstractAuthenticationExecutionRepresentation {

    /** 执行步骤持久化 ID。 */
    private String id;
    /** 嵌套子流 ID（当步骤为子流时）。 */
    private String flowId;
    /** 父认证流 ID。 */
    private String parentFlow;

    /** @return 执行步骤 ID */
    public String getId() {
        return id;
    }

    /** @param id 执行步骤 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 嵌套子流 ID */
    public String getFlowId() {
        return flowId;
    }

    /** @param flowId 嵌套子流 ID */
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    /** @return 父认证流 ID */
    public String getParentFlow() {
        return parentFlow;
    }

    /** @param parentFlow 父认证流 ID */
    public void setParentFlow(String parentFlow) {
        this.parentFlow = parentFlow;
    }
}
