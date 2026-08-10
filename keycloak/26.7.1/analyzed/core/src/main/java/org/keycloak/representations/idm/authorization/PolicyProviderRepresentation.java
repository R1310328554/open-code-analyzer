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
 * 策略提供方（Policy Provider）的元数据表示，描述内置或自定义策略类型的注册信息。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class PolicyProviderRepresentation {

    /** 策略类型标识。 */
    private String type;
    /** 策略类型显示名称。 */
    private String name;
    /** 策略类型所属分组。 */
    private String group;
    /** 策略类型描述。 */
    private String description;
    /** 策略类型关联的代码或脚本标识。 */
    private String code;

    /** @return 策略类型标识 */
    public String getType() {
        return this.type;
    }

    /** @param type 策略类型标识 */
    public void setType( String type) {
        this.type = type;
    }

    /** @return 显示名称 */
    public String getName() {
        return this.name;
    }

    /** @param name 显示名称 */
    public void setName( String name) {
        this.name = name;
    }

    /** @return 所属分组 */
    public String getGroup() {
        return this.group;
    }

    /** @param group 所属分组 */
    public void setGroup( String group) {
        this.group = group;
    }

    /** @return 描述 */
    public String getDescription() {
        return this.description;
    }

    /** @param description 描述 */
    public void setDescription( String description) {
        this.description = description;
    }

    /** @return 代码或脚本标识 */
    public String getCode() {
        return this.code;
    }

    /** @param code 代码或脚本标识 */
    public void setCode( String code) {
        this.code = code;
    }
}
