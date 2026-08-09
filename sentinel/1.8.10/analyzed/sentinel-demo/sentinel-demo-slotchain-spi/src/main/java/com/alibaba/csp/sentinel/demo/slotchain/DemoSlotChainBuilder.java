/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.slotchain;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slotchain.*;
import com.alibaba.csp.sentinel.slots.DefaultSlotChainBuilder;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeSlot;
import com.alibaba.csp.sentinel.spi.Spi;
import com.alibaba.csp.sentinel.spi.SpiLoader;

import java.util.List;

/**
 * 演示如何通过 {@link SlotChainBuilder} SPI 构建自定义 ProcessorSlot 链。
 * 源码中还保留了另一种逐 Slot 加载的写法（已注释）。
 *
 * 注意 {@code ProcessorSlotChain} 本身不是 SPI，可扩展的是 {@code SlotChainBuilder}。
 * 多数场景只需自定义 {@code ProcessorSlot}，可参考 {@code sentinel-demo-slot-spi} 模块。
 *
 * Sentinel 默认 Slot 及其顺序非常关键，定制时务必参考 {@link Constants} 中的顺序常量
 * 及 {@link DefaultSlotChainBuilder} 的默认实现。
 *
 * @author cdfive
 */
@Spi
public class DemoSlotChainBuilder implements SlotChainBuilder {

    @Override
    public ProcessorSlotChain build() {
        ProcessorSlotChain chain = new DefaultProcessorSlotChain();

        List<ProcessorSlot> sortedSlotList = SpiLoader.of(ProcessorSlot.class).loadInstanceListSorted();
        // 移除 DegradeSlot：配合 DemoDegradeRuleApplication，降级规则不会触发 DegradeException
        sortedSlotList.removeIf(o -> DegradeSlot.class.equals(o.getClass()));
        for (ProcessorSlot slot : sortedSlotList) {
            if (!(slot instanceof AbstractLinkedProcessorSlot)) {
                RecordLog.warn("The ProcessorSlot(" + slot.getClass().getCanonicalName() + ") is not an instance of AbstractLinkedProcessorSlot, can't be added into ProcessorSlotChain");
                continue;
            }

            chain.addLast((AbstractLinkedProcessorSlot<?>) slot);
        }

        return chain;
    }

    /**
     * 另一种构建方式：通过 {@code SpiLoader#loadInstance} 逐个添加 Slot。
     * 默认 Slot 顺序同样重要，详见 {@link com.alibaba.csp.sentinel.Constants}。
     */
    /*
    @Override
    public ProcessorSlotChain build() {
        ProcessorSlotChain chain = new DefaultProcessorSlotChain();

        // Create a `SpiLoader` instance
        SpiLoader<ProcessorSlot> spiLoader = SpiLoader.of(ProcessorSlot.class);

        // Add `NodeSelectorSlot`, load by class
        chain.addLast((AbstractLinkedProcessorSlot<?>) spiLoader.loadInstance(NodeSelectorSlot.class));

        // Add `ClusterBuilderSlot`, load by aliasname(default is classname)
        chain.addLast((AbstractLinkedProcessorSlot<?>) spiLoader.loadInstance("com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot"));

        // Add `StatisticSlot`
        chain.addLast((AbstractLinkedProcessorSlot<?>) spiLoader.loadInstance(StatisticSlot.class));

        // Add `FlowSlot`
        chain.addLast((AbstractLinkedProcessorSlot<?>) spiLoader.loadInstance(FlowSlot.class));

        // Add `DegradeSlot`
        // Test for `DemoDegradeRuleApplication`
        // If we don't add `DegradeSlot`, the demo will not be blocked by `DegradeException`
        // If it's added, we can see the expected DegradeException
//        chain.addLast((AbstractLinkedProcessorSlot<?>) spiLoader.loadInstance(DegradeSlot.class));
        return chain;
    }
    */
}
