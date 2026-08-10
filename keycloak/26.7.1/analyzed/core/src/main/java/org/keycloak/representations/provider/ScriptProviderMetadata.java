/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.representations.provider;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 单个脚本 Provider 的元数据，描述名称、脚本文件名及可选说明；
 * {@code id} 与 {@code code} 仅用于服务端加载，不参与 JSON 序列化。
 */
public class ScriptProviderMetadata {

    /** 内部唯一标识，JSON 中忽略。 */
    @JsonIgnore
    private String id;
    /** Provider 显示名称。 */
    private String name;
    /** 脚本源文件名。 */
    private String fileName;
    /** 人类可读的 Provider 描述。 */
    private String description;

    /** 脚本源代码内容，JSON 中忽略。 */
    @JsonIgnore
    private String code;

    /** 默认构造函数，供 JSON 反序列化使用。 */
    public ScriptProviderMetadata() {

    }

    /**
     * 构造脚本 Provider 元数据。
     *
     * @param name        显示名称
     * @param fileName    脚本文件名
     * @param description 描述文字
     */
    public ScriptProviderMetadata(String name, String fileName, String description) {
        this.name = name;
        this.fileName = fileName;
        this.description = description;
    }

    /** @return 内部 ID */
    public String getId() {
        return id;
    }

    /** @param id 内部 ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return 显示名称 */
    public String getName() {
        return name;
    }

    /** @param name 显示名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 脚本文件名 */
    public String getFileName() {
        return fileName;
    }

    /** @param fileName 脚本文件名 */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /** @return 描述 */
    public String getDescription() {
        return description;
    }

    /** @param description 描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 脚本源代码 */
    public String getCode() {
        return code;
    }

    /** @param code 脚本源代码 */
    public void setCode(String code) {
        this.code = code;
    }
}
