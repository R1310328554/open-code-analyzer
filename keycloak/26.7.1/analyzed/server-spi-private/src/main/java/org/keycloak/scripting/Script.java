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
package org.keycloak.scripting;

import org.keycloak.models.ScriptModel;

/**
 * {@link ScriptModel} 的简单实现，持有脚本元数据与源码。
 * <p>包含 id、realmId、名称、MIME 类型、代码与描述等字段。</p>
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class Script implements ScriptModel {

    private String id;

    private String realmId;

    private String name;

    private String mimeType;

    private String code;

    private String description;

    /** 构造脚本实例，填充全部元数据字段。 */
    public Script(String id, String realmId, String name, String mimeType, String code, String description) {

        this.id = id;
        this.realmId = realmId;
        this.name = name;
        this.mimeType = mimeType;
        this.code = code;
        this.description = description;
    }

    /** @return 脚本唯一标识 */
    @Override
    public String getId() {
        return id;
    }

    /** @param id 脚本唯一标识 */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 所属 realm ID */
    @Override
    public String getRealmId() {
        return realmId;
    }

    /** @param realmId 所属 realm ID */
    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    /** @return 脚本名称 */
    @Override
    public String getName() {
        return name;
    }

    /** @param name 脚本名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 脚本 MIME 类型（如 JavaScript） */
    @Override
    public String getMimeType() {
        return mimeType;
    }

    /** @param mimeType 脚本 MIME 类型 */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /** @return 脚本源代码 */
    @Override
    public String getCode() {
        return code;
    }

    /** @param code 脚本源代码 */
    public void setCode(String code) {
        this.code = code;
    }

    /** @return 脚本描述 */
    @Override
    public String getDescription() {
        return description;
    }

    /** @param description 脚本描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Script{" +
                "id='" + id + '\'' +
                ", realmId='" + realmId + '\'' +
                ", name='" + name + '\'' +
                ", type='" + mimeType + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
