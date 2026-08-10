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
 */

package org.keycloak.representations.idm;

import java.util.List;
import java.util.Objects;

/**
 * Client Profile 的外部 REST 表示，聚合一组 Client Policy 执行器。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientProfileRepresentation {

    /** Profile 名称。 */
    protected String name;
    /** Profile 描述。 */
    protected String description;
    /** 该 Profile 包含的执行器列表。 */
    protected List<ClientPolicyExecutorRepresentation> executors;

    /** @return Profile 名称 */
    public String getName() {
        return name;
    }

    /** @param name Profile 名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return Profile 描述 */
    public String getDescription() {
        return description;
    }

    /** @param description Profile 描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 执行器列表 */
    public List<ClientPolicyExecutorRepresentation> getExecutors() {
        return executors;
    }

    /** @param executors 执行器列表 */
    public void setExecutors(List<ClientPolicyExecutorRepresentation> executors) {
        this.executors = executors;
    }

    /** 基于名称、描述与执行器列表比较相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientProfileRepresentation that = (ClientProfileRepresentation) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(executors, that.executors);
    }

    /** 基于核心字段计算哈希。 */
    @Override
    public int hashCode() {
        return Objects.hash(name, description, executors);
    }
}
