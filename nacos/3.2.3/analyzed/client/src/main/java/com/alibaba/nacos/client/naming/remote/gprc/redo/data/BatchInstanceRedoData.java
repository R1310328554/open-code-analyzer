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

package com.alibaba.nacos.client.naming.remote.gprc.redo.data;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;
import java.util.Objects;

/**
 * 批量实例注册 redo 数据。
 *
 * <p>继承 {@link InstanceRedoData}，持有多个 {@link Instance} 列表，用于批量注册/差量注销场景的断线补偿。</p>
 *
 * @author <a href="mailto:chenhao26@xiaomi.com">chenhao26</a>
 */
public class BatchInstanceRedoData extends InstanceRedoData {
    
    /** 批量注册的实例列表。 */
    List<Instance> instances;
    
    public List<Instance> getInstances() {
        return instances;
    }
    
    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }
    
    protected BatchInstanceRedoData(String serviceName, String groupName) {
        super(serviceName, groupName);
    }
    
    /**
     * 构建批量实例 redo 数据。
     *
     * @param serviceName service name
     * @param groupName   group name
     * @param instances   instances
     * @return build BatchInstanceRedoData
     */
    public static BatchInstanceRedoData build(String serviceName, String groupName,
        List<Instance> instances) {
        BatchInstanceRedoData result = new BatchInstanceRedoData(serviceName, groupName);
        result.setInstances(instances);
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BatchInstanceRedoData)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        BatchInstanceRedoData redoData = (BatchInstanceRedoData) o;
        return Objects.equals(instances, redoData.instances);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instances);
    }
}
