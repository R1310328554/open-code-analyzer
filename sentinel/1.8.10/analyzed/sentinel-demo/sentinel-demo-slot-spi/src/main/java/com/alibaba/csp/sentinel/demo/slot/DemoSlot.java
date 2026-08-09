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
package com.alibaba.csp.sentinel.demo.slot;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.AbstractLinkedProcessorSlot;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeSlot;
import com.alibaba.csp.sentinel.slots.block.flow.FlowSlot;
import com.alibaba.csp.sentinel.spi.Spi;

/**
 * 自定义 Slot 演示：在 entry/exit 时打印当前 Context 与资源名。
 * <p>
 * {@code @Spi(order = -1500)} 表示在 {@link FlowSlot}(-2000) 之后、
 * {@link DegradeSlot}(-1000) 之前执行，详见 {@link Constants} 中的顺序常量。
 * </p>
 *
 * @author Eric Zhao
 * @author cdfive
 */
@Spi(order = -1500)
public class DemoSlot extends AbstractLinkedProcessorSlot<DefaultNode> {

    @Override
    public void entry(Context context, ResourceWrapper resourceWrapper, DefaultNode node, int count, boolean prioritized, Object... args)
            throws Throwable {
        System.out.println("------Entering for entry on DemoSlot------"); // 进入 DemoSlot
        System.out.println("Current context: " + context.getName());
        System.out.println("Current entry resource: " + context.getCurEntry().getResourceWrapper().getName());

        fireEntry(context, resourceWrapper, node, count, prioritized, args);
    }

    @Override
    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {
        System.out.println("------Exiting for entry on DemoSlot------"); // 退出 DemoSlot
        System.out.println("Current context: " + context.getName());
        System.out.println("Current entry resource: " + context.getCurEntry().getResourceWrapper().getName());

        fireExit(context, resourceWrapper, count, args);
    }
}
