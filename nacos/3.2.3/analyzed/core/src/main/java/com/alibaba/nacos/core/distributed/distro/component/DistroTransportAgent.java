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

package com.alibaba.nacos.core.distributed.distro.component;

import com.alibaba.nacos.core.distributed.distro.entity.DistroData;
import com.alibaba.nacos.core.distributed.distro.entity.DistroKey;

/**
 * Distro 传输代理：封装跨节点同步、校验与拉取数据的网络交互。
 * Distro transport agent.
 *
 * @author xiweng.yy
 */
public interface DistroTransportAgent {
    
    /**
     * 是否支持带回调的异步传输。
     *
     * @return true if support, otherwise false
     */
    boolean supportCallbackTransport();
    
    /**
     * 同步单条 Distro 数据到目标节点（同步调用）。
     *
     * @param data         data
     * @param targetServer target server
     * @return true is sync successfully, otherwise false
     */
    boolean syncData(DistroData data, String targetServer);
    
    /**
     * 异步同步数据并在完成时回调。
     *
     * @param data         data
     * @param targetServer target server
     * @param callback     callback
     * @throws UnsupportedOperationException if method supportCallbackTransport is false, should throw {@code
     *                                       UnsupportedOperationException}
     */
    void syncData(DistroData data, String targetServer, DistroCallback callback);
    
    /**
     * 向目标节点发送校验数据（同步调用）。
     *
     * @param verifyData   verify data
     * @param targetServer target server
     * @return true is verify successfully, otherwise false
     */
    boolean syncVerifyData(DistroData verifyData, String targetServer);
    
    /**
     * 异步发送校验数据并在完成时回调。
     *
     * @param verifyData   verify data
     * @param targetServer target server
     * @param callback     callback
     * @throws UnsupportedOperationException if method supportCallbackTransport is false, should throw {@code
     *                                       UnsupportedOperationException}
     */
    void syncVerifyData(DistroData verifyData, String targetServer, DistroCallback callback);
    
    /**
     * 从目标节点拉取指定 key 的 Distro 数据。
     *
     * @param key          key of data
     * @param targetServer target server
     * @return distro data
     */
    DistroData getData(DistroKey key, String targetServer);
    
    /**
     * 从目标节点拉取全量数据快照。
     *
     * @param targetServer target server.
     * @return distro data
     */
    DistroData getDatumSnapshot(String targetServer);
}
