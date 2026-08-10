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

/**
 * 资源所有者的 REST 表示，标识 UMA 场景下资源的归属用户或实体。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ResourceOwnerRepresentation {

    /** 所有者唯一标识。 */
    private String id;
    /** 所有者显示名称。 */
    private String name;

    /** 创建空的所有者表示。 */
    public ResourceOwnerRepresentation() {

    }

    /** @param id 所有者 ID */
    public ResourceOwnerRepresentation(String id) {
        this.id = id;
    }

    /** @return 所有者 ID */
    public String getId() {
        return this.id;
    }

    /** @param id 所有者 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 所有者显示名称 */
    public String getName() {
        return this.name;
    }

    /** @param name 所有者显示名称 */
    public void setName(String name) {
        this.name = name;
    }
}
