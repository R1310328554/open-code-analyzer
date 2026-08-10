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
 * 必需操作提供者的简化 REST 表示。部分端点（如注册新必需操作）不支持全部字段（如 setEnabled 等），
 * 因此使用此精简版本替代完整的 {@link RequiredActionProviderRepresentation}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class RequiredActionProviderSimpleRepresentation {

    /** 必需操作 ID。 */
    private String id;
    /** 必需操作名称。 */
    private String name;
    /** 提供者 SPI 标识。 */
    private String providerId;

    /** @return 必需操作 ID */
    public String getId() {
        return id;
    }

    /** @param id 必需操作 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 必需操作名称 */
    public String getName() {
        return name;
    }

    /** @param name 必需操作名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 提供者 ID */
    public String getProviderId() {
        return providerId;
    }

    /** @param providerId 提供者 ID */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

}
