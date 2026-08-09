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
package com.alibaba.csp.sentinel.slotchain;

/**
 * 处理器槽链的构建器 SPI 接口。
 *
 * @author qinan.qn
 * @author leyou
 * @author Eric Zhao
 */
public interface SlotChainBuilder {

    /**
     * 构建处理器槽链。
     *
     * @return 串联多个槽位的处理器链
     */
    ProcessorSlotChain build();
}
