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

package com.alibaba.nacos.plugin.control.tps.barrier.creator;

import com.alibaba.nacos.plugin.control.tps.barrier.TpsBarrier;

/**
 * TPS 限流屏障创建器 SPI 接口。
 *
 * <p>管控插件通过 SPI 加载不同实现，为各限流点按需创建 {@link TpsBarrier} 实例。</p>
 *
 * @author shiyiyue
 */
public interface TpsBarrierCreator {
    
    /**
     * 获取创建器唯一标识名。
     *
     * @return 创建器名称
     */
    String getName();
    
    /**
     * 为指定限流点创建 TPS 屏障。
     *
     * @param pointName 限流点名称
     * @return 新建的 TPS 限流屏障
     */
    TpsBarrier createTpsBarrier(String pointName);
}
