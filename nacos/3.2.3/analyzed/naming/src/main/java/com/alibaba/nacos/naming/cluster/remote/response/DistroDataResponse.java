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

package com.alibaba.nacos.naming.cluster.remote.response;

import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.core.distributed.distro.entity.DistroData;

/**
 * 集群 Distro 数据同步 RPC 响应体。
 *
 * <p>成功时携带对端返回的 {@link DistroData}（如 QUERY/SNAPSHOT 结果）。</p>
 *
 * @author xiweng.yy
 */
public class DistroDataResponse extends Response {
    
    /** 响应中的 Distro 数据载荷。 */
    private DistroData distroData;
    
    /** 获取响应 Distro 数据。 */
    public DistroData getDistroData() {
        return distroData;
    }
    
    /** 设置响应 Distro 数据。 */
    public void setDistroData(DistroData distroData) {
        this.distroData = distroData;
    }
}
