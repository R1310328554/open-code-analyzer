/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.core.v2.metadata;

import java.io.Serializable;

/**
 * 元数据 CP 写操作载体。
 *
 * <p>封装命名空间、分组、服务名、标签（集群/实例标识）及元数据载荷，由 Raft 日志序列化传播。</p>
 *
 * @author xiweng.yy
 */
public class MetadataOperation<T> implements Serializable {
    
    private static final long serialVersionUID = -111405695252896706L;
    
    /** 服务所属命名空间。 */
    private String namespace;
    
    /** 服务分组。 */
    private String group;
    
    /** 服务名。 */
    private String serviceName;
    
    /** 集群名或实例 metadataId；服务级操作时可为空。 */
    private String tag;
    
    /** 元数据载荷（服务/集群/实例元数据）。 */
    private T metadata;
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    public T getMetadata() {
        return metadata;
    }
    
    public void setMetadata(T metadata) {
        this.metadata = metadata;
    }
}
