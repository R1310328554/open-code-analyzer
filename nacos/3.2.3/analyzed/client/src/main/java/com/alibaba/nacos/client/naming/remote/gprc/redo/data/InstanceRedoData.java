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

/**
 * 单实例注册 redo 数据。
 *
 * <p>封装服务名、分组与 {@link Instance}，供 {@link NamingGrpcRedoService} 在断线重连后补偿注册或注销。</p>
 *
 * @author xiweng.yy
 */
public class InstanceRedoData extends NamingRedoData<Instance> {
    
    protected InstanceRedoData(String serviceName, String groupName) {
        super(serviceName, groupName);
    }
    
    /**
     * 构建单实例注册 redo 数据。
     *
     * @param serviceName service name for redo data
     * @param groupName   group name for redo data
     * @param instance    instance for redo data
     * @return new {@code RedoData} for register service instance
     */
    public static InstanceRedoData build(String serviceName, String groupName, Instance instance) {
        InstanceRedoData result = new InstanceRedoData(serviceName, groupName);
        result.set(instance);
        return result;
    }
}
