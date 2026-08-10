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

package com.alibaba.nacos.naming.cluster.remote.request;

import com.alibaba.nacos.consistency.DataOperation;
import com.alibaba.nacos.core.cluster.remote.request.AbstractClusterRequest;
import com.alibaba.nacos.core.distributed.distro.entity.DistroData;

/**
 * 集群 Distro 数据同步 RPC 请求体。
 *
 * <p>封装 {@link DistroData} 与 {@link DataOperation}，经 {@link com.alibaba.nacos.core.cluster.remote.ClusterRpcClientProxy} 在节点间传输。</p>
 *
 * @author xiweng.yy
 */
public class DistroDataRequest extends AbstractClusterRequest {
    
    /** 待同步或查询的 Distro 数据包。 */
    private DistroData distroData;
    
    /** 数据操作类型（ADD/CHANGE/DELETE/VERIFY 等）。 */
    private DataOperation dataOperation;
    
    /** 无参构造，供反序列化使用。 */
    public DistroDataRequest() {
    }
    
    /**
     * 构造带数据与操作类型的 Distro 请求。
     *
     * @param distroData    Distro 数据
     * @param dataOperation 操作类型
     */
        this.distroData = distroData;
        this.dataOperation = dataOperation;
    }
    
    public DistroData getDistroData() {
        return distroData;
    }
    
    public void setDistroData(DistroData distroData) {
        this.distroData = distroData;
    }
    
    public DataOperation getDataOperation() {
        return dataOperation;
    }
    
    public void setDataOperation(DataOperation dataOperation) {
        this.dataOperation = dataOperation;
    }
}
