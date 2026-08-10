/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.model.response;

/**
 * 命名空间（租户）信息。
 *
 * <p>描述命名空间 ID、展示名、描述、配额、配置数量及类型，供控制台与 Open API 返回。</p>
 *
 * @author diamond
 */
public class Namespace {
    
    /** 命名空间 ID（tenant）。 */
    private String namespace;
    
    /** 命名空间展示名称。 */
    private String namespaceShowName;
    
    /** 命名空间描述。 */
    private String namespaceDesc;
    
    /** 配置配额上限。 */
    private int quota;
    
    /** 当前配置数量。 */
    private int configCount;
    
    /** 命名空间类型（如全局/自定义）。 */
    private int type;
    
    /** 获取命名空间展示名。 */
    public String getNamespaceShowName() {
        return namespaceShowName;
    }
    
    /** 设置命名空间展示名。 */
    public void setNamespaceShowName(String namespaceShowName) {
        this.namespaceShowName = namespaceShowName;
    }
    
    /** 获取命名空间 ID。 */
    public String getNamespace() {
        return namespace;
    }
    
    /** 设置命名空间 ID。 */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    /** 无参构造。 */
    public Namespace() {
    }
    
    /**
     * 构造含 ID 与展示名的命名空间。
     *
     * @param namespace         命名空间 ID
     * @param namespaceShowName 展示名称
     */
    public Namespace(String namespace, String namespaceShowName) {
        this.namespace = namespace;
        this.namespaceShowName = namespaceShowName;
    }
    
    /**
     * 构造含配额与配置数量的命名空间。
     *
     * @param namespace         命名空间 ID
     * @param namespaceShowName 展示名称
     * @param quota             配额上限
     * @param configCount       当前配置数
     * @param type              命名空间类型
     */
    public Namespace(String namespace, String namespaceShowName, int quota, int configCount,
        int type) {
        this.namespace = namespace;
        this.namespaceShowName = namespaceShowName;
        this.quota = quota;
        this.configCount = configCount;
        this.type = type;
    }
    
    /**
     * 构造完整字段的命名空间。
     *
     * @param namespace         命名空间 ID
     * @param namespaceShowName 展示名称
     * @param namespaceDesc     描述
     * @param quota             配额上限
     * @param configCount       当前配置数
     * @param type              命名空间类型
     */
    public Namespace(String namespace, String namespaceShowName, String namespaceDesc, int quota,
        int configCount,
        int type) {
        this.namespace = namespace;
        this.namespaceShowName = namespaceShowName;
        this.quota = quota;
        this.configCount = configCount;
        this.type = type;
        this.namespaceDesc = namespaceDesc;
    }
    
    /** 获取命名空间描述。 */
    public String getNamespaceDesc() {
        return namespaceDesc;
    }
    
    /** 设置命名空间描述。 */
    public void setNamespaceDesc(String namespaceDesc) {
        this.namespaceDesc = namespaceDesc;
    }
    
    /** 获取配额上限。 */
    public int getQuota() {
        return quota;
    }
    
    /** 设置配额上限。 */
    public void setQuota(int quota) {
        this.quota = quota;
    }
    
    /** 获取当前配置数量。 */
    public int getConfigCount() {
        return configCount;
    }
    
    /** 设置当前配置数量。 */
    public void setConfigCount(int configCount) {
        this.configCount = configCount;
    }
    
    /** 获取命名空间类型。 */
    public int getType() {
        return type;
    }
    
    /** 设置命名空间类型。 */
    public void setType(int type) {
        this.type = type;
    }
    
}
