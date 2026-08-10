/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.services.clientpolicy;

import java.io.Serializable;
import java.util.List;

import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProvider;

/**
 * 客户端配置文件运行时模型：聚合名称、描述与 {@link ClientPolicyExecutorProvider} 执行器列表。
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
class ClientProfile implements Serializable {

    protected String name;
    protected String description;
    protected List<ClientPolicyExecutorProvider> executors;

    /** @return Profile 名称 */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** @return Profile 描述 */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 已配置并初始化的 Executor Provider 列表 */
    public List<ClientPolicyExecutorProvider> getExecutors() {
        return executors;
    }

    /** @param executors Executor Provider 列表 */
    public void setExecutors(List<ClientPolicyExecutorProvider> executors) {
        this.executors = executors;
    }
}
