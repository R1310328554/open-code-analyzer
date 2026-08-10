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

/**
 * Distro 入站数据处理器：负责处理同步数据、校验数据与快照数据。
 * Distro data processor.
 *
 * @author xiweng.yy
 */
public interface DistroDataProcessor {
    
    /**
     * 返回本处理器负责的资源/处理类型标识。
     *
     * @return type of this processor
     */
    String processType();
    
    /**
     * 处理远端同步过来的 Distro 数据。
     *
     * @param distroData received data
     * @return true if process data successfully, otherwise false
     */
    boolean processData(DistroData distroData);
    
    /**
     * 处理校验数据；不一致时可从 {@code sourceAddress} 拉取完整数据修复。
     *
     * @param distroData    verify data
     * @param sourceAddress source server address, might be get data from source server
     * @return true if the data is available, otherwise false
     */
    boolean processVerifyData(DistroData distroData, String sourceAddress);
    
    /**
     * 处理全量快照数据（如启动加载阶段）。
     *
     * @param distroData snapshot data
     * @return true if process data successfully, otherwise false
     */
    boolean processSnapshot(DistroData distroData);
}
