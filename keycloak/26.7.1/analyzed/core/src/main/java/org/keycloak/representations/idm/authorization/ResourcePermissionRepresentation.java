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
package org.keycloak.representations.idm.authorization;

import java.util.Set;

/**
 * 基于资源类型的权限策略 REST 表示，将策略绑定到指定资源类型而非单个资源。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ResourcePermissionRepresentation extends AbstractPolicyRepresentation {

    /** 创建流式构建器。 */
    public static Builder create() {
        return new Builder();
    }

    /** 目标资源类型。 */
    private String resourceType;

    /** @return 策略类型，固定为 {@code resource} */
    @Override
    public String getType() {
        return "resource";
    }

    /** @param resourceType 资源类型 */
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /** @return 资源类型 */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * {@link ResourcePermissionRepresentation} 的流式构建器。
     */
    public static final class Builder {

        private final ResourcePermissionRepresentation rep;

        private Builder() {
            rep = new ResourcePermissionRepresentation();
        }

        /** @param name 策略名称 */
        public Builder name(String name) {
            rep.setName(name);
            return this;
        }

        /** @param ids 关联资源 ID 集合 */
        public Builder resources(Set<String> ids) {
            rep.setResources(ids);
            return this;
        }

        /** @param ids 关联策略 ID 集合 */
        public Builder policies(Set<String> ids) {
            rep.setPolicies(ids);
            return this;
        }

        /** @param policy 要关联的策略（必须已有 ID） */
        public Builder policy(AbstractPolicyRepresentation policy) {
            String id = policy.getId();

            if (id == null) {
                throw new IllegalArgumentException("Policy must have an id");
            }

            rep.addPolicy(id);

            return this;
        }

        /** @return 构建完成的资源权限策略表示 */
        public ResourcePermissionRepresentation build() {
            return rep;
        }
    }
}
