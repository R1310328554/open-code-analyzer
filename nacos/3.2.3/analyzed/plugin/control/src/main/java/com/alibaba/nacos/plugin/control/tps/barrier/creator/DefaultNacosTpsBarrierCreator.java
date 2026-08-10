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
import com.alibaba.nacos.plugin.control.tps.barrier.DefaultNacosTpsBarrier;

/**
 * Nacos 默认 TPS 限流屏障创建器。
 *
 * <p>按限流点名称实例化 {@link DefaultNacosTpsBarrier}，作为内置 SPI 实现供
 * {@link TpsBarrierCreator} 机制加载。</p>
 *
 * @author shiyiyue
 */
public class DefaultNacosTpsBarrierCreator implements TpsBarrierCreator {
    
    /**
     * 返回创建器标识名 {@code nacos}。
     *
     * @return 创建器名称
     */
    @Override
    public String getName() {
        return "nacos";
    }
    
    /**
     * 为指定限流点创建默认 Nacos TPS 屏障实例。
     *
     * @param pointName 限流点名称
     * @return 新建的 {@link DefaultNacosTpsBarrier}
     */
    @Override
    public TpsBarrier createTpsBarrier(String pointName) {
        return new DefaultNacosTpsBarrier(pointName);
    }
}
