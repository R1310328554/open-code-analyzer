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

import java.util.List;

/**
 * Distro 本地数据存储抽象：提供单条读取、全量快照与校验数据枚举，并跟踪初始化完成状态。
 * Distro data storage.
 *
 * @author xiweng.yy
 */
public interface DistroDataStorage {
    
    /** 标记本存储已完成初始化（如全量加载结束）。 */
    void finishInitial();
    
    /**
     * 是否已完成初始化。
     *
     * <p>未完成时不应向其他节点发送校验数据。
     *
     * @return {@code true} if finished, otherwise false
     */
    boolean isFinishInitial();
    
    /**
     * 按 key 读取待同步的单条 Distro 数据。
     *
     * @param distroKey key of distro datum
     * @return need to sync datum
     */
    DistroData getDistroData(DistroKey distroKey);
    
    /**
     * 返回本类型全部数据的快照（用于全量同步/加载）。
     *
     * @return all datum
     */
    DistroData getDatumSnapshot();
    
    /**
     * 返回用于定时校验的数据摘要列表。
     *
     * @return verify datum
     */
    List<DistroData> getVerifyData();
}
