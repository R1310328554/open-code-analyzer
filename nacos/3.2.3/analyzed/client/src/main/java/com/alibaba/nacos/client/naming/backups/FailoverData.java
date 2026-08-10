/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.naming.backups;

/**
 * 容灾数据通用封装。
 *
 * <p>携带数据类型（命名/配置）与原始载荷对象，供 {@link FailoverDataSource} 读取。</p>
 *
 * @author zongkang.guo
 */
public class FailoverData {
    
    /** 容灾数据类型：命名或配置。 */
    /** failover type,naming or config. */
    /** 容灾类型，命名或配置。 */
    private final DataType dataType;
    
    /** 容灾载荷（如 {@link com.alibaba.nacos.api.naming.pojo.ServiceInfo}）。 */
    /** failover data. */
    /** 容灾数据对象。 */
    private final Object data;
    
    /** 构造指定类型与载荷的容灾数据。 */
    public FailoverData(DataType dataType, Object data) {
        this.data = data;
        this.dataType = dataType;
    }
    
    public enum DataType {
        /** 命名服务容灾数据。 */
        /** naming. */
        /** 命名模块。 */
        naming,
        /** 配置模块容灾数据。 */
        /** config. */
        /** 配置模块。 */
        config
    }
    
    /** 返回容灾数据类型。 */
    public DataType getDataType() {
        return dataType;
    }
    
    /** 返回容灾载荷对象。 */
    public Object getData() {
        return data;
    }
}
