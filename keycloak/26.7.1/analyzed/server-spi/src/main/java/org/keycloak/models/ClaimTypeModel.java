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

package org.keycloak.models;

/**
 * 声明类型模型：定义用户属性/Token 声明的数据类型（布尔、整型、字符串、JSON）。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ClaimTypeModel {

    /** 声明值的数据类型枚举。 */
    public enum ValueType {
        BOOLEAN,
        INT,
        STRING,
        JSON
    }

    private String id;
    private String name;
    private boolean builtIn;
    private ValueType type;

    /** 复制构造。 */
    public ClaimTypeModel(ClaimTypeModel copy) {
        this(copy.getId(), copy.getName(), copy.isBuiltIn(), copy.getType());
    }

    /** @param id 声明类型 ID
     * @param name 声明名称
     * @param builtIn 是否内置
     * @param type 值类型 */
    public ClaimTypeModel(String id, String name, boolean builtIn, ValueType type) {
        this.id = id;
        this.name = name;
        this.builtIn = builtIn;
        this.type = type;
    }

    public ClaimTypeModel() {
    }

    /** @return 声明类型 ID */
    public String getId() {
        return id;
    }

    /** @return 声明名称 */
    public String getName() {
        return name;
    }

    /** @return 是否为内置声明类型 */
    public boolean isBuiltIn() {
        return builtIn;
    }

    /** @return 声明值类型 */
    public ValueType getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    public void setType(ValueType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClaimTypeModel that = (ClaimTypeModel) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
