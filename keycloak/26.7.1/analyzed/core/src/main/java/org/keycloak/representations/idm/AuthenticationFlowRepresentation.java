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
import java.util.List;

/**
 * 认证流（Authentication Flow）的 REST 表示，包含流元数据及其执行步骤列表。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AuthenticationFlowRepresentation implements Serializable {

    /** 认证流持久化 ID。 */
    private String id;
    /** 流别名，在 realm 内唯一。 */
    private String alias;
    /** 流描述。 */
    private String description;
    /** 流提供方 ID（如 basic-flow）。 */
    private String providerId;
    /** 是否为顶级流（可直接绑定到客户端或 realm）。 */
    private boolean topLevel;
    /** 是否为 Keycloak 内置流，不可删除。 */
    private boolean builtIn;
    /** 流内各执行步骤的导出表示列表。 */
    protected List<AuthenticationExecutionExportRepresentation> authenticationExecutions;

    /** @return 认证流 ID */
    public String getId() {
        return id;
    }

    /** @param id 认证流 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 流别名 */
    public String getAlias() {
        return alias;
    }

    /** @param alias 流别名 */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /** @return 流描述 */
    public String getDescription() {
        return description;
    }

    /** @param description 流描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 流提供方 ID */
    public String getProviderId() {
        return providerId;
    }

    /** @param providerId 流提供方 ID */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /** @return 是否为顶级流 */
    public boolean isTopLevel() {
        return topLevel;
    }

    /** @param topLevel 是否为顶级流 */
    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    /** @return 是否为内置流 */
    public boolean isBuiltIn() {
        return builtIn;
    }

    /** @param builtIn 是否为内置流 */
    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    /** @return 执行步骤列表 */
    public List<AuthenticationExecutionExportRepresentation> getAuthenticationExecutions() {
        return authenticationExecutions;
    }

    /** @param authenticationExecutions 执行步骤列表 */
    public void setAuthenticationExecutions(List<AuthenticationExecutionExportRepresentation> authenticationExecutions) {
        this.authenticationExecutions = authenticationExecutions;
    }
}
